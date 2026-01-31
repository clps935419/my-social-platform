<template>
  <div v-if="isAuthor" class="post-actions">
    <el-dropdown trigger="click" @command="handleCommand">
      <el-button text>
        <el-icon><More /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="edit">
            <el-icon><Edit /></el-icon>
            編輯
          </el-dropdown-item>
          <el-dropdown-item command="delete" divided>
            <el-icon class="danger-text"><Delete /></el-icon>
            <span class="danger-text">刪除</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="showEditDialog"
      title="編輯貼文"
      width="90%"
      class="edit-dialog"
      destroy-on-close
    >
      <el-form @submit.prevent="handleUpdate">
        <el-form-item label="內容">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="圖片網址">
          <el-input
            v-model="editForm.imageUrl"
            placeholder="http/https 開頭的圖片網址"
            maxlength="2048"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleUpdate"
          :loading="isUpdating"
        >
          儲存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useQueryClient } from '@tanstack/vue-query';
import { updatePost, deletePost } from '../api/generated/sdk.gen';
import { useMeQuery } from '../queries/me';

const props = defineProps<{
  postId: string;
  authorUserId: string;
  currentContent: string;
  currentImageUrl?: string;
}>();

const emit = defineEmits<{
  updated: [];
  deleted: [];
}>();

const queryClient = useQueryClient();
const { data: currentUser } = useMeQuery();

const isAuthor = computed(() => currentUser.value?.userId === props.authorUserId);

const showEditDialog = ref(false);
const isUpdating = ref(false);

const editForm = reactive({
  content: props.currentContent,
  imageUrl: props.currentImageUrl || '',
});

function handleCommand(command: string) {
  if (command === 'edit') {
    editForm.content = props.currentContent;
    editForm.imageUrl = props.currentImageUrl || '';
    showEditDialog.value = true;
  } else if (command === 'delete') {
    handleDelete();
  }
}

async function handleUpdate() {
  if (!editForm.content.trim()) {
    ElMessage.warning('貼文內容不可為空');
    return;
  }

  isUpdating.value = true;

  try {
    await updatePost({
      path: { postId: props.postId },
      body: {
        content: editForm.content,
        image: editForm.imageUrl || undefined,
      },
    });

    ElMessage.success('貼文更新成功');
    showEditDialog.value = false;

    // Invalidate posts query using generated query key
    queryClient.invalidateQueries({
      predicate: (query) => {
        const key = query.queryKey?.[0] as { _id?: string } | undefined;
        return key?._id === 'listPosts';
      },
    });

    emit('updated');
  } catch (error: any) {
    const message = error?.response?.data?.message || '更新失敗';
    ElMessage.error(message);
  } finally {
    isUpdating.value = false;
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('確定要刪除這則貼文嗎？此操作無法復原。', '確認刪除', {
      confirmButtonText: '刪除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
    });

    await deletePost({
      path: { postId: props.postId },
    });

    ElMessage.success('貼文已刪除');

    // Invalidate posts query using generated query key
    queryClient.invalidateQueries({
      predicate: (query) => {
        const key = query.queryKey?.[0] as { _id?: string } | undefined;
        return key?._id === 'listPosts';
      },
    });

    emit('deleted');
  } catch (error: any) {
    if (error === 'cancel') {
      // User cancelled
      return;
    }
    const message = error?.response?.data?.message || '刪除失敗';
    ElMessage.error(message);
  }
}
</script>

<style scoped>
.post-actions {
  position: absolute;
  top: 12px;
  right: 12px;
}

.danger-text {
  color: #f56c6c;
}

.edit-dialog {
  max-width: 500px;
}
</style>
