<template>
  <el-dialog
    v-model="visible"
    :title="isLogin ? '會員登入' : '會員註冊'"
    width="90%"
    style="max-width: 420px"
    align-center
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      label-position="top"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="手機號碼" required>
        <el-input
          v-model="form.phone"
          placeholder="請輸入手機號碼 (如: 912345678)"
          type="tel"
          @input="handlePhoneInput"
        >
          <template #prepend>
            <el-select
              v-model="form.region"
              placeholder="國碼"
              style="width: 100px"
            >
              <el-option label="+886 (台灣)" value="+886" />
              <el-option label="+852 (香港)" value="+852" />
              <el-option label="+86 (中國)" value="+86" />
              <el-option label="+1 (美國)" value="+1" />
            </el-select>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item v-if="!isLogin" label="姓名" required>
        <el-input
          v-model="form.name"
          placeholder="請輸入您的姓名"
          maxlength="50"
        />
      </el-form-item>

      <el-form-item label="密碼" required>
        <el-input
          v-model="form.password"
          type="password"
          placeholder="請輸入密碼"
          prefix-icon="Lock"
          show-password
          @keyup.enter="handleSubmit"
        />
      </el-form-item>

      <el-form-item v-if="!isLogin" label="確認密碼" required>
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="請再次輸入密碼"
          prefix-icon="Lock"
          show-password
          @keyup.enter="handleSubmit"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div style="display: flex; flex-direction: column; gap: 12px">
        <div style="display: flex; gap: 8px">
          <el-button @click="handleClose" style="flex: 1">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="isLoading"
            style="flex: 1"
          >
            {{ isLogin ? '登入' : '註冊' }}
          </el-button>
        </div>
        <div style="text-align: center; font-size: 14px; color: #909399">
          <span v-if="isLogin">還沒有帳號？</span>
          <span v-else>已經有帳號？</span>
          <el-link type="primary" @click="toggleMode" style="margin-left: 4px">
            {{ isLogin ? '立即註冊' : '立即登入' }}
          </el-link>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import { useQueryClient } from '@tanstack/vue-query';
import { login, register } from '../api/generated/sdk.gen';
import { saveSession } from '../auth/session';
import { invalidateMeQuery } from '../queries/me';
import type { LoginResponse } from '../api/generated/types.gen';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'login-success': [];
}>();

const queryClient = useQueryClient();

const visible = ref(props.modelValue);
const isLogin = ref(true);
const isLoading = ref(false);

const form = reactive({
  region: '+886',
  phone: '',
  name: '',
  password: '',
  confirmPassword: '',
});

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val;
  }
);

watch(visible, (val) => {
  emit('update:modelValue', val);
});

function handlePhoneInput(val: string) {
  if (val.startsWith('0')) {
    form.phone = val.substring(1);
    ElMessage({
      message: '國際格式無需輸入首位 0',
      type: 'info',
      duration: 1500,
    });
  }
}

function toggleMode() {
  isLogin.value = !isLogin.value;
  // Clear password fields when switching
  form.password = '';
  form.confirmPassword = '';
}

function validateForm(): boolean {
  if (!form.phone || !form.password) {
    ElMessage.warning('請輸入完整的資訊');
    return false;
  }

  if (!isLogin.value) {
    if (!form.name || !form.name.trim()) {
      ElMessage.warning('請輸入姓名');
      return false;
    }

    if (form.password !== form.confirmPassword) {
      ElMessage.warning('兩次輸入的密碼不一致');
      return false;
    }

    if (form.password.length < 6) {
      ElMessage.warning('密碼長度至少為 6 個字元');
      return false;
    }
  }

  return true;
}

async function handleSubmit() {
  if (!validateForm()) {
    return;
  }

  isLoading.value = true;

  try {
    const phoneE164 = `${form.region}${form.phone}`;

    if (isLogin.value) {
      // Login
      const response = await login({
        body: {
          phoneE164,
          password: form.password,
        },
      });

      const data = response.data as LoginResponse;
      handleLoginSuccess(data);
    } else {
      // Register
      const response = await register({
        body: {
          phoneE164,
          userName: form.name,
          password: form.password,
        },
      });

      const data = response.data as LoginResponse;
      handleLoginSuccess(data);
    }
  } catch (error: any) {
    const message = error?.response?.data?.message || error?.message || '操作失敗';
    ElMessage.error(message);
  } finally {
    isLoading.value = false;
  }
}

function handleLoginSuccess(data: LoginResponse) {
  // Save session
  saveSession({
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    user: data.user,
  });

  // Invalidate /me query
  invalidateMeQuery(queryClient);

  // Close dialog
  visible.value = false;

  // Show success message
  ElNotification({
    title: isLogin.value ? '登入成功' : '註冊成功',
    message: `歡迎${isLogin.value ? '回來' : ''}，${data.user.userName}！`,
    type: 'success',
  });

  // Emit success event
  emit('login-success');
}

function handleClose() {
  visible.value = false;
  // Reset form
  form.phone = '';
  form.name = '';
  form.password = '';
  form.confirmPassword = '';
  form.region = '+886';
  isLogin.value = true;
}
</script>
