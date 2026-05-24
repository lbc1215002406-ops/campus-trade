<template>
  <div class="ai-form-generate">
    <div class="ai-trigger" v-if="!generated">
      <el-input
        v-model="userInput"
        type="textarea"
        :rows="3"
        placeholder="用自然语言描述你的商品，AI 帮你自动填表。例如：用了半年的iPhone 16，成色很新，屏幕有一处小划痕，心理价位3000"
        resize="none"
      />
      <el-button type="primary" class="generate-btn" @click="handleGenerate" :loading="loading">
        <el-icon><MagicStick /></el-icon> AI 生成表单
      </el-button>
    </div>

    <div v-if="loading" class="loading-box">
      <el-icon class="is-loading" :size="20"><Loading /></el-icon>
      <span>AI 正在分析你的描述…</span>
    </div>

    <div v-if="errorMsg" class="error-box">
      <el-alert :title="errorMsg" type="error" closable @close="errorMsg=''" />
    </div>

    <div v-if="generated && !loading" class="result-box">
      <el-alert title="AI 已根据你的描述生成表单，请检查修改后发布" type="success" :closable="false" style="margin-bottom:12px" />
      <div class="image-tips" v-if="generated.imageSuggestions && generated.imageSuggestions.length">
        <span class="tip-label">建议拍摄的图片：</span>
        <el-tag v-for="(tip, idx) in generated.imageSuggestions" :key="idx" type="info" size="small" class="tip-tag">{{ tip }}</el-tag>
      </div>
      <el-button size="small" type="warning" @click="resetForm" style="margin-top:8px">清除 AI 结果，手动填写</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits } from 'vue'
import { generateForm } from '../api/ai'
import { MagicStick, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['form-generated'])

const userInput = ref('')
const loading = ref(false)
const generated = ref(null)
const errorMsg = ref('')

const handleGenerate = async () => {
  if (!userInput.value.trim()) { ElMessage.warning('请输入商品描述'); return }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await generateForm(userInput.value)
    if (res.code === 200) {
      generated.value = res.data
      emit('form-generated', res.data)
    } else {
      errorMsg.value = res.message || '生成失败'
    }
  } catch (e) {
    errorMsg.value = 'AI 调用失败，请检查模型配置或网络'
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  generated.value = null
  userInput.value = ''
  emit('form-generated', null)
}
</script>

<style scoped>
.ai-form-generate { margin-bottom: 20px; }
.ai-trigger { position: relative; }
.generate-btn { margin-top: 12px; width: 100%; }
.loading-box {
  display: flex; align-items: center; gap: 10px; padding: 16px;
  color: #1890ff; font-size: 14px; background: #e6f7ff; border-radius: 8px; margin-top: 12px;
}
.error-box { margin-top: 12px; }
.result-box { margin-top: 12px; }
.image-tips { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.tip-label { font-size: 13px; color: #666; }
.tip-tag { margin-right: 4px; }
</style>
