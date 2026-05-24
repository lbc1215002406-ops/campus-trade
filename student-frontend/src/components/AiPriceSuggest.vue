<template>
  <div class="ai-price-suggest">
    <el-button size="small" circle @click="handleSuggest" :loading="loading" class="suggest-btn">
      <el-icon :size="16"><MagicStick /></el-icon>
    </el-button>
    <span class="suggest-label">AI 建议定价</span>

    <div v-if="result" class="suggest-popover">
      <div class="popover-price">￥{{ result.suggestedPrice }} <span class="popover-range">区间 ￥{{ result.priceRange?.min }} - ￥{{ result.priceRange?.max }}</span></div>
      <div class="popover-reason">{{ result.reasoning }}</div>
      <el-button size="small" type="primary" @click="$emit('use-price', result.suggestedPrice)">使用此价格</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { suggestPrice } from '../api/ai'
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  title: { type: String, default: '' },
  description: { type: String, default: '' },
  price: { type: Number, default: null },
  originalPrice: { type: Number, default: null },
  itemCondition: { type: Number, default: null },
  categoryName: { type: String, default: '' }
})

const emit = defineEmits(['use-price'])

const loading = ref(false)
const result = ref(null)

const conditionLabels = { 1: '全新', 2: '九成新', 3: '八成新', 4: '五成新' }

const handleSuggest = async () => {
  if (!props.title && !props.description) { ElMessage.warning('请先填写商品信息'); return }
  loading.value = true
  try {
    const res = await suggestPrice({
      title: props.title,
      description: props.description,
      price: props.price,
      originalPrice: props.originalPrice,
      itemCondition: props.itemCondition,
      categoryName: props.categoryName
    })
    if (res.code === 200) {
      result.value = res.data
    }
  } catch (e) {
    ElMessage.error('定价建议获取失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-price-suggest {
  display: flex; align-items: center; gap: 8px; margin-bottom: 12px;
}
.suggest-btn { border-color: #1890ff; color: #1890ff; }
.suggest-label { font-size: 13px; color: #1890ff; }
.suggest-popover {
  background: #f0f9ff; border: 1px solid #91d5ff; border-radius: 8px; padding: 12px; margin-top: 8px;
}
.popover-price { font-size: 22px; color: #f56c6c; font-weight: 700; }
.popover-range { font-size: 13px; color: #999; font-weight: 400; }
.popover-reason { font-size: 13px; color: #666; margin: 6px 0 10px; }
</style>
