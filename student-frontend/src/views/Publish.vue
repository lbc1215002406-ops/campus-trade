<template>
  <div class="publish-wrapper">
    <div class="publish-card">
      <h2 class="publish-title">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
      <!-- AI 智能填写 -->
      <AiFormGenerate @form-generated="handleAiGenerated" />

      <el-form :model="form" label-position="top" class="publish-form">
        <el-form-item label="商品标题" required>
          <el-input v-model="form.title" placeholder="输入吸引人的标题..." size="large" maxlength="50" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" required>
              <el-select v-model="form.categoryId" placeholder="选择分类" size="large" style="width:100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新旧程度" required>
              <el-select v-model="form.itemCondition" placeholder="选择成色" size="large" style="width:100%">
                <el-option label="全新" :value="1" />
                <el-option label="九成新" :value="2" />
                <el-option label="八成新" :value="3" />
                <el-option label="五成新" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="售价 (元)" required>
              <el-input-number v-model="form.price" :min="0" :precision="2" size="large" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价 (元)">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" size="large" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <AiPriceSuggest
          :title="form.title"
          :description="form.description"
          :price="form.price"
          :originalPrice="form.originalPrice"
          :itemCondition="form.itemCondition"
          :categoryName="getCategoryName(form.categoryId)"
          @use-price="(price) => form.price = price"
        />
        <el-form-item label="商品图片">
          <el-upload ref="uploadRef" :action="uploadUrl" :headers="uploadHeaders" name="files" :auto-upload="true" :on-success="handleUploadSuccess" :on-error="handleUploadError" :on-remove="handleRemove" :file-list="fileList" list-type="picture-card" multiple>
            <el-icon :size="24"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持多张图片，点击上传本地图片</div>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="描述一下你的宝贝吧..." />
          <el-button size="small" type="warning" style="margin-top:8px" @click="handlePolish" :loading="polishing">
            <el-icon><MagicStick /></el-icon> AI 润色描述
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleSubmit" class="submit-btn">发布商品</el-button>
          <el-button size="large" @click="$router.push('/')">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addProduct, getProductById, updateProduct } from '../api/product'
import { getCategoryList } from '../api/category'
import { polishDescription } from '../api/ai'
import { ElMessage } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import AiFormGenerate from '../components/AiFormGenerate.vue'
import AiPriceSuggest from '../components/AiPriceSuggest.vue'


const route = useRoute()
const router = useRouter()
const categories = ref([])
const fileList = ref([])
const uploadedUrls = ref([])

const uploadUrl = 'http://localhost:8080/api/files/upload'

const userInfo = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
})

const uploadHeaders = computed(() => {
  const headers = {}
  const token = localStorage.getItem('token')
  if (token) headers['Authorization'] = 'Bearer ' + token
  return headers
})

const isEdit = computed(() => !!route.params.id)

const form = ref({
  title: '', categoryId: null, price: 0, originalPrice: null,
  itemCondition: 1, description: ''
})

onMounted(async () => {
  if (!userInfo.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  const catRes = await getCategoryList()
  if (catRes.code === 200) categories.value = catRes.data
  if (isEdit.value) {
    const res = await getProductById(route.params.id)
    if (res.code === 200) {
      const p = res.data
      form.value = { title: p.title, categoryId: p.categoryId, price: p.price, originalPrice: p.originalPrice, itemCondition: p.itemCondition, description: p.description || '' }
      if (p.images) {
        const urls = p.images.split(',').filter(i => i.trim())
        uploadedUrls.value = urls
        fileList.value = urls.map((url, idx) => ({ name: 'img-' + idx, url, uid: idx }))
      }
    }
  }
})

const handleUploadSuccess = (response, file) => {
  if (response.code === 200 && response.data && response.data.length > 0) {
    const url = response.data[0]
    uploadedUrls.value.push(url)
    file.url = url
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}
const handleUploadError = () => ElMessage.error('图片上传失败')
const handleRemove = (file) => {
  const idx = uploadedUrls.value.indexOf(file.url)
  if (idx > -1) uploadedUrls.value.splice(idx, 1)
}

const polishing = ref(false)

const getCategoryName = (id) => {
  const cat = categories.value.find(c => c.id === id)
  return cat ? cat.name : ''
}

const handleAiGenerated = (data) => {
  if (!data) return
  form.value.title = data.title || ''
  form.value.description = data.description || ''
  form.value.price = data.price || 0
  form.value.originalPrice = data.originalPrice || null
  form.value.itemCondition = data.itemCondition || 1
  if (data.categoryName) {
    const cat = categories.value.find(c => c.name === data.categoryName)
    if (cat) form.value.categoryId = cat.id
  }
}

const handlePolish = async () => {
  if (!form.value.title && !form.value.description) { ElMessage.warning('请先填写标题或描述'); return }
  polishing.value = true
  try {
    const res = await polishDescription(form.value.title, form.value.description)
    if (res.code === 200) {
      form.value.description = res.data.polished
      ElMessage.success('润色完成')
    }
  } catch { ElMessage.error('润色失败') }
  finally { polishing.value = false }
}

const handleSubmit = async () => {
  if (!form.value.title || !form.value.categoryId || !form.value.price) { ElMessage.warning('请填写必填项'); return }
  const data = { ...form.value, images: uploadedUrls.value.join(',') }
  if (isEdit.value) { await updateProduct(route.params.id, data); ElMessage.success('修改成功') }
  else { await addProduct(data); ElMessage.success('发布成功') }
  router.push('/')
}
</script>

<style scoped>
.publish-wrapper { display: flex; justify-content: center; }
.publish-card { width: 660px; background: #fff; border-radius: 16px; padding: 40px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
.publish-title { margin: 0 0 24px; font-size: 22px; text-align: center; }
.publish-form :deep(.el-form-item__label) { font-weight: 500; }
.submit-btn { padding: 0 40px; }
.upload-tip { font-size: 12px; color: #bbb; margin-top: 6px; }
</style>
