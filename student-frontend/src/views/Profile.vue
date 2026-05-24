<template>
  <div class="profile-wrapper">
    <div class="profile-card">
      <div class="profile-header">
        <div class="profile-avatar">
          <el-avatar :size="64" style="background:linear-gradient(135deg, #1890ff, #096dd9);font-size:28px">
            {{ (form.nickname || form.username || '用')[0] }}
          </el-avatar>
        </div>
        <h2>个人资料</h2>
        <p>修改您的个人信息</p>
      </div>
      <el-form :model="form" label-position="top" class="profile-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled size="large" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="form.avatar" placeholder="请输入头像图片URL" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleSave" class="save-btn" :loading="saving">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, updateProfile } from '../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const saving = ref(false)

const userInfo = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
})

const form = ref({ id: null, username: '', nickname: '', phone: '', avatar: '' })

onMounted(async () => {
  if (!userInfo.value) { router.push('/login'); return }
  const res = await getCurrentUser()
  if (res.code === 200) {
    form.value = {
      id: res.data.id,
      username: res.data.username,
      nickname: res.data.nickname || '',
      phone: res.data.phone || '',
      avatar: res.data.avatar || ''
    }
  }
})

const handleSave = async () => {
  saving.value = true
  try {
    await updateProfile(form.value)
    ElMessage.success('修改成功')
    const stored = JSON.parse(localStorage.getItem('userInfo'))
    stored.nickname = form.value.nickname
    localStorage.setItem('userInfo', JSON.stringify(stored))
    window.dispatchEvent(new Event('userLogin'))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-wrapper { display: flex; justify-content: center; }
.profile-card { width: 500px; background: #fff; border-radius: 16px; padding: 40px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
.profile-header { text-align: center; margin-bottom: 30px; }
.profile-avatar { margin-bottom: 16px; }
.profile-header h2 { margin: 0 0 6px; font-size: 22px; }
.profile-header p { margin: 0; color: #999; font-size: 14px; }
.profile-form :deep(.el-form-item__label) { font-weight: 500; }
.save-btn { width: 100%; height: 44px; font-size: 16px; }
</style>
