<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>

    <div v-if="orders.length > 0">
      <el-card v-for="order in orders" :key="order.id" class="order-card" shadow="hover">
        <div class="order-header">
          <span class="order-role" :class="order.buyerId === userId ? 'role-buyer' : 'role-seller'">
            {{ order.buyerId === userId ? '我买的' : '我卖的' }}
          </span>
          <div class="order-header-right">
            <el-badge v-if="unreadCounts[order.id]" :value="unreadCounts[order.id]" class="unread-badge" />
            <el-tag :type="statusType[order.status]">{{ statusMap[order.status] }}</el-tag>
          </div>
        </div>

        <div class="order-body">
          <div class="order-product" @click="$router.push(`/product/${order.productId}`)">
            <el-image v-if="getFirstImage(order.productImages)" :src="getFirstImage(order.productImages)" fit="cover" style="width:80px;height:80px;border-radius:8px" />
            <div v-else class="order-no-img"><el-icon :size="32"><Picture /></el-icon></div>
            <div class="order-product-info">
              <div class="order-product-title">{{ order.productTitle }}</div>
              <div class="order-product-price">￥{{ order.productPrice }}</div>
            </div>
          </div>
          <div class="order-parties">
            <span>买家：{{ order.buyerName }}</span>
            <span class="parties-arrow">→</span>
            <span>卖家：{{ order.sellerName }}</span>
          </div>
          <div class="order-time">{{ order.createTime }}</div>
        </div>

        <div class="order-actions" v-if="order.status !== 2 && order.status !== 3">
          <template v-if="order.status === 0 && order.sellerId === userId">
            <el-button type="primary" size="small" @click="handleConfirm(order)">确认交易</el-button>
            <el-button size="small" @click="handleCancel(order)">拒绝</el-button>
          </template>
          <template v-if="order.status === 1">
            <el-button type="success" size="small" @click="handleComplete(order)">确认完成</el-button>
            <el-button size="small" type="warning" @click="handleCancel(order)">取消订单</el-button>
          </template>
          <template v-if="order.status === 0 && order.buyerId === userId">
            <el-button size="small" type="warning" @click="handleCancel(order)">取消下单</el-button>
          </template>
          <el-button size="small" @click="toggleChat(order)" :type="chatOpen[order.id] ? '' : 'info'">
            <el-icon><ChatDotRound /></el-icon>
            {{ chatOpen[order.id] ? '收起会话' : '交易会话' }}
          </el-button>
        </div>

        <div v-if="chatOpen[order.id]" class="chat-panel">
          <div class="chat-messages" ref="chatBoxes">
            <div v-if="messages[order.id] && messages[order.id].length > 0">
              <div v-for="msg in messages[order.id]" :key="msg.id"
                   class="chat-bubble" :class="msg.senderId === userId ? 'chat-mine' : 'chat-other'">
                <div class="chat-sender">{{ msg.senderName }}</div>
                <div class="chat-content">{{ msg.content }}</div>
                <div class="chat-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>
            <div v-else class="chat-empty">暂无消息，开始沟通吧</div>
          </div>
          <div class="chat-input-box">
            <el-input v-model="chatInput[order.id]" placeholder="输入消息..." size="small" @keyup.enter="sendMsg(order)" maxlength="500" show-word-limit />
            <el-button type="primary" size="small" @click="sendMsg(order)" :disabled="!chatInput[order.id]?.trim()">发送</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <div v-else class="empty-state">
      <el-icon :size="64" color="#ccc"><Document /></el-icon>
      <p>暂无订单</p>
    </div>

    <div class="pagination" v-if="total > pageSize">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="loadData" />
    </div>

    <el-dialog v-model="cancelDialog.visible" title="取消订单" width="400px">
      <el-input v-model="cancelDialog.reason" placeholder="请输入取消原因（选填）" type="textarea" :rows="2" />
      <template #footer>
        <el-button @click="cancelDialog.visible = false">返回</el-button>
        <el-button type="danger" @click="doCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { getOrderList, confirmOrder, completeOrder, cancelOrder, getOrderMessages, sendOrderMessage, getBatchUnreadCount } from '../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Document, ChatDotRound } from '@element-plus/icons-vue'

const orders = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const chatOpen = ref({})
const messages = ref({})
const chatInput = ref({})
const unreadCounts = ref({})
let pollTimer = null
let chatPollTimer = null

const statusMap = { 0: '待确认', 1: '交易中', 2: '已完成', 3: '已取消' }
const statusType = { 0: 'warning', 1: '', 2: 'success', 3: 'info' }

const cancelDialog = ref({ visible: false, orderId: null, reason: '' })

const userInfo = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
})

const userId = computed(() => userInfo.value ? userInfo.value.userId : null)

const getFirstImage = (images) => {
  if (!images) return null
  const arr = images.split(',').filter(i => i.trim())
  return arr.length > 0 ? arr[0] : null
}

const formatTime = (t) => {
  if (!t) return ''
  return t.substring(5, 16).replace('T', ' ')
}

const loadData = async () => {
  if (!userId.value) return
  const res = await getOrderList({ page: page.value, pageSize: pageSize.value })
  if (res.code === 200) {
    orders.value = res.data.records
    total.value = res.data.total
    loadAllUnread()
  }
}

const loadAllUnread = async () => {
  const activeIds = orders.value
    .filter(o => o.status === 0 || o.status === 1)
    .map(o => o.id)
  if (activeIds.length === 0) return
  try {
    const res = await getBatchUnreadCount(activeIds)
    if (res.code === 200) {
      const counts = res.data
      for (const orderId of activeIds) {
        const cnt = counts[orderId] || 0
        if (cnt > 0) {
          unreadCounts.value[orderId] = cnt
          if (chatOpen.value[orderId]) loadMessages(orders.value.find(o => o.id === orderId))
        } else {
          unreadCounts.value[orderId] = 0
        }
      }
    }
  } catch {}
}

