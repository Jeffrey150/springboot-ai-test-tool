<template>
  <div class="app-container">
    <div class="app-header">
      <div class="header-content">
        <div class="logo">
          <el-icon :size="24"><Platform /></el-icon>
          <span>TestAI</span>
        </div>
        <div class="nav-menu">
          <router-link to="/" class="nav-item">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/task-management" class="nav-item">
            <el-icon><Document /></el-icon>
            <span>任务管理</span>
          </router-link>
        </div>
      </div>
    </div>
    <div class="app-main">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Platform, HomeFilled, Document } from '@element-plus/icons-vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f5f7fa;
}

.app-container {
  min-height: 100vh;
}

.app-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.logo .el-icon {
  color: #3b82f6;
}

.nav-menu {
  display: flex;
  gap: 24px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: #6b7280;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.3s;
}

.nav-item:hover {
  background-color: #f3f4f6;
  color: #3b82f6;
}

.nav-item.router-link-active {
  background-color: #eff6ff;
  color: #3b82f6;
}

.app-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px 20px;
}
</style>
