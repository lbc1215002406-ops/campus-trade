<template>
  <div class="page-card">
    <h2 class="page-title">
      <el-icon :size="22" color="#f56c6c"><Goods /></el-icon>
      商品管理
    </h2>

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索商品标题..." clearable size="default" style="width:320px" @clear="loadData" @keyup.enter="loadData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="products" class="styled-table" empty-text="暂无商品数据">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ row.title }}</span>
          <el-tag v-if="row.isPinned === 1" type="danger" size="small" effect="dark" style="margin-left:6px">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">
          <span class="price-cell">￥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sellerName" label="发布者" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" effect="plain">在售</el-tag>
          <el-tag v-else-if="row.status === 2" type="info" effect="plain">已售</el-tag>
          <el-tag v-else type="warning" effect="plain">已下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :type="row.isPinned === 1 ? 'warning' : ''" @click="handleTogglePin(row)">
            {{ row.isPinned === 1 ? '取消置顶' : '置顶' }}
          </el-button>
          <el-popconfirm title="确认删除该商品？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination" v-if="total > pageSize">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { deleteProduct } from '../api/product'
import request from '../api/request'
import { ElMessage } from 'element-plus'
import { Goods, Search } from '@element-plus/icons-vue'

const products = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const loadData = async () => {
  const params = { page: page.value, pageSize: pageSize.value }
  if (keyword.value) params.keyword = keyword.value
  const res = await request.get('/products/admin/all', { params })
  if (res.code === 200) {
    products.value = res.data.records
    total.value = res.data.total
  }
}

const handleTogglePin = async (row) => {
  const res = await request.put(`/products/admin/${row.id}/toggle-pin`)
  if (res.code === 200) {
    ElMessage.success(res.message)
    loadData()
  }
}

const handleDelete = async (id) => {
  await deleteProduct(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-card { background: #fff; border-radius: 16px; padding: 30px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
.page-title { margin: 0 0 24px; font-size: 22px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
.price-cell { color: #f56c6c; font-weight: 600; }
.pagination { display: flex; justify-content: center; margin-top: 24px; }
:deep(.el-table th) { background: #fafafa; color: #333; font-weight: 600; }
:deep(.el-table td) { color: #555; }
</style>
