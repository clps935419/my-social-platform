<template>
  <div class="create-comment-form">
    <div class="flex-row" style="gap: 8px">
      <el-input
        v-model="content"
        placeholder="寫下你的看法..."
        maxlength="500"
        @keyup.enter="handleSubmit"
      />
      <el-button
        type="primary"
        plain
        @click="handleSubmit"
        :disabled="!isValid"
        :loading="isLoading"
      >
        送出
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useQueryClient } from '@tanstack/vue-query';
import { postPostsPostIdComments } from '../api/generated/sdk.gen';

const props = defineProps<{
  postId: string;
}>();

const emit = defineEmits<{
  'comment-created': [];
}>();

const queryClient = useQueryClient();

const content = ref('');
const isLoading = ref(false);

const isValid = computed(() => {
  return content.value.trim().length > 0;
});

async function handleSubmit() {
  if (!isValid.value) {
    ElMessage.warning('請輸入留言內容');
    return;
  }

  isLoading.value = true;

  try {
    await postPostsPostIdComments({
      path: { postId: props.postId },
      body: {
        content: content.value,
      },
    });

    ElMessage.success('留言已送出');

    // Clear input
    content.value = '';

    // Invalidate comments query
    queryClient.invalidateQueries({
      queryKey: ['posts', props.postId, 'comments'],
    });

    emit('comment-created');
  } catch (error: any) {
    const message = error?.response?.data?.message || '送出失敗';
    ElMessage.error(message);
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
.create-comment-form {
  margin-top: 12px;
}

.flex-row {
  display: flex;
  align-items: center;
}
</style>
