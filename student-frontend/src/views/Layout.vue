<template>
  <div class="app-wrapper">
    <header class="app-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <span class="logo-icon">川工科</span>
          <span class="logo-text">校园二手交易平台</span>
        </router-link>
        <nav class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/publish" class="nav-link nav-publish">发布商品</router-link>
        </nav>
        <div class="nav-right">
          <template v-if="userInfo">
            <el-dropdown trigger="click">
              <span class="user-avatar">
                <el-avatar :size="32" :src="userInfo.avatar" />
                <span class="user-name">{{ userInfo.nickname || userInfo.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/my/orders')">
                    <span class="dropdown-item-inner">
                      <el-icon><Tickets /></el-icon> 我的订单
                      <span v-if="unreadTotal > 0" class="unread-dot">{{ unreadTotal > 99 ? '99+' : unreadTotal }}</span>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my/products')">
                    <el-icon><Goods /></el-icon> 我的发布
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my/favorites')">
                    <el-icon><Star /></el-icon> 我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my/profile')">
                    <el-icon><User /></el-icon> 个人资料
                  </el-dropdown-item>
                  <template v-if="isAdmin">
                    <el-dropdown-item divided style="color:#f56c6c" @click="$router.push('/admin/users')">
                      <el-icon><Setting /></el-icon> 用户管理
                    </el-dropdown-item>
                    <el-dropdown-item style="color:#f56c6c" @click="$router.push('/admin/products')">
                      <el-icon><Setting /></el-icon> 商品管理
                    </el-dropdown-item>
                    <el-dropdown-item style="color:#f56c6c" @click="$router.push('/admin/ai-config')">
                      <el-icon><Cpu /></el-icon> AI 配置
                    </el-dropdown-item>
                  </template>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link">登录</router-link>
            <router-link to="/register" class="nav-btn-register">注册</router-link>
          </template>
        </div>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
    <footer class="app-footer">
      <p>川工科校园二手交易平台  让闲置流转，让资源再生</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Goods, Star, User, Setting, Cpu, SwitchButton, Tickets } from '@element-plus/icons-vue'
import { getTotalUnreadCount } from '../api/order'

const router = useRouter()
const userInfo = ref(null)
const unreadTotal = ref(0)
let pollTimer = null

const isAdmin = computed(() => userInfo.value && userInfo.value.role === 1)

const loadUser = () => {
  const stored = localStorage.getItem('userInfo')
  userInfo.value = stored ? JSON.parse(stored) : null
  if (userInfo.value) startPolling()
  else stopPolling()
}

const fetchUnreadTotal = async () => {
  if (!userInfo.value) return
  try {
    const res = await getTotalUnreadCount()
    if (res.code === 200) unreadTotal.value = res.data.total
  } catch {}
}

const startPolling = () => {
  stopPolling()
  fetchUnreadTotal()
  pollTimer = setInterval(fetchUnreadTotal, 60000)
}

const stopPolling = () => {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

loadUser()

window.addEventListener('userLogin', loadUser)

const handleLogout = () => {
  stopPolling()
  unreadTotal.value = 0
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  userInfo.value = null
  router.push('/')
}

onBeforeUnmount(() => {
  stopPolling()
  window.removeEventListener('userLogin', loadUser)
})
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}
.app-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  box-shadow: 0 2px 12px rgba(24,144,255,0.3);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 20px;
}
.logo {
  display: flex;
  align-items: center;
  text-decoration: none;
  margin-right: 40px;
}
.logo-icon {
  background: #fff;
  color: #1890ff;
  font-size: 14px;
  font-weight: bold;
  padding: 4px 10px;
  border-radius: 6px;
  margin-right: 10px;
}
.logo-text {
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}
.nav-links {
  display: flex;
  gap: 8px;
}
.nav-link {
  color: rgba(255,255,255,0.85);
  text-decoration: none;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}
.nav-link:hover, .nav-link.router-link-active {
  color: #fff;
  background: rgba(255,255,255,0.15);
}
.nav-publish {
  background: rgba(255,255,255,0.2);
  color: #fff;
}
.nav-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
.nav-btn-register {
  background: #fff;
  color: #1890ff;
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
}
.nav-btn-register:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 20px;
  transition: background 0.2s;
}
.user-avatar:hover {
  background: rgba(255,255,255,0.15);
}
.user-name {
  font-size: 14px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}
.dropdown-item-inner {
  display: flex; align-items: center; gap: 4px;
}
.unread-dot {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 18px; height: 18px; padding: 0 5px;
  background: #f56c6c; color: #fff; font-size: 11px; font-weight: 600;
  border-radius: 9px; margin-left: 4px;
}
.app-footer {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 13px;
  background: #fff;
  border-top: 1px solid #eee;
}
</style>
