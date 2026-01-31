<template>
  <div class="comments-section">
    <div v-if="isLoading" style="text-align: center; padding: 10px">
      <el-icon class="is-loading"><Loading /></el-icon>
    </div>
    <div v-else-if="comments.length > 0">
      <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
        <span class="comment-user">{{ comment.author?.userName || 'Unknown' }}:</span>
        <span>{{ comment.content }}</span>
      </div>
    </div>
    <div v-else class="no-comments">
      搶頭香！成為第一個留言的人。
    </div>

    <div class="comment-input-area">
      <slot name="comment-input" />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Comment } from '../api/generated/types.gen';

defineProps<{
  comments: Comment[];
  isLoading?: boolean;
}>();
</script>

<style scoped>
.comments-section {
  background-color: #f9fafc;
  padding: 16px;
  border-radius: 8px;
  margin-top: 16px;
}

.comment-item {
  margin-bottom: 12px;
  font-size: 14px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.comment-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.comment-user {
  font-weight: bold;
  color: #606266;
  margin-right: 4px;
}

.no-comments {
  font-size: 13px;
  color: #c0c4cc;
  text-align: center;
  margin-bottom: 10px;
}

.comment-input-area {
  margin-top: 12px;
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
