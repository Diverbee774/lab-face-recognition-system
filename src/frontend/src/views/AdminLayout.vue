<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">🧪 实验室管理</div>
      <nav class="nav">
        <router-link to="/admin/student" class="nav-item">
          <span class="nav-icon">👤</span>
          <span>学生管理</span>
        </router-link>
        <router-link to="/admin/lab" class="nav-item">
          <span class="nav-icon">🏠</span>
          <span>实验室管理</span>
        </router-link>
        <router-link to="/admin/recognize" class="nav-item">
          <span class="nav-icon">📷</span>
          <span>人脸识别</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">
          <span>🚪</span>
          <span>退出登录</span>
        </button>
      </div>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogout } from '@/api/admin'

const router = useRouter()

async function handleLogout() {
  try {
    await adminLogout()
  } catch (e) {
  }
  localStorage.removeItem('token')
  localStorage.removeItem('admin')
  router.push('/login')
  ElMessage.success('已退出登录')
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
}

.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  padding: 20px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: rgba(255,255,255,0.7);
  text-decoration: none;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.nav-item.router-link-active {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.nav-icon {
  font-size: 18px;
}

.sidebar-footer {
  padding: 12px 8px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: transparent;
  border: none;
  color: rgba(255,255,255,0.7);
  font-size: 14px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.main {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
}
</style>