<template>
  <div class="page-card">
    <h2 class="page-title">我的发布</h2>
    <el-table :data="products" class="styled-table" empty-text="暂无发布记录">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">
          <span class="price-cell">￥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" effect="plain">在售</el-tag>
          <el-tag v-else-if="row.status === 2" type="info" effect="plain">已售</el-tag>
          <el-tag v-else type="warning" effect="plain">已下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/product/${row.id}`)">查看</el-button>
          <el-button size="small" type="primary" @click="$router.push(`/edit/${row.id}`)">编辑</el-button>
          <el-button v-if="row.status===1" size="small" type="warning" @click="handleToggleStatus(row)">下架</el-button>
          <el-button v-if="row.status===3" size="small" type="success" @click="handleToggleStatus(row)">上架</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
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
import { getMyProducts, updateProductStatus, deleteProduct } from '../api/product'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const products = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const userInfo = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
})

const loadData = async () => {
  if (!userInfo.value) { router.push('/login'); return }
  const res = await getMyProducts({ page: page.value, pageSize: pageSize.value })
  if (res.code === 200) {
    products.value = res.data.records
    total.value = res.data.total
  }
}

onMounted(loadData)

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 3 : 1
  await updateProductStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该商品？', '提示', { type: 'warning' })
  await deleteProduct(id)
  ElMessage.success('删除成功')
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
