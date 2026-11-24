// src/router/permission.ts（简化工作版）
import router from './index'
import { Message } from '@arco-design/web-vue'

const getUserRole = (): string | null => {
  const loginUser = localStorage.getItem('loginUser')
  if (loginUser) {
    try {
      const user = JSON.parse(loginUser)
      return user.userRole || null
    } catch (e) {
      return null
    }
  }
  return null
}

const getSelectedRole = (): string | null => {
  return sessionStorage.getItem('selectedRole')
}

const roleToSelectedRoleMap: Record<string, string> = {
  'ADMIN': 'admin',
  'COUNSELOR': 'teacher',
  'USER': 'user'
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = getUserRole()
  const selectedRole = getSelectedRole()
  
  const whiteList = ['/user/login', '/user/register']
  
  if (whiteList.includes(to.path)) {
    next()
    return
  }
  
  if (!token) {
    Message.warning('请先登录')
    next('/user/login')
    return
  }
  
  if (!userRole) {
    localStorage.clear()
    sessionStorage.clear()
    Message.warning('用户信息异常，请重新登录')
    next('/user/login')
    return
  }
  
  const expectedSelectedRole = roleToSelectedRoleMap[userRole]
  if (selectedRole !== expectedSelectedRole) {
    Message.error(`请使用${expectedSelectedRole === 'admin' ? '管理者' : expectedSelectedRole === 'teacher' ? '心理老师' : '普通用户'}身份登录`)
    localStorage.clear()
    sessionStorage.clear()
    next('/user/login')
    return
  }
  
  // ✅ 管理员：可以访问所有页面
  if (userRole === 'ADMIN') {
    // 管理员不做任何限制，所有页面都能访问
    next()
    return
  }
  
  // 心理老师：放行所有非 /admin 的路径
  if (userRole === 'COUNSELOR') {
    if (to.path.startsWith('/admin')) {
      next('/teacher/dashboard')
      return
    }
    next()
    return
  }
  
  // 普通用户
  if (userRole === 'USER') {
    if (to.path.startsWith('/admin')) {
      Message.error('您没有权限访问该页面')
      next('/')
      return
    }
    next()
    return
  }
  
  next()
})

export default router