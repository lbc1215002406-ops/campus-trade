import request from './request'

export function getProductList(params) {
    return request.get('/products', { params })
}

export function getProductById(id) {
    return request.get(`/products/${id}`)
}

export function addProduct(data) {
    return request.post('/products', data)
}

export function updateProduct(id, data) {
    return request.put(`/products/${id}`, data)
}

export function updateProductStatus(id, status) {
    return request.put(`/products/${id}/status`, { status })
}

export function deleteProduct(id) {
    return request.delete(`/products/${id}`)
}

export function purchaseProduct(id, buyerId) {
    return request.post(`/products/${id}/purchase`, { buyerId })
}

export function getMyProducts(params) {
    return request.get('/products/my', { params })
}
