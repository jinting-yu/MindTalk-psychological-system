<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧装饰区域 -->
      <div class="login-left">
        <div class="decorative-content">
          <div class="logo-circle">
            <icon-heart />
          </div>
          <h2>心理测试</h2>
          <p>探索内心世界，了解真实自我</p>
          <div class="features">
            <div class="feature-item">
              <icon-check-circle />
              <span>专业心理测评</span>
            </div>
            <div class="feature-item">
              <icon-check-circle />
              <span>科学分析报告</span>
            </div>
            <div class="feature-item">
              <icon-check-circle />
              <span>个性化建议</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="login-right">
        <div class="form-container">
          <h1 class="form-title">用户登录</h1>
          <p class="form-subtitle">欢迎来到心理测试平台</p>

          <form @submit.prevent="handleLogin" class="login-form">
            <!-- 角色选择 -->
            <div class="form-group">
              <label>选择身份</label>
              <div class="role-selector">
                <div 
                  class="role-option"
                  :class="{ active: selectedRole === 'user' }"
                  @click="selectedRole = 'user'"
                >
                  <icon-user />
                  <span>普通用户</span>
                </div>
                <div 
                  class="role-option"
                  :class="{ active: selectedRole === 'admin' }"
                  @click="selectedRole = 'admin'"
                >
                  <icon-user-group />
                  <span>管理者</span>
                </div>
                <div 
                  class="role-option"
                  :class="{ active: selectedRole === 'teacher' }"
                  @click="selectedRole = 'teacher'"
                >
                  <icon-star />
                  <span>心理老师</span>
                </div>
              </div>
            </div>

            <!-- 账号输入框 -->
            <div class="form-group">
              <label for="userAccount">账号</label>
              <div class="input-wrapper">
                <input
                  id="userAccount"
                  v-model="formData.userAccount"
                  type="text"
                  :placeholder="getPlaceholder()"
                  required
                  :class="{ 'input-error': errors.userAccount }"
                />
                <icon-user class="input-icon" />
              </div>
              <span v-if="errors.userAccount" class="error-message">{{ errors.userAccount }}</span>
            </div>

            <!-- 密码输入框 -->
            <div class="form-group">
              <label for="userPassword">密码</label>
              <div class="input-wrapper">
                <input
                  id="userPassword"
                  v-model="formData.userPassword"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  required
                  :class="{ 'input-error': errors.userPassword }"
                />
                <icon-lock class="input-icon" />
                <icon-eye 
                  v-if="showPassword" 
                  class="password-icon"
                  @click="showPassword = false"
                />
                <icon-eye-invisible 
                  v-else 
                  class="password-icon"
                  @click="showPassword = true"
                />
              </div>
              <span v-if="errors.userPassword" class="error-message">{{ errors.userPassword }}</span>
            </div>

            <!-- 登录按钮 -->
            <button type="submit" class="login-btn" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else>登录</span>
            </button>

            <!-- 注册链接 -->
            <div class="signup-link">
              还没有账号？
              <a href="#" @click.prevent="handleSignUp">新用户注册</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useLoginUserStore } from '@/store/userStore'