const loadMessages = async (order) => {
  try {
    const res = await getOrderMessages(order.id)
    if (res.code === 200) {
      messages.value[order.id] = res.data
      unreadCounts.value[order.id] = 0
      await nextTick()
      scrollChat(order.id)
    }
  } catch {}
}

const scrollChat = (orderId) => {
  const el = document.querySelector('.chat-messages')
  if (el) el.scrollTop = el.scrollHeight
}

const anyChatOpen = () => Object.values(chatOpen.value).some(v => v)

const refreshOpenChats = () => {
  for (const order of orders.value) {
    if (chatOpen.value[order.id]) loadMessages(order)
  }
}

const startChatPolling = () => {
  stopChatPolling()
  chatPollTimer = setInterval(refreshOpenChats, 5000)
}

const stopChatPolling = () => {
  if (chatPollTimer) { clearInterval(chatPollTimer); chatPollTimer = null }
}

const toggleChat = (order) => {
  const wasOpen = chatOpen.value[order.id]
  chatOpen.value[order.id] = !wasOpen
  if (!wasOpen) {
    loadMessages(order)
    startChatPolling()
  } else {
    if (!anyChatOpen()) stopChatPolling()
  }
}

const sendMsg = async (order) => {
  const content = chatInput.value[order.id]
  if (!content || !content.trim()) return
  try {
    await sendOrderMessage(order.id, { content: content.trim() })
    chatInput.value[order.id] = ''
    loadMessages(order)
  } catch {
    ElMessage.error('发送失败')
  }
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(() => {
    if (userId.value) loadAllUnread()
  }, 30000)
}

const stopPolling = () => {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
  stopChatPolling()
}

const handleConfirm = async (order) => {
  await ElMessageBox.confirm('确认接受这笔交易？', '确认交易', { type: 'warning' })
  const res = await confirmOrder(order.id)
  if (res.code === 200) { ElMessage.success(res.message); loadData() }
  else ElMessage.error(res.message)
}

const handleComplete = async (order) => {
  await ElMessageBox.confirm('确认交易已完成？', '完成交易', { type: 'warning' })
  const res = await completeOrder(order.id)
  if (res.code === 200) { ElMessage.success(res.message); loadData() }
  else ElMessage.error(res.message)
}

const handleCancel = (order) => {
  cancelDialog.value = { visible: true, orderId: order.id, reason: '' }
}

const doCancel = async () => {
  const res = await cancelOrder(cancelDialog.value.orderId, cancelDialog.value.reason || null)
  if (res.code === 200) { ElMessage.success(res.message); cancelDialog.value.visible = false; loadData() }
  else ElMessage.error(res.message)
}

onMounted(() => { loadData(); startPolling() })
onBeforeUnmount(() => { stopPolling() })
</script>

<style scoped>
.orders-page { max-width: 900px; margin: 0 auto; }
.page-title { font-size: 22px; margin: 0 0 20px; }
.order-card { margin-bottom: 16px; border-radius: 12px; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-header-right { display: flex; align-items: center; gap: 10px; }
.unread-badge { margin-right: 4px; }
.order-role { font-size: 12px; padding: 2px 10px; border-radius: 4px; }
.role-buyer { background: #e6f7ff; color: #1890ff; }
.role-seller { background: #fff7e6; color: #fa8c16; }
.order-body { display: flex; align-items: center; gap: 20px; margin-bottom: 12px; }
.order-product { display: flex; align-items: center; gap: 12px; cursor: pointer; flex: 1; }
.order-no-img { width: 80px; height: 80px; background: #f5f5f5; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.order-product-title { font-weight: 500; margin-bottom: 4px; }
.order-product-price { color: #f56c6c; font-weight: 600; font-size: 15px; }
.order-parties { font-size: 13px; color: #666; flex-shrink: 0; }
.parties-arrow { margin: 0 6px; color: #ccc; }
.order-time { font-size: 12px; color: #bbb; flex-shrink: 0; }
.order-actions { display: flex; gap: 8px; justify-content: flex-end; border-top: 1px solid #f0f0f0; padding-top: 12px; }

.chat-panel { border-top: 1px solid #f0f0f0; margin-top: 12px; padding-top: 12px; }
.chat-messages { max-height: 280px; overflow-y: auto; padding: 8px 0; display: flex; flex-direction: column; gap: 10px; }
.chat-empty { text-align: center; color: #ccc; font-size: 13px; padding: 20px 0; }
.chat-bubble { max-width: 70%; padding: 8px 14px; border-radius: 12px; font-size: 14px; position: relative; }
.chat-mine { align-self: flex-end; background: #1890ff; color: #fff; border-bottom-right-radius: 4px; }
.chat-other { align-self: flex-start; background: #f0f2f5; color: #333; border-bottom-left-radius: 4px; }
.chat-sender { font-size: 11px; margin-bottom: 2px; opacity: 0.7; }
.chat-mine .chat-sender { color: rgba(255,255,255,0.8); }
.chat-other .chat-sender { color: #999; }
.chat-content { line-height: 1.5; word-break: break-word; }
.chat-time { font-size: 10px; margin-top: 4px; text-align: right; opacity: 0.6; }
.chat-input-box { display: flex; gap: 8px; margin-top: 10px; align-items: center; }
.chat-input-box .el-input { flex: 1; }

.empty-state { text-align: center; padding: 80px 0; color: #ccc; }
.empty-state p { margin-top: 16px; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
