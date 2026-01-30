<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="app-brand">
        <el-icon><ChatDotRound /></el-icon>
        <span>Social Platform</span>
      </div>
      <el-space>
        <el-button type="primary" plain>登入</el-button>
        <el-button>註冊</el-button>
      </el-space>
    </el-header>
    <el-main class="app-main">
      <el-container class="content-container">
        <el-card class="welcome-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon><Notebook /></el-icon>
              <span>Welcome</span>
            </div>
          </template>
          <el-text>歡迎來到簡易社群平台。接下來會逐步補上貼文與留言功能。</el-text>
        </el-card>
      </el-container>
    </el-main>
  </el-container>
</template>

<script>
export default {
  name: 'App',
};
</script>

<style>
.app-shell {
  min-height: 100vh;
  background: #f5f7fa;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

.app-brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.app-main {
  padding: 24px;
}

.content-container {
  max-width: 960px;
  margin: 0 auto;
}

.welcome-card {
  border-radius: 12px;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
</style>
