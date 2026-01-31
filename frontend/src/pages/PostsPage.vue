<template>
  <div class="posts-page">
    <!-- Create Post Form (only when logged in) -->
    <transition name="el-fade-in">
      <CreatePostForm v-if="currentUser" />
    </transition>

    <div v-if="isLoading" style="text-align: center; padding: 40px">
      <el-icon class="is-loading" size="32"><Loading /></el-icon>
      <p style="color: #909399; margin-top: 16px">載入中...</p>
    </div>

    <div v-else-if="error" style="text-align: center; padding: 40px">
      <el-icon size="32" color="#f56c6c"><CircleClose /></el-icon>
      <p style="color: #f56c6c; margin-top: 16px">載入失敗: {{ error.message }}</p>
      <el-button @click="refetch" style="margin-top: 12px">重試</el-button>
    </div>

    <div v-else-if="posts && posts.length === 0" style="text-align: center; padding: 40px">
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
      <div v-if="total > limit" style="text-align: center; margin-top: 24px">
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
import { ref, computed } from 'vue';
import { useGetPosts } from '../api/generated/@tanstack/vue-query.gen';
import { useMeQuery } from '../queries/me';
import PostCard from '../components/PostCard.vue';
import CreatePostForm from '../components/CreatePostForm.vue';
import AuthDialog from '../components/AuthDialog.vue';

const { data: currentUser } = useMeQuery();

const showAuthDialog = ref(false);

const limit = 10;
const currentPage = ref(1);
const offset = computed(() => (currentPage.value - 1) * limit);

const { data, isLoading, error, refetch } = useGetPosts({
  query: {
    limit,
    offset,
  },
});

const posts = computed(() => data.value?.data?.posts ?? []);
const total = computed(() => data.value?.data?.total ?? 0);

function handlePageChange(page: number) {
  currentPage.value = page;
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
</script>

<style scoped>
.posts-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}
</style>
