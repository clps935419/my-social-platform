<template>
  <div class="app-layout">
    <!-- Header -->
    <header class="header">
      <div class="brand" @click="goHome" aria-label="SocialApp">
        <span class="brand-logo">S</span>
      </div>
      <div class="user-area">
        <template v-if="!currentUser">
          <el-button type="primary" plain @click="showAuthDialog = true">
            <el-icon class="mr-2"><User /></el-icon> 登入
          </el-button>
        </template>

        <template v-else>
          <el-dropdown trigger="click">
            <span class="el-dropdown-link" style="cursor: pointer; display: flex; align-items: center">
              <el-avatar :size="32" style="background: #409eff; margin-right: 8px">
                {{ currentUser.userName.charAt(0) }}
              </el-avatar>
              <span style="font-weight: 500">{{ currentUser.userName }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">登出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </header>

    <!-- Main Content -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- Auth Dialog -->
    <AuthDialog v-model="showAuthDialog" @login-success="handleLoginSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import { ElMessage } from 'element-plus';
import AuthDialog from './components/AuthDialog.vue';
import { useMeQuery, clearMeQuery } from './queries/me';
import { clearSession, loadSession } from './auth/session';

const router = useRouter();
const queryClient = useQueryClient();

const showAuthDialog = ref(false);

// Load user on mount
const { data: currentUser, refetch: refetchMe } = useMeQuery();

onMounted(() => {
  // Try to load session from localStorage
  const session = loadSession();
  if (session.accessToken) {
    refetchMe();
  }
});

function goHome() {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function handleLoginSuccess() {
  refetchMe();
}

function handleLogout() {
  clearSession();
  clearMeQuery(queryClient);
  ElMessage.info('已登出');
  
  // Refresh current page
  router.go(0);
}
</script>

<style>
body {
  margin: 0;
  background-color: #f2f3f5;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.app-layout {
  max-width: 700px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #fff;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.brand-logo {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #409eff 0%, #2dd4bf 100%);
  letter-spacing: -0.5px;
  user-select: none;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-content {
  flex: 1;
}

.mr-2 {
  margin-right: 8px;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
}
</style>
