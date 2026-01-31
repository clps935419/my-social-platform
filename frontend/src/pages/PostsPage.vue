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

      <!-- Pagination -->
      <div v-if="total > limit" class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="limit"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- Auth Dialog -->
    <AuthDialog v-model="showAuthDialog" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { listPostsOptions } from '../api/generated/@tanstack/vue-query.gen';
import { useQuery } from '@tanstack/vue-query';
import { useMeQuery } from '../queries/me';
import PostCard from '../components/PostCard.vue';
import CreatePostForm from '../components/CreatePostForm.vue';
import AuthDialog from '../components/AuthDialog.vue';

const { data: currentUser } = useMeQuery();

const showAuthDialog = ref(false);

const limit = 10;
const currentPage = ref(1);
const offset = computed(() => (currentPage.value - 1) * limit);
const sortOrder = ref<'newest' | 'oldest'>('newest');
const mineOnly = ref(false);

// Create computed query options that update when offset changes
const queryOptions = computed(() => listPostsOptions({
  query: {
    limit,
    offset: offset.value,
    sort: sortOrder.value,
    mine: mineOnly.value,
  },
}));

const { data, isLoading, error, refetch } = useQuery(queryOptions);

const posts = computed(() => data.value?.items ?? []);
const total = computed(() => data.value?.total ?? 0);

watch([sortOrder, mineOnly], () => {
  currentPage.value = 1;
});

watch(currentUser, (user) => {
  if (!user) {
    mineOnly.value = false;
  }
});

function handlePageChange(page: number) {
  currentPage.value = page;
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function toggleMineOnly() {
  mineOnly.value = !mineOnly.value;
}
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

.pagination-wrap {
  text-align: center;
  margin-top: 24px;
}

</style>
