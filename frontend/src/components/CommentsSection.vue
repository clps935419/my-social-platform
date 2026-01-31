<template>
  <div class="comments-section">
    <div v-if="comments.length > 0">
      <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
        <span class="comment-user">{{ comment.authorUserName }}:</span>
        <span>{{ comment.content }}</span>
        <span class="comment-time">{{ formatRelativeTime(comment.createdAt) }}</span>
      </div>
    </div>
    <div v-else class="no-comments">
      搶頭香！成為第一個留言的人。
    </div>

    <div class="comment-input-area">
      <slot name="comment-input">
        <div class="guest-notice">
          <el-link type="primary" @click="$emit('login-required')">登入</el-link> 後即可參與討論
        </div>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatRelativeTime } from '../utils/datetime';
import type { Comment } from '../api/generated/types.gen';

defineProps<{
  comments: Comment[];
}>();

defineEmits<{
  'login-required': [];
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
  display: flex;
  flex-direction: column;
  gap: 4px;
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

.comment-time {
  font-size: 12px;
  color: #909399;
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
