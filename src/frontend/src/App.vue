<template>
  <div class="app-container">
    <aside class="sidebar">
      <div class="logo">
        <span>🧪 实验室管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        :collapse="false"
      >
        <el-menu-item index="/student">
          <span class="menu-icon">👤</span>
          <span>学生管理</span>
        </el-menu-item>
        <el-menu-item index="/lab">
          <span class="menu-icon">🏠</span>
          <span>实验室管理</span>
        </el-menu-item>
        <el-menu-item index="/recognize">
          <span class="menu-icon">📷</span>
          <span>人脸识别</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = ref(route.path)

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
})
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0,0,0,0.1);
}

.logo {
  padding: 24px 20px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  letter-spacing: 1px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  margin-top: 10px;
}

.sidebar-menu :deep(.el-menu-item) {
  color: rgba(255,255,255,0.7);
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.menu-icon {
  margin-right: 12px;
  font-size: 18px;
}

.main-content {
  flex: 1;
  background: #f5f7fa;
  overflow-y: auto;
}
</style>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>