import { userLoginUsingPost } from '@/api/userController'
import {
  IconUser,
  IconLock,
  IconEye,
  IconEyeInvisible,
  IconHeart,
  IconCheckCircle,
  IconUserGroup,
  IconStar,
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const showPassword = ref(false)
const selectedRole = ref('user')

// 表单数据
const formData = reactive({
  userAccount: '',
  userPassword: '',
})

// 错误信息
const errors = reactive({
  userAccount: '',
  userPassword: ''
})

// 角色映射表（统一管理）
const ROLE_MAP = {
  user: { value: 'USER', name: '普通用户', path: '/' },
  admin: { value: 'ADMIN', name: '管理者', path: '/admin/user' },
  teacher: { value: 'COUNSELOR', name: '心理老师', path: '/teacher/dashboard' }
}

/**
 * 根据角色获取输入框占位符
 */
const getPlaceholder = () => {
  const roleMap = {
    user: '请输入用户名',
    admin: '请输入管理员账号',
    teacher: '请输入老师账号'
  }
  return roleMap[selectedRole.value as keyof typeof roleMap] || '请输入账号'
}

/**
 * 验证表单
 */
const validateForm = () => {
  let isValid = true
  errors.userAccount = ''
  errors.userPassword = ''

  if (!formData.userAccount) {
    errors.userAccount = '请输入账号'
    isValid = false
  }

  if (!formData.userPassword) {
    errors.userPassword = '请输入密码'
    isValid = false
  }

  return isValid
}

/**
 * 验证身份是否匹配（核心权限控制）
 */
const validateRoleMatch = (userRole: string) => {
  const selected = selectedRole.value
  const expectedRole = ROLE_MAP[selected as keyof typeof ROLE_MAP]?.value
  
  // 普通用户不能登录管理员或老师账号
  if (selected === 'user' && userRole !== 'USER') {
    let errorMsg = ''
    if (userRole === 'ADMIN') {
      errorMsg = '该账号是管理员账号，请选择"管理者"身份登录'
    } else if (userRole === 'COUNSELOR') {
      errorMsg = '该账号是心理老师账号，请选择"心理老师"身份登录'
    } else {
      errorMsg = '您选择的身份与账号不匹配，请重新选择'
    }
    Message.error(errorMsg)
    return false
  }
  
  // 管理员只能登录管理员账号
  if (selected === 'admin' && userRole !== 'ADMIN') {
    Message.error('该账号不是管理员，无法以管理者身份登录')
    return false
  }
  
  // 心理老师只能登录心理老师账号
  if (selected === 'teacher' && userRole !== 'COUNSELOR') {
    Message.error('该账号不是心理老师，无法以心理老师身份登录')
    return false
  }
  
  return true
}

/**
 * 根据角色跳转
 */
const redirectByRole = (userRole: string) => {
  const redirect = route.query.redirect as string
  
  if (redirect) {
    router.push(redirect)
  } else if (userRole === 'ADMIN') {
    router.push('/admin/user')
  } else if (userRole === 'COUNSELOR') {
    router.push('/teacher/dashboard')
  } else {
    router.push('/')
  }
}

/**
 * 清除存储数据（解决请求头过大问题）
 */
const clearStorage = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('loginUser')
  sessionStorage.removeItem('selectedRole')
}

/**
 * 登录处理
 */
