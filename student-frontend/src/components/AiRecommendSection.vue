<template>
  <div v-if="recommendations.length > 0" class="ai-recommend-section">
    <div class="section-header">
      <h3 class="section-title"><el-icon><MagicStick /></el-icon> AI 为你推荐</h3>
    </div>
    <div class="recommend-grid">
      <el-card v-for="item in recommendations" :key="item.productId" class="reco-card" shadow="hover"
        :body-style="{ padding: '0' }" @click="$router.push(`/product/${item.productId}`)">
        <div class="reco-image">
          <el-image v-if="getFirstImage(item.images)" :src="getFirstImage(item.images)" fit="cover" style="width:100%;height:100%" />
          <div v-else class="no-image"><el-icon :size="36"><Picture /></el-icon></div>
        </div>
        <div class="reco-body">
          <div class="reco-title">{{ item.title }}</div>
          <div class="reco-price">￥{{ item.price }}</div>
          <el-tag size="small" type="warning" class="reco-reason">{{ item.reason }}</el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { aiRecommend } from '../api/ai'
import { MagicStick, Picture } from '@element-plus/icons-vue'

const recommendations = ref([])

const getFirstImage = (images) => {
  if (!images) return null
  const arr = images.split(',').filter(i => i.trim())
  return arr.length > 0 ? arr[0] : null
}

const loadRecommend = async () => {
  const userInfo = localStorage.getItem('userInfo')
  if (!userInfo) return
  const user = JSON.parse(userInfo)
  try {
    const res = await aiRecommend(user.userId)
    if (res.code === 200 && res.data.recommendations) {
      recommendations.value = res.data.recommendations
    }
  } catch {}
}

onMounted(loadRecommend)
</script>

<style scoped>
.ai-recommend-section { margin-top: 32px; }
.section-header { margin-bottom: 16px; }
.section-title {
  font-size: 18px; font-weight: 600; color: #333;
  display: flex; align-items: center; gap: 8px;
}
.section-title .el-icon { color: #1890ff; }
.recommend-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.reco-card { cursor: pointer; border-radius: 10px; overflow: hidden; transition: all 0.3s; border: none; }
.reco-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.1); }
.reco-image { height: 140px; background: #f5f5f5; display: flex; align-items: center; justify-content: center; }
.no-image { color: #ccc; }
.reco-body { padding: 12px; }
.reco-title { font-size: 14px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 6px; }
.reco-price { font-size: 18px; color: #f56c6c; font-weight: 700; margin-bottom: 6px; }
.reco-reason { font-size: 11px; }
</style>
