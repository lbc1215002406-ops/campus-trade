<template>
  <div class="page-card">
    <h2 class="page-title">
      <el-icon :size="22" color="#f56c6c"><UserFilled /></el-icon>
      用户管理
    </h2>

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名或昵称..." clearable size="default" style="width:320px" @clear="loadData" @keyup.enter="loadData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="users" class="styled-table" empty-text="暂无用户数据">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="nickname" label="昵称" width="150" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : ''" effect="plain">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="确认删除该用户？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger" :disabled="row.role === 1">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'
import { UserFilled, Search } from '@element-plus/icons-vue'

const users = ref([])
const keyword = ref('')

const loadData = async () => {
  const params = {}
  if (keyword.value) params.keyword = keyword.value
  const res = await request.get('/users/admin/list', { params })
  if (res.code === 200) users.value = res.data
}

const handleDelete = async (id) => {
  await request.delete(`/users/admin/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-card { background: #fff; border-radius: 16px; padding: 30px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
.page-title { margin: 0 0 24px; font-size: 22px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
:deep(.el-table th) { background: #fafafa; color: #333; font-weight: 600; }
:deep(.el-table td) { color: #555; }
</style>
