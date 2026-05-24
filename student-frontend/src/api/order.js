import request from './request'

export function createOrder(data) {
    return request.post('/orders', data)
}

export function getOrderList(params) {
    return request.get('/orders', { params })
}

export function getOrderById(id) {
    return request.get(`/orders/${id}`)
}

export function confirmOrder(id) {
    return request.put(`/orders/${id}/confirm`)
}

export function completeOrder(id) {
    return request.put(`/orders/${id}/complete`)
}

export function cancelOrder(id, reason) {
    return request.put(`/orders/${id}/cancel`, { reason })
}

export function getOrderMessages(orderId) {
    return request.get(`/orders/${orderId}/messages`)
}

export function sendOrderMessage(orderId, data) {
    return request.post(`/orders/${orderId}/messages`, data)
}

export function getUnreadCount(orderId) {
    return request.get(`/orders/${orderId}/messages/unread`)
}

export function getBatchUnreadCount(orderIds) {
    return request.get('/orders/messages/unread/batch', { params: { orderIds: orderIds.join(',') } })
}

export function getTotalUnreadCount() {
    return request.get('/orders/messages/unread/total')
}