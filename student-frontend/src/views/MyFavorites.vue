<template>
  <div class="page-card">
    <h2 class="page-title">我的收藏</h2>
    <el-table :data="favorites" class="styled-table" empty-text="暂无收藏记录">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="productTitle" label="商品标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="productPrice" label="价格" width="100">
        <template #default="{ row }">
          <span class="price-cell" v-if="row.productPrice">￥{{ row.productPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="收藏时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="$router.push(`/product/${row.productId}`)">查看详情</el-button>
          <el-button size="small" type="danger" @click="handleRemove(row.productId)">取消收藏</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination" v-if="total > pageSize">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMyFavorites, removeFavorite } from '../api/favorite'
import { ElMessage } from 'element-plus'

const router = useRouter()
const favorites = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const userInfo = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
})

const loadData = async () => {
  if (!userInfo.value) { router.push('/login'); return }
  const res = await getMyFavorites({ page: page.value, pageSize: pageSize.value })
  if (res.code === 200) {
    favorites.value = res.data.records
    total.value = res.data.total
  }
}

onMounted(loadData)

const handleRemove = async (productId) => {
  await removeFavorite(productId)
  ElMessage.success('已取消收藏')
  loadData()
}
</script>

<style scoped>
.page-card { background: #fff; border-radius: 16px; padding: 30px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
.page-title { margin: 0 0 24px; font-size: 22px; font-weight: 600; }
.price-cell { color: #f56c6c; font-weight: 600; }
.pagination { display: flex; justify-content: center; margin-top: 24px; }
:deep(.el-table th) { background: #fafafa; color: #333; font-weight: 600; }
:deep(.el-table td) { color: #555; }
</style>
