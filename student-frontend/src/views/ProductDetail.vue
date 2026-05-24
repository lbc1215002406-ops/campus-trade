<template>
  <div v-if="product" class="detail-page">
    <div class="detail-main">
      <div class="detail-gallery">
        <div v-if="imageList.length > 0" class="gallery-main">
          <el-image :src="currentImage || imageList[0]" fit="contain" style="width:100%;max-height:420px" />
        </div>
        <div v-else class="no-image-large"><el-icon :size="64" color="#ddd"><Picture /></el-icon></div>
        <div class="gallery-thumbs" v-if="imageList.length > 1">
          <div v-for="(img, idx) in imageList" :key="idx" class="thumb-item" :class="{ active: (currentImage || imageList[0]) === img }" @click="currentImage = img">
            <img :src="img" />
          </div>
        </div>
      </div>

      <div class="detail-info">
        <h1 class="info-title">{{ product.title }}</h1>
        <div class="info-price-box">
          <div class="info-price">
            <span class="price-symbol">￥</span>{{ product.price }}
          </div>
          <div v-if="product.originalPrice && product.originalPrice > product.price" class="info-original">
            原价 ￥{{ product.originalPrice }}
          </div>
        </div>

        <div class="info-meta">
          <div class="meta-row"><span class="meta-label">新旧程度</span><el-tag :type="conditionType[product.itemCondition]">{{ conditionMap[product.itemCondition] }}</el-tag></div>
          <div class="meta-row"><span class="meta-label">发布时间</span><span>{{ product.createTime }}</span></div>
          <div class="meta-row"><span class="meta-label">浏览量</span><span>{{ product.viewCount }} 次</span></div>
        </div>

        <div class="info-seller">
          <el-avatar :size="40" style="background:#1890ff">{{ (sellerName || '匿')[0] }}</el-avatar>
          <div class="seller-text">
            <div class="seller-name">{{ sellerName || '匿名用户' }}</div>
            <div class="seller-tip">卖家</div>
          </div>
        </div>

        <div class="info-actions">
          <template v-if="isOwner">
            <el-button type="primary" size="large" @click="$router.push(`/edit/${product.id}`)">编辑商品</el-button>
            <el-button size="large" @click="handleToggleStatus">{{ product.status === 1 ? '下架商品' : '上架商品' }}</el-button>
            <el-button size="large" type="danger" @click="handleDelete">删除</el-button>
          </template>
          <template v-else>
            <el-button v-if="product.status === 1" type="success" size="large" @click="handlePurchase" class="buy-btn">
              <el-icon><ShoppingCart /></el-icon>
              立即购买
            </el-button>
            <el-button v-if="product.status === 1" type="danger" size="large" @click="handleToggleFavorite" class="fav-btn">
              <el-icon><Star v-if="!isFavorited" /><StarFilled v-else /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-tag v-if="product.status === 2" size="large" type="info" style="padding:12px 32px;font-size:16px">已售出</el-tag>
            <el-tag v-if="product.status === 3" size="large" type="warning" style="padding:12px 32px;font-size:16px">已下架</el-tag>
          </template>
        </div>
      </div>
    </div>

    <div class="detail-description">
      <h3>商品描述</h3>
      <div class="desc-content">{{ product.description || '卖家很懒，没有留下描述~' }}</div>
    </div>

    <AiRecommendSection />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductById, updateProductStatus, deleteProduct } from '../api/product'
import { createOrder } from '../api/order'
import { checkFavorite, addFavorite, removeFavorite } from '../api/favorite'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Star, StarFilled, ShoppingCart } from '@element-plus/icons-vue'
import AiRecommendSection from '../components/AiRecommendSection.vue'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const isFavorited = ref(false)
const sellerName = ref('')
const currentImage = ref(null)

const conditionMap = { 1: '全新', 2: '九成新', 3: '八成新', 4: '五成新' }
const conditionType = { 1: 'danger', 2: 'warning', 3: 'info', 4: '' }

