<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-icon">川</div>
        <h2>欢迎回来</h2>
        <p>登录川工科校园二手交易平台</p>
      </div>
      <el-form :model="form" class="auth-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入用户名/学号" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" size="large" class="auth-btn" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await login(form.value.username, form.value.password)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      ElMessage.success('登录成功')
      router.push('/')
      setTimeout(() => location.reload(), 50)
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
}
.auth-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}
.auth-header {
  text-align: center;
  margin-bottom: 30px;
}
.auth-icon {
  width: 56px; height: 56px;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  color: #fff;
  font-size: 24px; font-weight: bold;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 14px;
}
.auth-header h2 { margin: 0 0 6px; font-size: 22px; }
.auth-header p { margin: 0; color: #999; font-size: 14px; }
.auth-form { margin-top: 8px; }
.auth-btn { width: 100%; height: 44px; font-size: 16px; }
.auth-footer { text-align: center; font-size: 14px; color: #999; margin-top: 16px; }
.auth-footer a { color: #1890ff; text-decoration: none; font-weight: 500; }
</style>