const handleLogin = async () => {
  if (!validateForm()) return

  loading.value = true
  try {
    const res = await userLoginUsingPost({
      username: formData.userAccount,
      password: formData.userPassword
    })
    
    console.log('Login response:', res)
    
    // 兼容多种响应格式
    let responseData = res
    if (res.data) {
      responseData = res.data
    }
    
    // 判断登录成功
    if (responseData.code === 200 && responseData.data) {
      const { token, user: userInfo } = responseData.data
      
      // 检查用户信息是否存在
      if (!userInfo) {
        Message.error('登录返回数据异常')
        return
      }
      
      const userRole = userInfo.role
      console.log('用户角色:', userRole)
      
      // 关键：验证身份匹配
      if (!validateRoleMatch(userRole)) {
        return
      }
      
      // 保存 token
      if (token) {
        localStorage.setItem('token', token)
        console.log('Token已保存')
      }
      
      // 保存用户信息到 store
      loginUserStore.setLoginUser({
        id: userInfo.id,
        userName: userInfo.realName || userInfo.username,
        userAccount: userInfo.username,
        userRole: userInfo.role,
        avatar: userInfo.avatar || '',
        email: userInfo.email || '',
        phone: userInfo.phone || ''
      })

      // 保存用户信息到 localStorage
      localStorage.setItem('loginUser', JSON.stringify({
        id: userInfo.id,
        userName: userInfo.realName || userInfo.username,
        userAccount: userInfo.username,
        userRole: userInfo.role,
        email: userInfo.email || '',
        phone: userInfo.phone || ''
      }))
      
      // 保存选择的角色到 sessionStorage
      sessionStorage.setItem('selectedRole', selectedRole.value)
      
      Message.success('登录成功！')
      
      // 根据角色跳转
      setTimeout(() => {
        redirectByRole(userRole)
      }, 100)
    } else {
      Message.error(responseData.message || '登录失败，请检查账号密码')
    }
  } catch (error: any) {
    console.error('Login error:', error)
    
    // 详细的错误处理
    if (error.response?.status === 431) {
      Message.error('请求头过大，请清除浏览器缓存后重试')
      clearStorage()
      // 提示用户刷新页面
      setTimeout(() => {
        window.location.reload()
      }, 1500)
    } else if (error.message === 'Network Error' || error.message?.includes('Failed to fetch')) {
      Message.error('网络错误，请检查后端服务是否启动')
    } else if (error.name === 'SyntaxError') {
      Message.error('服务器响应异常，请稍后重试')
    } else {
      Message.error(error.message || '登录失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到注册页
 */
const handleSignUp = () => {
  router.push('/user/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6f5 100%);
  padding: 20px;
}

.login-card {
  display: flex;
  width: 950px;
  max-width: 100%;
  background: white;
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
}

/* 左侧装饰区域 - 治愈系渐变 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #9bcd9c 0%, #a7d8c6 50%, #b8e1d4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.decorative-content {
  text-align: center;
  color: white;
}

.logo-circle {
  width: 70px;
  height: 70px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 36px;
  backdrop-filter: blur(4px);
}

.decorative-content h2 {
  font-size: 26px;
  margin-bottom: 8px;
  font-weight: 500;
  letter-spacing: 2px;
}

.decorative-content > p {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 28px;
}

.features {
  text-align: left;
  margin-top: 28px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  font-size: 13px;
  opacity: 0.95;
}

.feature-item svg {
  font-size: 16px;
}

/* 右侧表单区域 */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.form-container {
  width: 100%;
  max-width: 320px;
}

.form-title {
  font-size: 26px;
  font-weight: 500;
  color: #2c5f2d;
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.form-subtitle {
  color: #8ba888;
  font-size: 13px;
  margin-bottom: 28px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: #5a7c5b;
  letter-spacing: 0.5px;
}

/* 角色选择器 */
.role-selector {
  display: flex;
  gap: 10px;
}

.role-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px 6px;
  border: 1px solid #d9e5d6;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.role-option svg {
  font-size: 20px;
  color: #9bbf8f;
}

.role-option span {
  font-size: 11px;
  color: #6b8c6c;
}

.role-option:hover {
  border-color: #9bcd9c;
  background: #f6fbf4;
}

.role-option.active {
  border-color: #7cb87a;
  background: #eef6ea;
}

.role-option.active svg {
  color: #5a9e5b;
}

.role-option.active span {
  color: #5a9e5b;
  font-weight: 500;
}

/* 输入框 - 更小更圆润 */
.input-wrapper {
  position: relative;
}

.input-wrapper input {
  width: 100%;
  padding: 10px 36px;
  border: 1px solid #e2ecd9;
  border-radius: 28px;
  font-size: 13px;
  transition: all 0.3s;
  outline: none;
  background: #fefef8;
}

.input-wrapper input:focus {
  border-color: #9bcd9c;
  box-shadow: 0 0 0 3px rgba(155, 205, 156, 0.15);
  background: white;
}

.input-wrapper input.input-error {
  border-color: #e6a0a0;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: #b8d0ae;
}

.password-icon {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: #b8d0ae;
  cursor: pointer;
}

.password-icon:hover {
  color: #7cb87a;
}

.error-message {
  font-size: 11px;
  color: #e6a0a0;
  margin-left: 12px;
}

/* 登录按钮 - 治愈系颜色 */
.login-btn {
  background: linear-gradient(135deg, #9bcd9c 0%, #7cb87a 100%);
  color: white;
  padding: 10px;
  border: none;
  border-radius: 32px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 8px;
  letter-spacing: 1px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(124, 184, 122, 0.3);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 注册链接 */
.signup-link {
  text-align: center;
  font-size: 12px;
  color: #8ba888;
}

.signup-link a {
  color: #7cb87a;
  text-decoration: none;
  font-weight: 500;
}

.signup-link a:hover {
  text-decoration: underline;
  color: #5a9e5b;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    border-radius: 28px;
  }
  
  .login-left {
    padding: 28px;
  }
  
  .login-right {
    padding: 28px;
  }
  
  .role-selector {
    gap: 8px;
  }
  
  .role-option {
    padding: 6px 4px;
  }
  
  .role-option svg {
    font-size: 18px;
  }
  
  .form-container {
    max-width: 100%;
  }
}
</style>