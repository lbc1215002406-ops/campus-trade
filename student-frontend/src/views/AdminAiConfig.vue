<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>AI 模型配置管理</h2>
      <el-button type="primary" @click="showAddDialog"><el-icon><Plus /></el-icon> 添加厂商</el-button>
    </div>

    <el-table :data="configs" border stripe v-loading="loading" empty-text="暂无配置，请添加">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="provider" label="厂商" width="100">
        <template #default="{ row }">
          <el-tag :type="providerTag(row.provider)">{{ row.provider }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="modelName" label="模型名" width="150" />
      <el-table-column prop="baseUrl" label="API 地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="apiKey" label="API Key" width="150">
        <template #default="{ row }">{{ row.apiKey }}</template>
      </el-table-column>
      <el-table-column prop="maxTokens" label="Max Tokens" width="100" align="center" />
      <el-table-column prop="temperature" label="Temperature" width="100" align="center" />
      <el-table-column prop="isActive" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">{{ row.isActive === 1 ? '启用中' : '未启用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" v-if="row.isActive !== 1" @click="handleActivate(row.id)" link>启用</el-button>
          <el-button size="small" type="warning" @click="handleTest(row)" link>测试连接</el-button>
          <el-button size="small" @click="showEditDialog(row)" link>编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)" link>删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="厂商">
          <el-select v-model="form.provider" placeholder="选择厂商" style="width:100%">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问 (Qwen)" value="qwen" />
            <el-option label="Kimi (Moonshot)" value="kimi" />
            <el-option label="OpenAI 兼容" value="openai" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名 (自动填充)">
          <el-input v-model="form.modelName" placeholder="已自动填充，可手动修改" />
        </el-form-item>
        <el-form-item label="Base URL (自动填充)">
          <el-input v-model="form.baseUrl" placeholder="已自动填充，可手动修改" />
        </el-form-item>
        <el-form-item label="API Key (必须填写)">
          <el-input v-model="form.apiKey" placeholder="输入你的 API Key" show-password />
        </el-form-item>
        <el-form-item label="Max Tokens">
          <el-input-number v-model="form.maxTokens" :min="256" :max="32768" style="width:100%" />
        </el-form-item>
        <el-form-item label="Temperature">
          <el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAiConfigs, addAiConfig, updateAiConfig, deleteAiConfig, activateAiConfig, testAiConnection } from '../api/ai'

const configs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)

const form = ref({
  provider: 'deepseek',
  modelName: 'deepseek-chat',
  baseUrl: 'https://api.deepseek.com',
  apiKey: '',
  maxTokens: 4096,
  temperature: 0.7
})

const dialogTitle = computed(() => editingId.value ? '编辑配置' : '添加配置')

// 厂商预设配置（管理员只需填 API Key）
const providerPresets = {
  deepseek: { modelName: 'deepseek-chat', baseUrl: 'https://api.deepseek.com', maxTokens: 4096, temperature: 0.7 },
  qwen: { modelName: 'qwen-max', baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode', maxTokens: 4096, temperature: 0.7 },
  kimi: { modelName: 'moonshot-v1-8k', baseUrl: 'https://api.moonshot.cn', maxTokens: 4096, temperature: 0.7 },
  openai: { modelName: 'gpt-4o', baseUrl: 'https://api.openai.com', maxTokens: 4096, temperature: 0.7 },
  other: { modelName: '', baseUrl: '', maxTokens: 4096, temperature: 0.7 }
}

// 选择厂商时自动填充（编辑模式下不触发自动填充）
watch(() => form.value.provider, (newProvider) => {
  if (editingId.value) return // 编辑模式不自动覆盖
  const preset = providerPresets[newProvider]
  if (preset) {
    form.value.modelName = preset.modelName
    form.value.baseUrl = preset.baseUrl
    form.value.maxTokens = preset.maxTokens
    form.value.temperature = preset.temperature
  }
})

const providerTag = (p) => {
  const map = { deepseek: '', qwen: 'success', kimi: 'warning', openai: 'danger' }
  return map[p] || 'info'
}

const loadConfigs = async () => {
  loading.value = true
  const res = await getAiConfigs()
  if (res.code === 200) configs.value = res.data
  loading.value = false
}

const showAddDialog = () => {
  editingId.value = null
  form.value = { provider: 'deepseek', modelName: 'deepseek-chat', baseUrl: 'https://api.deepseek.com', apiKey: '', maxTokens: 4096, temperature: 0.7 }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  editingId.value = row.id
  form.value = { provider: row.provider, modelName: row.modelName, baseUrl: row.baseUrl, apiKey: row.apiKey, maxTokens: row.maxTokens, temperature: row.temperature }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.apiKey || !form.value.modelName || !form.value.baseUrl) {
    ElMessage.warning('请填写完整信息'); return
  }
  if (editingId.value) {
    await updateAiConfig(editingId.value, form.value)
    ElMessage.success('更新成功')
  } else {
    await addAiConfig(form.value)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadConfigs()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该配置吗？', '确认', { type: 'warning' })
  await deleteAiConfig(id)
  ElMessage.success('删除成功')
  loadConfigs()
}

const handleActivate = async (id) => {
  await activateAiConfig(id)
  ElMessage.success('已切换模型')
  loadConfigs()
}

const handleTest = async (row) => {
  const loading = ElMessage({ message: '正在测试连接...', type: 'info', duration: 0 })
  try {
    const res = await testAiConnection({ id: row.id, apiKey: row.apiKey, modelName: row.modelName, baseUrl: row.baseUrl })
    loading.close()
    if (res.code === 200) ElMessage.success('连接成功！')
    else ElMessage.error(res.message || '连接失败')
  } catch {
    loading.close()
    ElMessage.error('连接失败，请检查配置')
  }
}

onMounted(loadConfigs)
</script>

<style scoped>
.admin-page {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; font-size: 20px; }
</style>
