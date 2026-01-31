<template>
  <el-card class="post-card" shadow="hover">
    <!-- Post Actions (for author only) -->
    <PostActions
      v-if="currentUser && currentUser.userId === post.author?.userId"
      :post-id="post.postId!"
      :author-user-id="post.author!.userId!"
      :current-content="post.content!"
      :current-image-url="post.image"
    />

    <div class="post-header">
      <el-avatar :size="40" class="author-avatar">
        {{ post.author?.userName?.charAt(0) || '?' }}
      </el-avatar>
      <div class="post-meta">
        <span class="author-name">{{ post.author?.userName || 'Unknown' }}</span>
        <span class="post-time">{{ formatRelativeTime(post.createdAt!) }}</span>
      </div>
    </div>

    <div class="post-body">{{ post.content }}</div>

    <el-image
      v-if="post.image"
      :src="post.image"
      fit="cover"
      class="post-image"
    />

    <div class="post-actions">
      <el-divider content-position="left">
        <span class="comments-label">留言區 ({{ commentsTotal }})</span>
      </el-divider>
    </div>

    <!-- Comments Section - inline like prototype -->
    <CommentsSection :comments="comments" :is-loading="isLoadingComments">
      <template #comment-input>
        <CreateCommentForm v-if="currentUser" :post-id="post.postId!" />
        <div v-else class="guest-notice">
          <el-link type="primary" @click="$emit('login-required')">登入</el-link>
          <span>後即可參與討論</span>
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
const { data: commentsData, isLoading: isLoadingComments } = useQuery(
      computed(() => ({
        ...listCommentsOptions({
          path: { postId: props.post.postId ?? '' },
          query: {
            limit: 20,
            offset: 0,
          },
        }),
        enabled: Boolean(props.post.postId),
      }))
);

const comments = computed(() => commentsData.value?.items ?? []);
const commentsTotal = computed(() => commentsData.value?.total ?? 0);
</script>

<style scoped>
.post-card {
  margin-bottom: 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.post-card :deep(.el-card__body) {
  padding: 20px;
  position: relative;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.author-avatar {
  background: #e6a23c;
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

.post-image {
  width: 100%;
  max-height: 400px;
  border-radius: 8px;
  margin-top: 12px;
}

.comments-label {
  font-size: 12px;
  color: #909399;
}

.guest-notice {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
  padding: 8px 0;
  background: #f4f4f5;
  border-radius: 4px;
}
</style>
