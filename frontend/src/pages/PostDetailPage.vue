<template>
  <div class="post-detail-page">
    <el-button @click="goBack" text style="margin-bottom: 16px">
      <el-icon><ArrowLeft /></el-icon>
      返回列表
    </el-button>

    <div v-if="isLoadingPost" style="text-align: center; padding: 40px">
      <el-icon class="is-loading" size="32"><Loading /></el-icon>
      <p style="color: #909399; margin-top: 16px">載入貼文中...</p>
    </div>

    <div v-else-if="postError" style="text-align: center; padding: 40px">
      <el-icon size="32" color="#f56c6c"><CircleClose /></el-icon>
      <p style="color: #f56c6c; margin-top: 16px">
        {{ postError.message || '載入貼文失敗' }}
      </p>
      <el-button @click="refetchPost" style="margin-top: 12px">重試</el-button>
    </div>

    <div v-else-if="post">
      <!-- Post Content -->
      <el-card class="post-detail-card" body-style="padding: 24px;">
        <div class="post-header">
          <el-avatar :size="48" style="background: #e6a23c">
            {{ post.authorUserName.charAt(0) }}
          </el-avatar>
          <div class="post-meta">
            <span class="author-name">{{ post.authorUserName }}</span>
            <span class="post-time">{{ formatDateTime(post.createdAt) }}</span>
          </div>
        </div>

        <div class="post-body">{{ post.content }}</div>

        <el-image
          v-if="post.imageUrl"
          :src="post.imageUrl"
          fit="cover"
          style="width: 100%; max-height: 500px; border-radius: 8px; margin-top: 16px;"
        />
      </el-card>

      <!-- Comments Section -->
      <el-card class="comments-card" style="margin-top: 20px;">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <span>留言 ({{ commentsTotal }})</span>
            <el-button v-if="!isLoadingComments" text @click="refetchComments">
              <el-icon><Refresh /></el-icon>
              重新整理
            </el-button>
          </div>
        </template>

        <div v-if="isLoadingComments" style="text-align: center; padding: 20px">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>

        <div v-else-if="commentsError" style="text-align: center; padding: 20px">
          <p style="color: #f56c6c">載入留言失敗: {{ commentsError.message }}</p>
          <el-button @click="refetchComments" size="small" style="margin-top: 8px">重試</el-button>
        </div>

        <CommentsSection v-else :comments="comments" />

        <!-- Pagination for comments -->
        <div v-if="commentsTotal > commentsLimit" style="text-align: center; margin-top: 16px">
          <el-pagination
            v-model:current-page="commentsPage"
            :page-size="commentsLimit"
            :total="commentsTotal"
            layout="prev, pager, next"
            small
            @current-change="handleCommentsPageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useGetPostsPostIdComments } from '../api/generated/@tanstack/vue-query.gen';
import { getPostsPostId } from '../api/generated/sdk.gen';
import CommentsSection from '../components/CommentsSection.vue';
import { formatDateTime } from '../utils/datetime';
import type { Post } from '../api/generated/types.gen';

const props = defineProps<{
  postId: string;
}>();

const router = useRouter();

// Load post (using direct call since we need single post, not list)
const post = ref<Post | null>(null);
const isLoadingPost = ref(true);
const postError = ref<Error | null>(null);

async function loadPost() {
  isLoadingPost.value = true;
  postError.value = null;
  try {
    const response = await getPostsPostId({ path: { postId: props.postId } });
    post.value = response.data as Post;
  } catch (err) {
    postError.value = err as Error;
  } finally {
    isLoadingPost.value = false;
  }
}

function refetchPost() {
  loadPost();
}

// Load post on mount
loadPost();

// Comments pagination
const commentsLimit = 20;
const commentsPage = ref(1);
const commentsOffset = computed(() => (commentsPage.value - 1) * commentsLimit);

const {
  data: commentsData,
  isLoading: isLoadingComments,
  error: commentsError,
  refetch: refetchComments,
} = useGetPostsPostIdComments({
  path: { postId: props.postId },
  query: {
    limit: commentsLimit,
    offset: commentsOffset,
  },
});

const comments = computed(() => commentsData.value?.data?.comments ?? []);
const commentsTotal = computed(() => commentsData.value?.data?.total ?? 0);

function handleCommentsPageChange(page: number) {
  commentsPage.value = page;
  // Scroll to comments section
  const commentsCard = document.querySelector('.comments-card');
  commentsCard?.scrollIntoView({ behavior: 'smooth' });
}

function goBack() {
  router.push({ name: 'posts' });
}
</script>

<style scoped>
.post-detail-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.post-detail-card {
  border-radius: 8px;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.post-meta {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.post-time {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.post-body {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.comments-card {
  border-radius: 8px;
}
</style>
