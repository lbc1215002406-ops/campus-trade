import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '../views/Layout.vue'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Publish from '../views/Publish.vue'
import MyProducts from '../views/MyProducts.vue'
import MyFavorites from '../views/MyFavorites.vue'
import MyOrders from '../views/MyOrders.vue'
import Profile from '../views/Profile.vue'
import AdminUsers from '../views/AdminUsers.vue'
import AdminProducts from '../views/AdminProducts.vue'
import AdminAiConfig from '../views/AdminAiConfig.vue'

const routes = [
    {
        path: '/',
        component: Layout,
        children: [
            { path: '', component: Home },
            { path: 'login', component: Login },
            { path: 'register', component: Register },
            { path: 'product/:id', component: ProductDetail },
            { path: 'publish', component: Publish },
            { path: 'edit/:id', component: Publish },
            { path: 'my/products', component: MyProducts },
            { path: 'my/favorites', component: MyFavorites },
            { path: 'my/orders', component: MyOrders },
            { path: 'my/profile', component: Profile },
            { path: 'admin/users', component: AdminUsers },
            { path: 'admin/products', component: AdminProducts },
            { path: 'admin/ai-config', component: AdminAiConfig },
        ]
    },
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router
