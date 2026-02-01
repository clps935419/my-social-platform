<template>
  <el-card class="create-post-card" shadow="hover">
    <template #header>
      <div style="display: flex; align-items: center">
        <el-icon class="mr-2"><EditPen /></el-icon>
        <span>建立新貼文</span>
      </div>
    </template>

    <el-form @submit.prevent="handleSubmit">
      <el-form-item>
        <el-input
          v-model="content"
          type="textarea"
          :rows="3"
          placeholder="今天有什麼想分享的嗎？"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item>
        <div style="display: flex; justify-content: flex-end; width: 100%">
          <el-button
            type="primary"
            @click="handleSubmit"
            :disabled="!isValid"
            :loading="isLoading"
          >
            發佈
          </el-button>
        </div>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { isAxiosError } from 'axios';
import { useQueryClient } from '@tanstack/vue-query';
import { createPost } from '../api/generated/sdk.gen';
import { invalidateMeQuery } from '../queries/me';

const queryClient = useQueryClient();

const content = ref('');
const isLoading = ref(false);

const emit = defineEmits<{
  'post-created': [];
}>();

const isValid = computed(() => {
  return content.value.trim().length > 0;
});

async function handleSubmit() {
  if (!isValid.value) {
    ElMessage.warning('請輸入貼文內容');
    return;
  }

  isLoading.value = true;

  try {
    await createPost({
      body: {
        content: content.value,
      },
    });

    ElMessage.success('貼文發佈成功！');

    // Clear form
    content.value = '';

    // Invalidate posts queries and /me to refresh counts/state
    queryClient.invalidateQueries({
      predicate: (query) => {
        const key = query.queryKey?.[0] as { _id?: string } | undefined;
        return key?._id === 'listPosts' || key?._id === 'getMyPosts';
      },
    });
    invalidateMeQuery(queryClient);

    emit('post-created');
  } catch (error: unknown) {
    const message = getErrorMessage(error, '發佈失敗');
    ElMessage.error(message);
  } finally {
    isLoading.value = false;
  }
}

function getErrorMessage(error: unknown, fallback: string) {
  if (isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined;
    return data?.message || error.message || fallback;
  }
  if (error instanceof Error) {
    return error.message || fallback;
  }
  return fallback;
}
</script>

<style scoped>
.create-post-card {
  margin-bottom: 24px;
  border-radius: 8px;
}

.mr-2 {
  margin-right: 8px;
}
</style>
