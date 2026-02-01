<template>
  <div class="posts-page">
    <!-- Create Post Form (only when logged in) -->
    <transition name="el-fade-in">
      <CreatePostForm v-if="currentUser" />
    </transition>

    <div class="posts-toolbar">
      <div class="sort-group">
        <el-radio-group v-model="sortOrder" size="small" class="sort-buttons">
          <el-radio-button label="newest">最新</el-radio-button>
          <el-radio-button label="oldest">最舊</el-radio-button>
        </el-radio-group>
      </div>
      <div v-if="currentUser" class="mine-toggle">
        <el-button
          size="small"
          :type="mineOnly ? 'primary' : 'default'"
          @click="toggleMineOnly"
        >
          只看我
        </el-button>
      </div>
    </div>

    <div v-if="isLoading" class="status-block">
      <el-icon class="is-loading" size="32"><Loading /></el-icon>
      <p class="status-text">載入中...</p>
    </div>

    <div v-else-if="error" class="status-block">
      <el-icon size="32" color="#f56c6c"><CircleClose /></el-icon>
      <p class="status-text status-error">載入失敗: {{ error.message }}</p>
      <el-button class="status-retry" @click="refetch">重試</el-button>
    </div>

    <div v-else-if="posts && posts.length === 0" class="status-block">
      <el-empty description="目前沒有任何貼文" />
    </div>

    <div v-else>
      <PostCard
        v-for="post in posts"
        :key="post.postId"
        :post="post"
        @login-required="showAuthDialog = true"
      />

      <div ref="loadMoreRef" class="load-more">
        <el-icon v-if="isFetchingNextPage" class="is-loading" size="20">
          <Loading />
        </el-icon>
        <span v-else-if="hasNextPage" class="load-more-text">載入更多...</span>
        <span v-else class="load-more-text">沒有更多貼文</span>
      </div>
    </div>

    <!-- Auth Dialog -->
    <AuthDialog v-model="showAuthDialog" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import {
  getMyPostsInfiniteOptions,
  listPostsInfiniteOptions,
} from '../api/generated/@tanstack/vue-query.gen';
import { useInfiniteQuery } from '@tanstack/vue-query';
import { useMeQuery } from '../queries/me';
import PostCard from '../components/PostCard.vue';
import CreatePostForm from '../components/CreatePostForm.vue';
import AuthDialog from '../components/AuthDialog.vue';

const { data: currentUser } = useMeQuery();

const showAuthDialog = ref(false);

const limit = 5;
const sortOrder = ref<'newest' | 'oldest'>('newest');
const mineOnly = ref(false);
const loadMoreRef = ref<HTMLElement | null>(null);
const observer = ref<IntersectionObserver | null>(null);

const baseOptions = computed(() => {
  const query = {
    limit,
    sort: sortOrder.value,
  };

  if (mineOnly.value && currentUser.value) {
    return getMyPostsInfiniteOptions({ query });
  }

  return listPostsInfiniteOptions({ query });
});

const queryOptions = computed(() => ({
  ...baseOptions.value,
  initialPageParam: 0,
  getNextPageParam: (lastPage, allPages) => {
    const total = lastPage.total ?? 0;
    const loaded = allPages.reduce((sum, page) => sum + (page.items?.length ?? 0), 0);
    return loaded < total ? loaded : undefined;
  },
}));

const { data, isLoading, error, refetch, fetchNextPage, hasNextPage, isFetchingNextPage } =
  useInfiniteQuery(queryOptions);

const posts = computed(() => data.value?.pages?.flatMap((page) => page.items ?? []) ?? []);

watch([sortOrder, mineOnly], () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
});

watch(currentUser, (user) => {
  if (!user) {
    mineOnly.value = false;
  }
});

function toggleMineOnly() {
  mineOnly.value = !mineOnly.value;
}

onMounted(() => {
  observer.value = new IntersectionObserver(
    (entries) => {
      const entry = entries[0];
      if (entry?.isIntersecting && hasNextPage.value && !isFetchingNextPage.value) {
        fetchNextPage();
      }
    },
    { rootMargin: '200px' }
  );

  if (loadMoreRef.value) {
    observer.value.observe(loadMoreRef.value);
  }
});

watch(loadMoreRef, (el, prev) => {
  if (!observer.value) return;
  if (prev) observer.value.unobserve(prev);
  if (el) observer.value.observe(el);
});

onBeforeUnmount(() => {
  observer.value?.disconnect();
});
</script>

<style scoped>
.posts-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.posts-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.sort-group,
.mine-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-buttons :deep(.el-radio-button__inner) {
  padding: 6px 14px;
}

.mine-toggle :deep(.el-button) {
  padding: 6px 14px;
  height: 28px;
  line-height: 16px;
}

.status-block {
  text-align: center;
  padding: 40px;
}

.status-text {
  color: #909399;
  margin-top: 16px;
}

.status-error {
  color: #f56c6c;
}

.status-retry {
  margin-top: 12px;
}

.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 8px 0 24px;
  color: #909399;
  font-size: 13px;
}

.load-more-text {
  color: #909399;
}

</style>