const imageList = computed(() => {
  if (!product.value || !product.value.images) return []
  return product.value.images.split(',').filter(i => i.trim())
})

const userInfo = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
})

const isOwner = computed(() => userInfo.value && product.value && userInfo.value.userId === product.value.userId)

onMounted(async () => {
  const res = await getProductById(route.params.id)
  if (res.code === 200) {
    product.value = res.data
    sellerName.value = res.data.sellerName || ''
    if (userInfo.value && !isOwner.value) {
      const favRes = await checkFavorite(route.params.id)
      if (favRes.code === 200) isFavorited.value = favRes.data
    }
  }
})

const handleToggleFavorite = async () => {
  if (!userInfo.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  if (isFavorited.value) {
    await removeFavorite(route.params.id)
    isFavorited.value = false
    ElMessage.success('已取消收藏')
  } else {
    await addFavorite(route.params.id)
    isFavorited.value = true
    ElMessage.success('收藏成功')
  }
}

const handlePurchase = async () => {
  if (!userInfo.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  await ElMessageBox.confirm('确认下单购买该商品？下单后需等待卖家确认。', '下单确认', { type: 'warning', confirmButtonText: '确认下单', cancelButtonText: '取消' })
  const res = await createOrder({ productId: product.value.id })
  if (res.code === 200) {
    ElMessage.success('下单成功，请等待卖家确认')
    router.push('/my/orders')
  } else {
    ElMessage.error(res.message)
  }
}

const handleToggleStatus = async () => {
  const newStatus = product.value.status === 1 ? 3 : 1
  await updateProductStatus(product.value.id, newStatus)
  product.value.status = newStatus
  ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
}

const handleDelete = async () => {
  await ElMessageBox.confirm('确认删除该商品？', '提示', { type: 'warning' })
  await deleteProduct(product.value.id)
  ElMessage.success('删除成功')
  router.push('/')
}
</script>

<style scoped>
.detail-page { background: #fff; border-radius: 16px; padding: 30px; }
.detail-main { display: flex; gap: 40px; margin-bottom: 30px; }
.detail-gallery { width: 460px; flex-shrink: 0; }
.gallery-main { background: #fafafa; border-radius: 12px; padding: 10px; display: flex; align-items: center; justify-content: center; min-height: 400px; }
.no-image-large { background: #fafafa; border-radius: 12px; height: 400px; display: flex; align-items: center; justify-content: center; }
.gallery-thumbs { display: flex; gap: 8px; margin-top: 10px; }
.thumb-item { width: 60px; height: 60px; border-radius: 8px; overflow: hidden; cursor: pointer; border: 2px solid transparent; }
.thumb-item.active { border-color: #1890ff; }
.thumb-item img { width: 100%; height: 100%; object-fit: cover; }
.detail-info { flex: 1; }
.info-title { font-size: 22px; font-weight: 600; margin: 0 0 16px; }
.info-price-box { background: linear-gradient(135deg, #fff5f5, #fff0f0); border-radius: 12px; padding: 20px; margin-bottom: 20px; }
.info-price { font-size: 36px; color: #f56c6c; font-weight: 700; }
.price-symbol { font-size: 20px; }
.info-original { font-size: 14px; color: #bbb; text-decoration: line-through; margin-top: 4px; }
.info-meta { margin-bottom: 20px; }
.meta-row { display: flex; align-items: center; margin-bottom: 12px; }
.meta-label { color: #999; width: 80px; }
.info-seller { display: flex; align-items: center; gap: 12px; padding: 16px; background: #f5f7fa; border-radius: 12px; margin-bottom: 20px; }
.seller-name { font-weight: 500; }
.seller-tip { font-size: 12px; color: #999; }
.info-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.fav-btn { min-width: 140px; }
.detail-description { border-top: 1px solid #eee; padding-top: 24px; }
.detail-description h3 { margin: 0 0 16px; font-size: 18px; }
.desc-content { line-height: 2; color: #555; white-space: pre-wrap; font-size: 15px; }
</style>
