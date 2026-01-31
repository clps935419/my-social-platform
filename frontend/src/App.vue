<template>
  <div class="app-layout">
    <!-- Header -->
    <header class="header">
      <div class="brand" @click="goHome">SocialApp</div>
      <div class="user-area">
        <el-button type="primary" plain>
          <el-icon class="mr-2"><User /></el-icon> 登入
        </el-button>
      </div>
    </header>

    <!-- Main Content -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

function goHome() {
  router.push({ name: 'posts' });
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
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
  cursor: pointer;
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
</style>
