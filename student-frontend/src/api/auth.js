import request from './request'

export function login(username, password) {
    return request.post('/users/login', { username, password })
}

export function register(data) {
    return request.post('/users/register', data)
}

export function getCurrentUser() {
    return request.get('/users/me')
}

export function updateProfile(data) {
    return request.put('/users/profile', data)
}
