<template>
  <el-card class="post-card" body-style="padding: 20px; position: relative;" shadow="hover">
    <!-- Post Actions (for author only) -->
    <PostActions
      v-if="currentUser && currentUser.userId === post.authorUserId"
      :post-id="post.postId"
      :author-user-id="post.authorUserId"
      :current-content="post.content"
      :current-image-url="post.imageUrl"
    />

    <div class="post-header">
      <el-avatar :size="40" style="background: #e6a23c">
        {{ post.authorUserName.charAt(0) }}
      </el-avatar>
      <div class="post-meta">
        <span class="author-name">{{ post.authorUserName }}</span>
        <span class="post-time">{{ formatRelativeTime(post.createdAt) }}</span>
      </div>
    </div>

    <div class="post-body">{{ post.content }}</div>

    <el-image
      v-if="post.imageUrl"
      :src="post.imageUrl"
      fit="cover"
      class="post-image"
      style="width: 100%; max-height: 400px; border-radius: 8px; margin-top: 12px;"
    />

    <div class="post-actions">
      <el-divider content-position="left">
        <span style="font-size: 12px; color: #909399">留言區 ({{ commentsTotal }})</span>
      </el-divider>
    </div>

    <!-- Comments Section - inline like prototype -->
    <CommentsSection :comments="comments" :is-loading="isLoadingComments">
      <template #comment-input>
        <CreateCommentForm v-if="currentUser" :post-id="post.postId" />
        <div v-else class="guest-notice">
          <el-link type="primary" @click="$emit('login-required')">登入</el-link> 後即可參與討論
        </div>
      </template>
    </CommentsSection>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatRelativeTime } from '../utils/datetime';
import { useMeQuery } from '../queries/me';
import { listCommentsOptions } from '../api/generated/@tanstack/vue-query.gen';
import { useQuery } from '@tanstack/vue-query';
import type { Post } from '../api/generated/types.gen';
import PostActions from './PostActions.vue';
import CommentsSection from './CommentsSection.vue';
import CreateCommentForm from './CreateCommentForm.vue';

const { data: currentUser } = useMeQuery();

const props = defineProps<{
  post: Post;
}>();

defineEmits<{
  'login-required': [];
}>();

// Load comments for this post
const {
  data: commentsData,
  isLoading: isLoadingComments,
} = useQuery(
  listCommentsOptions({
    path: { postId: props.post.postId },
    query: {
      limit: 20,
      offset: 0,
    },
  })
);

const comments = computed(() => commentsData.value?.data?.comments ?? []);
const commentsTotal = computed(() => commentsData.value?.data?.total ?? 0);
</script>

<style scoped>
.post-card {
  margin-bottom: 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.post-meta {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.post-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.post-body {
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  margin-bottom: 16px;
}

.guest-notice {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 8px 0;
  background: #f4f4f5;
  border-radius: 4px;
}
</style>
