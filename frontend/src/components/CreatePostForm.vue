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
        <el-input
          v-model="imageUrl"
          placeholder="圖片網址 (選填，http/https)"
          maxlength="2048"
        >
          <template #prepend>
            <el-icon><Picture /></el-icon>
          </template>
        </el-input>
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
import { useQueryClient } from '@tanstack/vue-query';
import { postPosts } from '../api/generated/sdk.gen';

const queryClient = useQueryClient();

const content = ref('');
const imageUrl = ref('');
const isLoading = ref(false);

const emit = defineEmits<{
  'post-created': [];
}>();

const isValid = computed(() => {
  return content.value.trim().length > 0;
});

function validateImageUrl(url: string): boolean {
  if (!url) return true; // Empty is OK
  try {
    const parsed = new URL(url);
    return parsed.protocol === 'http:' || parsed.protocol === 'https:';
  } catch {
    return false;
  }
}

async function handleSubmit() {
  if (!isValid.value) {
    ElMessage.warning('請輸入貼文內容');
    return;
  }

  if (imageUrl.value && !validateImageUrl(imageUrl.value)) {
    ElMessage.warning('圖片網址格式不正確，請使用 http 或 https 開頭的網址');
    return;
  }

  isLoading.value = true;

  try {
    await postPosts({
      body: {
        content: content.value,
        image: imageUrl.value || undefined,
      },
    });

    ElMessage.success('貼文發佈成功！');

    // Clear form
    content.value = '';
    imageUrl.value = '';

    // Invalidate posts query to refresh list
    queryClient.invalidateQueries({ queryKey: ['posts'] });

    emit('post-created');
  } catch (error: any) {
    const message = error?.response?.data?.message || '發佈失敗';
    ElMessage.error(message);
  } finally {
    isLoading.value = false;
  }
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
