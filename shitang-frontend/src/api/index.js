// 统一 API 封装
import request from '@/utils/request'

// ====== 认证 ======
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  userInfo: () => request.get('/auth/userinfo')
}

// ====== 统计 ======
export const statsApi = {
  overview: () => request.get('/stats')
}

// ====== 导出 ======
export const exportApi = {
  dishes: () => request.get('/export/dishes', { responseType: 'blob' }),
  orders: () => request.get('/export/orders', { responseType: 'blob' }),
  users: () => request.get('/export/users', { responseType: 'blob' })
}

// ====== 上传 ======
export const uploadApi = {
  image: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/upload/image', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  }
}

// ====== 用户 ======
export const userApi = {
  list: (params) => request.get('/users', { params }),
  updateStatus: (id, status) => request.put(`/users/${id}/status`, null, { params: { status } }),
  updateRole: (id, role) => request.put(`/users/${id}/role`, null, { params: { role } }),
  updateProfile: (data) => request.put('/users/profile', data),
  changePassword: (data) => request.put('/users/password', data)
}

// ====== 菜品 ======
export const dishApi = {
  list: (params) => request.get('/dishes', { params }),
  detail: (id) => request.get(`/dishes/${id}`),
  like: (id) => request.post(`/dishes/${id}/like`),
  // 管理员/商户
  create: (data) => request.post('/dishes', data),
  update: (id, data) => request.put(`/dishes/${id}`, data),
  remove: (id) => request.delete(`/dishes/${id}`),
  updateStatus: (id, status) => request.put(`/dishes/${id}/status`, null, { params: { status } }),
  updateTags: (id, tagIds) => request.put(`/dishes/${id}/tags`, { tagIds })
}

// ====== 推荐 ======
export const recommendApi = {
  hot: (limit = 10) => request.get('/recommend/hot', { params: { limit } }),
  highScore: (limit = 10) => request.get('/recommend/high-score', { params: { limit } }),
  forUser: (userId, limit = 10) => request.get(`/recommend/user/${userId}`, { params: { limit } }),
  mixed: (userId, limit = 10) => request.get('/recommend/mixed', { params: { userId, limit } }),
  scenario: (scenario, userId, limit = 10) => {
    const params = { scenario, limit }
    if (userId) params.userId = userId
    return request.get('/recommend/scenarios', { params })
  },
  feedback: (scenario, dishId, action) => request.post('/recommend/feedback', null, { params: { scenario, dishId, action } }),
  // 管理员:推荐效果评估
  evalAb: (windowDays = 7, scenario) => request.get('/recommend/eval/ab', { params: { windowDays, scenario } }),
  evalNdcg: (topN = 10) => request.get('/recommend/eval/ndcg', { params: { topN } })
}

// ====== 收藏 ======
export const favoriteApi = {
  add: (dishId) => request.post(`/favorites/${dishId}`),
  cancel: (dishId) => request.delete(`/favorites/${dishId}`),
  my: () => request.get('/favorites/my')
}

// ====== 订单 ======
export const orderApi = {
  create: (data) => request.post('/orders', data),
  my: () => request.get('/orders/my'),
  updateStatus: (id, status) => request.put(`/orders/${id}/status`, null, { params: { status } }),
  // 管理员:全部订单
  adminList: () => request.get('/orders/admin'),
  adminUpdateStatus: (id, status) => request.put(`/orders/${id}/status`, null, { params: { status } })
}

// ====== 评论 ======
export const commentApi = {
  create: (data) => request.post('/comments', data),
  byDish: (dishId) => request.get(`/comments/dish/${dishId}`),
  my: () => request.get('/comments/my'),
  // 管理员:全部评论
  adminList: (params) => request.get('/comments/admin', { params }),
  reply: (id, reply) => request.put(`/comments/${id}/reply`, { reply })
}

// ====== 行为 ======
export const behaviorApi = {
  my: () => request.get('/behavior/my')
}

// ====== 购物车(服务端同步) ======
export const cartApi = {
  list: () => request.get('/cart'),
  add: (dishId, quantity = 1) => request.post('/cart', { dishId, quantity }),
  update: (dishId, quantity) => request.put(`/cart/${dishId}`, { quantity }),
  remove: (dishId) => request.delete(`/cart/${dishId}`),
  clear: () => request.delete('/cart')
}

// ====== 通知 ======
export const notificationApi = {
  list: (unreadOnly = false) => request.get('/notifications', { params: { unreadOnly } }),
  unreadCount: () => request.get('/notifications/unread-count'),
  markRead: (id) => request.put(`/notifications/${id}/read`),
  markAllRead: () => request.put('/notifications/read-all')
}

// ====== 标签 / 分类 / 食堂 / 窗口(管理员用) ======
export const tagApi = {
  list: (params) => request.get('/tags', { params }),
  create: (data) => request.post('/tags', data),
  update: (id, data) => request.put(`/tags/${id}`, data),
  remove: (id) => request.delete(`/tags/${id}`)
}
export const categoryApi = {
  list: (params) => request.get('/categories', { params }),
  create: (data) => request.post('/categories', data),
  update: (id, data) => request.put(`/categories/${id}`, data),
  remove: (id) => request.delete(`/categories/${id}`)
}
export const canteenApi = {
  list: (params) => request.get('/canteens', { params }),
  detail: (id) => request.get(`/canteens/${id}`),
  create: (data) => request.post('/canteens', data),
  update: (id, data) => request.put(`/canteens/${id}`, data),
  remove: (id) => request.delete(`/canteens/${id}`)
}
export const windowApi = {
  list: (params) => request.get('/windows', { params }),
  detail: (id) => request.get(`/windows/${id}`),
  create: (data) => request.post('/windows', data),
  update: (id, data) => request.put(`/windows/${id}`, data),
  remove: (id) => request.delete(`/windows/${id}`)
}
