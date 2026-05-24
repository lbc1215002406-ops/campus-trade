<template>
  <div>
    <div class="search-section">
      <div class="search-inner">
        <h2 class="search-title">发现校园好物</h2>
        <div class="search-controls">
          <el-select v-model="categoryId" placeholder="全部分类" clearable style="width:140px" size="large">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-input v-model="keyword" placeholder="试试说：适合考研的护眼显示器..." clearable size="large" class="search-input" @keyup.enter="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" size="large" @click="handleSearch" class="search-btn"><el-icon><MagicStick /></el-icon> 搜索</el-button>
        </div>
        <div v-if="parsedQuery" class="parsed-query">AI 理解你的意图：{{ parsedQuery }}</div>
      </div>
    </div>

    <div class="sort-bar" v-if="products.length > 0">
      <span class="sort-label">排序：</span>
      <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
        <el-radio-button value="time_desc">最新发布</el-radio-button>
        <el-radio-button value="popularity">最多浏览</el-radio-button>
        <el-radio-button value="price_asc">价格升序</el-radio-button>
        <el-radio-button value="price_desc">价格降序</el-radio-button>
      </el-radio-group>
    </div>

    <div class="product-grid" v-if="products.length > 0">
      <el-card v-for="item in products" :key="item.id" class="product-card" shadow="hover" :body-style="{ padding: '0' }" @click="$router.push(`/product/${item.id}`)">
        <div class="card-image">
          <el-image v-if="getFirstImage(item.images)" :src="getFirstImage(item.images)" fit="cover" style="width:100%;height:100%" />
          <div v-else class="no-image">
            <el-icon :size="48"><Picture /></el-icon>
            <p>暂无图片</p>
          </div>
          <div class="card-badge" v-if="item.itemCondition === 1">全新</div>
        </div>
        <div class="card-body">
          <div class="card-title">{{ item.title }}</div>
          <div class="card-price">
            <span class="price-current">￥{{ item.price }}</span>
            <span v-if="item.originalPrice && item.originalPrice > item.price" class="price-original">￥{{ item.originalPrice }}</span>
          </div>
          <div class="card-meta">
            <span class="meta-tag">{{ conditionMap[item.itemCondition] || '未知' }}</span>
            <span class="meta-views">{{ item.viewCount }} 次浏览</span>
          </div>
        </div>
      </el-card>
    </div>
    <div v-else class="empty-state">
      <el-icon :size="64" color="#ccc"><FolderOpened /></el-icon>
      <p>暂无商品，快去发布第一件宝贝吧！</p>
    </div>

    <div class="pagination" v-if="total > pageSize">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="loadData" />
    </div>

    <AiRecommendSection />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProductList } from '../api/product'
import { getCategoryList } from '../api/category'
import { aiSearch } from '../api/ai'
import { Search, Picture, FolderOpened, MagicStick } from '@element-plus/icons-vue'
import AiRecommendSection from '../components/AiRecommendSection.vue'

const products = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const keyword = ref('')
const categoryId = ref(null)
const sortBy = ref('time_desc')
const parsedQuery = ref('')
const aiSearching = ref(false)

const conditionMap = { 1: '全新', 2: '九成新', 3: '八成新', 4: '五成新' }

const getFirstImage = (images) => {
  if (!images) return null
  const arr = images.split(',').filter(i => i.trim())
  return arr.length > 0 ? arr[0] : null
}

const loadData = async () => {
  const res = await getProductList({ page: page.value, pageSize: pageSize.value, keyword: keyword.value || null, categoryId: categoryId.value || null, sortBy: sortBy.value })
  if (res.code === 200) {
    products.value = res.data.records
    total.value = res.data.total
  }
}

const handleSearch = async () => {
  page.value = 1
  parsedQuery.value = ''
  // 自然语言搜索：输入长度>5且没有选分类时走AI解析
  if (keyword.value && keyword.value.trim().length > 5 && !categoryId.value) {
    aiSearching.value = true
    try {
      const res = await aiSearch(keyword.value)
      if (res.code === 200 && res.data) {
        const d = res.data
        if (d.categoryIds && d.categoryIds.length > 0) categoryId.value = d.categoryIds[0]
        if (d.keywords && d.keywords.length > 0) keyword.value = d.keywords.join(' ')
        if (d.priceMin != null) { /*可扩展价格筛选*/ }
        if (d.priceMax != null) { /*可扩展价格筛选*/ }
        parsedQuery.value = d.parsedQuery || ''
        if (d.sortBy === 'price_asc' || d.sortBy === 'price_desc') { /*可扩展排序*/ }
      }
    } catch {}
    aiSearching.value = false
  }
  loadData()
}

const handleSortChange = () => {
  page.value = 1
  loadData()
}

onMounted(async () => {
  const catRes = await getCategoryList()
  if (catRes.code === 200) categories.value = catRes.data
  loadData()
})
</script>

<style scoped>
.search-section {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border-radius: 16px;
  padding: 40px 20px;
  margin-bottom: 24px;
  text-align: center;
}
.search-title {
  font-size: 28px;
  color: #1890ff;
  margin: 0 0 24px;
  font-weight: 600;
}
.search-controls {
  display: flex;
  justify-content: center;
  gap: 12px;
  max-width: 700px;
  margin: 0 auto;
}
.search-input {
  flex: 1;
  max-width: 400px;
}
.search-btn {
  padding: 0 32px;
}
.parsed-query {
  margin-top: 12px; font-size: 13px; color: #1890ff;
  background: rgba(24,144,255,0.08); padding: 8px 16px; border-radius: 8px;
  display: inline-block;
}
.sort-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.sort-label {
  font-size: 14px;
  color: #666;
  flex-shrink: 0;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.product-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border: none;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}
.card-image {
  height: 200px;
  position: relative;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
.no-image {
  text-align: center;
  color: #ccc;
}
.no-image p { margin: 8px 0 0; font-size: 13px; }
.card-badge {
  position: absolute;
  top: 8px; left: 8px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 4px;
}
.card-body {
  padding: 14px;
}
.card-title {
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10px;
}
.card-price {
  margin-bottom: 10px;
}
.price-current {
  font-size: 20px;
  color: #f56c6c;
  font-weight: 700;
}
.price-original {
  font-size: 13px;
  color: #bbb;
  text-decoration: line-through;
  margin-left: 8px;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.meta-tag {
  background: #f0f5ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 4px;
}
.meta-views { color: #bbb; }
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #ccc;
}
.empty-state p { margin-top: 16px; font-size: 15px; }
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
