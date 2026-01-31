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
        <span style="font-size: 12px; color: #909399">留言區</span>
      </el-divider>
    </div>

    <div class="post-footer">
      <el-button text @click="$emit('view-detail', post.postId)">
        查看完整內容與留言
      </el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { formatRelativeTime } from '../utils/datetime';
import { useMeQuery } from '../queries/me';
import type { Post } from '../api/generated/types.gen';
import PostActions from './PostActions.vue';

const { data: currentUser } = useMeQuery();

defineProps<{
  post: Post;
}>();

defineEmits<{
  'view-detail': [postId: string];
}>();
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

.post-footer {
  text-align: center;
  margin-top: 8px;
}
</style>
