import request from './request'

export function generateForm(userInput) {
    return request.post('/ai/generate-form', { userInput })
}

export function suggestPrice(data) {
    return request.post('/ai/suggest-price', data)
}

export function aiSearch(query) {
    return request.post('/ai/search', { query })
}

export function aiRecommend(userId) {
    return request.post('/ai/recommend', { userId })
}

export function polishDescription(title, description) {
    return request.post('/ai/polish', { title, description })
}

export function getAiConfigs() {
    return request.get('/admin/ai-config')
}

export function addAiConfig(data) {
    return request.post('/admin/ai-config', data)
}

export function updateAiConfig(id, data) {
    return request.put(`/admin/ai-config/${id}`, data)
}

export function deleteAiConfig(id) {
    return request.delete(`/admin/ai-config/${id}`)
}

export function activateAiConfig(id) {
    return request.put(`/admin/ai-config/${id}/activate`)
}

export function testAiConnection(data) {
    return request.post('/admin/ai-config/test', data)
}
