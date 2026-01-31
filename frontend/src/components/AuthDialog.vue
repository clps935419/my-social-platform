<template>
  <el-dialog
    v-model="visible"
    :title="isLogin ? '會員登入' : '會員註冊'"
    width="90%"
    class="auth-dialog"
    align-center
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="手機號碼" prop="phone">
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
              class="region-select"
            >
              <el-option label="+886 (台灣)" value="+886" />
              <el-option label="+852 (香港)" value="+852" />
              <el-option label="+86 (中國)" value="+86" />
              <el-option label="+1 (美國)" value="+1" />
            </el-select>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item v-if="!isLogin" label="姓名" prop="name" required>
        <el-input
          v-model="form.name"
          placeholder="請輸入您的姓名"
          maxlength="50"
        />
      </el-form-item>

      <el-form-item label="密碼" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="請輸入密碼"
          prefix-icon="Lock"
          show-password
          @keyup.enter="handleSubmit"
        />
      </el-form-item>

      <el-form-item v-if="!isLogin" label="確認密碼" prop="confirmPassword">
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
      <div class="auth-footer">
        <div class="auth-footer-actions">
          <el-button @click="handleClose" class="auth-footer-button">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="isLoading"
            class="auth-footer-button"
          >
            {{ isLogin ? '登入' : '註冊' }}
          </el-button>
        </div>
        <div class="auth-footer-switch">
          <span v-if="isLogin">還沒有帳號？</span>
          <span v-else>已經有帳號？</span>
          <el-link type="primary" @click="toggleMode" class="auth-footer-link">
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
import type { FormInstance, FormRules } from 'element-plus';
import { useQueryClient } from '@tanstack/vue-query';
import { login, register } from '../api/generated/sdk.gen';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../api/generated/types.gen';
import { saveSession } from '../auth/session';
import { invalidateMeQuery } from '../queries/me';
import { isAxiosError } from 'axios';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'login-success': [];
}>();

const queryClient = useQueryClient();

const formRef = ref<FormInstance>();
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

const rules: FormRules = {
  phone: [
    { required: true, message: '請輸入手機號碼', trigger: 'blur' },
    { pattern: /^\d{8,12}$/, message: '請輸入正確手機號碼格式', trigger: 'blur' },
  ],
  name: [
    {
      validator: (_rule, value, callback) => {
        if (isLogin.value) return callback();
        if (!value || !value.trim()) return callback(new Error('請輸入姓名'));
        return callback();
      },
      trigger: 'blur',
    },
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (isLogin.value) return callback();
        if (!value || value.length < 6) return callback(new Error('密碼長度至少為 6 個字元'));
        return callback();
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (isLogin.value) return callback();
        if (!value) return callback(new Error('請再次輸入密碼'));
        if (value !== form.password) return callback(new Error('兩次輸入的密碼不一致'));
        return callback();
      },
      trigger: 'blur',
    },
  ],
};

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
  formRef.value?.clearValidate();
}

async function handleSubmit() {
  const isValid = await formRef.value?.validate().catch(() => false);
  if (!isValid) return;

  isLoading.value = true;

  try {
    const phoneNumber = `${form.region}${form.phone}`;

    if (isLogin.value) {
      // Login
      const loginPayload: LoginRequest = {
        phoneNumber,
        password: form.password,
      };

      const response = await login({
        body: loginPayload,
      });

      const data = response.data as AuthResponse;
      handleLoginSuccess(data);
    } else {
      // Register
      const registerPayload: RegisterRequest = {
        phoneNumber,
        userName: form.name,
        password: form.password,
      };

      const response = await register({
        body: registerPayload,
      });

      const data = response.data as AuthResponse;
      handleLoginSuccess(data);
    }
  } catch (error: unknown) {
    const message = getErrorMessage(error, '操作失敗');
    ElMessage.error(message);
  } finally {
    isLoading.value = false;
  }
}

function handleLoginSuccess(data: AuthResponse) {
  if (!data.accessToken || !data.refreshToken) {
    ElMessage.error('登入資訊不完整，請重新登入');
    return;
  }

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
    message: `歡迎${isLogin.value ? '回來' : ''}，${data.user?.userName}！`,
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
.auth-dialog {
  max-width: 420px;
}

.region-select {
  width: 100px;
}

.auth-footer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.auth-footer-actions {
  display: flex;
  gap: 8px;
}

.auth-footer-button {
  flex: 1;
}

.auth-footer-switch {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #909399;
}

.auth-footer-link {
  margin-left: 4px;
}
</style>
