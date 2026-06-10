import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
      { path: 'dishes', component: () => import('@/views/DishList.vue'), meta: { title: '菜品浏览' } },
      { path: 'dishes/:id', component: () => import('@/views/DishDetail.vue'), meta: { title: '菜品详情' } },
      { path: 'recommend', component: () => import('@/views/Recommend.vue'), meta: { title: '推荐' } },
      { path: 'compare', component: () => import('@/views/Compare.vue'), meta: { title: '菜品对比' } },
      { path: 'cart', component: () => import('@/views/Cart.vue'), meta: { title: '购物车' } },
      { path: 'favorites', component: () => import('@/views/Favorites.vue'), meta: { title: '我的收藏', requireAuth: true } },
      { path: 'orders', component: () => import('@/views/Orders.vue'), meta: { title: '我的订单', requireAuth: true } },
      { path: 'profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人饮食画像', requireAuth: true } },
      { path: 'notifications', component: () => import('@/views/Notifications.vue'), meta: { title: '消息通知', requireAuth: true } }
    ]
  },
  // 管理后台(独立布局) - 仅 ADMIN/MERCHANT
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requireAdmin: true },
    children: [
      { path: 'dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'dishes', component: () => import('@/views/admin/AdminDishes.vue'), meta: { title: '菜品管理' } },
      { path: 'orders', component: () => import('@/views/admin/AdminOrders.vue'), meta: { title: '订单管理' } },
      { path: 'eval', component: () => import('@/views/admin/AdminEval.vue'), meta: { title: '推荐效果评估' } },
      { path: 'tags', component: () => import('@/views/admin/AdminTags.vue'), meta: { title: '标签 / 分类' } },
      { path: 'canteens', component: () => import('@/views/admin/AdminCanteens.vue'), meta: { title: '食堂管理' } },
      { path: 'windows', component: () => import('@/views/admin/AdminWindows.vue'), meta: { title: '窗口管理' } },
      { path: 'comments', component: () => import('@/views/admin/AdminComments.vue'), meta: { title: '评论管理' } },
      { path: 'users', component: () => import('@/views/admin/AdminUsers.vue'), meta: { title: '用户管理' } }
    ]
  },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFound.vue'), meta: { title: '404' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  // 登录态失效但需要登录
  if (to.meta.requireAuth && !userStore.isLogin) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  // 管理员/商户专属
  if (to.meta.requireAdmin) {
    const role = userStore.userInfo?.role
    if (role !== 'ADMIN' && role !== 'MERCHANT') {
      return next(userStore.isLogin ? '/home' : '/login')
    }
  }
  // 已登录用户访问登录页:按角色自动跳转
  if (to.path === '/login' && userStore.isLogin) {
    const role = userStore.userInfo?.role
    if (role === 'ADMIN' || role === 'MERCHANT') return next('/admin')
    return next('/home')
  }
  next()
})

export default router
