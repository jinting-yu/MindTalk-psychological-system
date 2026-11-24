<template>
  <div class="register-container">
    <div class="register-card">
      <!-- 左侧装饰区域 -->
      <div class="register-left">
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
      <div class="register-right">
        <div class="form-container">
          <h1 class="form-title">注册</h1>
          <p class="form-subtitle">创建新账号，开始心理探索之旅</p>

          <form @submit.prevent="handleRegister" class="register-form">
            <!-- 账号输入框 -->
            <div class="form-group">
              <label for="userAccount">账号</label>
              <div class="input-wrapper">
                <input
                  id="userAccount"
                  v-model="formData.userAccount"
                  type="text"
                  placeholder="请输入账号（字母、数字）"
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
                  placeholder="请输入密码（至少6位）"
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

            <!-- 确认密码输入框 -->
            <div class="form-group">
              <label for="checkPassword">确认密码</label>
              <div class="input-wrapper">
                <input
                  id="checkPassword"
                  v-model="formData.checkPassword"
                  :type="showCheckPassword ? 'text' : 'password'"
                  placeholder="请再次输入密码"
                  required
                  :class="{ 'input-error': errors.checkPassword }"
                />
                <icon-lock class="input-icon" />
                <icon-eye 
                  v-if="showCheckPassword" 
                  class="password-icon"
                  @click="showCheckPassword = false"
                />
                <icon-eye-invisible 
                  v-else 
                  class="password-icon"
                  @click="showCheckPassword = true"
                />
              </div>
              <span v-if="errors.checkPassword" class="error-message">{{ errors.checkPassword }}</span>
            </div>

            <!-- 角色选择 -->
            <div class="form-group">
              <label>注册身份</label>
              <div class="role-group">
                <label class="role-option">
                  <input type="radio" v-model="formData.role" value="USER" />
                  <span>普通用户</span>
                </label>
                <label class="role-option">
                  <input type="radio" v-model="formData.role" value="TEACHER" />
                  <span>心理老师</span>
                </label>
                <label class="role-option">
                  <input type="radio" v-model="formData.role" value="ADMIN" />
                  <span>管理者</span>
                </label>
              </div>
            </div>

            <!-- 注册按钮 -->
            <button type="submit" class="register-btn" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else>注册</span>
            </button>

            <!-- 登录链接 -->
            <div class="login-link">
              已有账号？
              <a href="#" @click.prevent="handleLogin">立即登录</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import myAxios from '@/request'
import {
  IconUser,
  IconLock,
  IconEye,
  IconEyeInvisible,
  IconHeart,
  IconCheckCircle,
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const showCheckPassword = ref(false)

// 表单数据
const formData = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  role: 'USER'
})

// 错误信息
const errors = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

/**
 * 验证表单
 */
const validateForm = () => {
  let isValid = true
  errors.userAccount = ''
  errors.userPassword = ''
  errors.checkPassword = ''

  if (!formData.userAccount) {
    errors.userAccount = '请输入账号'
    isValid = false
  } else if (!/^[a-zA-Z0-9]{4,16}$/.test(formData.userAccount)) {
    errors.userAccount = '账号必须是4-16位字母或数字'
    isValid = false
  }

  if (!formData.userPassword) {
    errors.userPassword = '请输入密码'
    isValid = false
  } else if (formData.userPassword.length < 6) {
    errors.userPassword = '密码长度至少6位'
    isValid = false
  }

  if (!formData.checkPassword) {
    errors.checkPassword = '请确认密码'
    isValid = false
  } else if (formData.userPassword !== formData.checkPassword) {
    errors.checkPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

/**
 * 注册处理 - 使用 myAxios
 */
const handleRegister = async () => {
  console.log('========== 注册按钮被点击 ==========')
  console.log('账号:', formData.userAccount)
  console.log('角色:', formData.role)
  
  if (!validateForm()) {
    console.log('表单验证失败')
    return
  }
  
  loading.value = true
  
  try {
    const requestData = {
      username: formData.userAccount,
      password: formData.userPassword,
      role: formData.role,
      realName: "",
      phone: ""
    }
    
    console.log('发送注册请求:', { ...requestData, password: '***' })
    
    // 使用 myAxios 发送请求
    const response = await myAxios.post('/api/user/register', requestData)
    
    console.log('注册响应:', response.data)
    
    // 判断注册成功
    if (response.data.code === 200 || response.data.code === 20000) {
      Message.success('注册成功！请登录')
      setTimeout(() => {
        router.push('/user/login')
      }, 1000)
    } else {
      Message.error(response.data.msg || response.data.message || '注册失败')
    }
  } catch (error: any) {
    console.error('注册错误:', error)
    
    if (error.response?.status === 431) {
      Message.error('请求头过大，请清除浏览器缓存后重试')
    } else if (error.response?.status === 500) {
      console.error('服务器错误详情:', error.response.data)
      Message.error(error.response.data?.message || '服务器错误，请稍后重试')
    } else if (error.message?.includes('Network Error') || error.message?.includes('Failed to fetch')) {
      Message.error('网络连接失败，请检查后端服务是否启动 (端口 8080 或 8081)')
    } else {
      Message.error(error.response?.data?.message || error.message || '注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
    console.log('========== 注册流程结束 ==========')
  }
}

/**
 * 跳转到登录页
 */
const handleLogin = () => {
  router.push('/user/login')
}

onMounted(() => {
  console.log('注册页面已加载')
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6f5 100%);
  padding: 20px;
}

.register-card {
  display: flex;
  width: 950px;
  max-width: 100%;
  background: white;
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
}

/* 左侧装饰区域 */
.register-left {
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
.register-right {
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

.register-form {
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

/* 角色选择 */
.role-group {
  display: flex;
  gap: 20px;
  padding: 8px 0;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #5a7c5b;
}

.role-option input {
  cursor: pointer;
}

/* 输入框 */
.input-wrapper {
  position: relative;
  width: 100%;
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
  box-sizing: border-box;
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

/* 注册按钮 */
.register-btn {
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
  width: 100%;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(124, 184, 122, 0.3);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 登录链接 */
.login-link {
  text-align: center;
  font-size: 12px;
  color: #8ba888;
}

.login-link a {
  color: #7cb87a;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
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
  .register-card {
    flex-direction: column;
    border-radius: 28px;
  }
  
  .register-left {
    padding: 28px;
  }
  
  .register-right {
    padding: 28px;
  }
  
  .form-container {
    max-width: 100%;
  }
}
</style>