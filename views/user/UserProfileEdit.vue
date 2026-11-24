<template>
  <div class="user-profile-edit-page">
    <div class="edit-container">
      <div class="edit-header">
        <a-button type="text" @click="goBack">
          <icon-left /> 返回
        </a-button>
        <h2>编辑资料</h2>
        <div style="width: 60px"></div>
      </div>

      <a-spin :loading="loading" class="loading-wrapper">
        <a-form :model="formData" layout="vertical" class="edit-form">
          <!-- 头像 -->
          <a-form-item label="头像">
            <div class="avatar-section">
              <a-avatar :size="80" :image-url="formData.avatar">
                {{ getAvatarInitial() }}
              </a-avatar>
              <a-button type="outline" size="small" :loading="avatarUploading" @click="handleChangeAvatar">
                更换头像
              </a-button>
            </div>
          </a-form-item>

          <!-- 用户名/真实姓名 -->
          <a-form-item label="真实姓名" field="realName" required>
            <a-input v-model="formData.realName" placeholder="请输入真实姓名" />
          </a-form-item>

          <!-- 昵称 -->
          <a-form-item label="昵称" field="nickname">
            <a-input v-model="formData.nickname" placeholder="请输入昵称" />
          </a-form-item>

          <!-- 性别 -->
          <a-form-item label="性别" field="gender">
            <a-radio-group v-model="formData.gender">
              <a-radio value="男">男</a-radio>
              <a-radio value="女">女</a-radio>
              <a-radio value="保密">保密</a-radio>
            </a-radio-group>
          </a-form-item>

          <!-- 手机号 -->
          <a-form-item label="手机号" field="phone">
            <a-input v-model="formData.phone" placeholder="请输入手机号" />
          </a-form-item>

          <!-- 邮箱 -->
          <a-form-item label="邮箱" field="email">
            <a-input v-model="formData.email" placeholder="请输入邮箱" />
          </a-form-item>

          <!-- 密码（留空则不修改） -->
          <a-form-item label="密码" field="password">
            <a-input-password v-model="formData.password" placeholder="留空则不修改密码" />
          </a-form-item>

          <!-- 个人简介 -->
          <a-form-item label="个人简介" field="bio">
            <a-textarea 
              v-model="formData.bio" 
              placeholder="介绍一下自己吧"
              :rows="4"
              :max-length="200"
              show-word-limit
            />
          </a-form-item>

          <!-- 最新测评结果 -->
          <a-form-item label="📊 最新测评结果">
            <div class="test-results">
              <div v-if="latestTestsLoading" class="test-loading">
                <a-spin /> 加载中...
              </div>
              <div v-else-if="latestTests.length === 0" class="no-test">
                暂无测评记录，<a-link @click="goToTest">去完成一个测评</a-link>
              </div>
              <div v-else class="test-list">
                <div 
                  v-for="test in latestTests" 
                  :key="test.id" 
                  class="test-item"
                  @click="viewTestDetail(test)"
                >
                  <div class="test-name">{{ test.name }}</div>
                  <div class="test-result" :class="getResultClass(test.result)">
                    {{ test.result }}
                  </div>
                  <div class="test-date">{{ test.date }}</div>
                </div>
              </div>
            </div>
          </a-form-item>

          <div class="edit-actions">
            <a-button class="cancel-btn" @click="goBack">取消</a-button>
            <a-button class="save-btn" type="primary" :loading="saving" @click="handleSave">保存修改</a-button>
          </div>
        </a-form>
      </a-spin>
    </div>

    <!-- 头像上传弹窗 -->
    <a-modal
      v-model:visible="avatarModalVisible"
      title="上传头像"
      @ok="handleConfirmAvatar"
      @cancel="handleCancelAvatar"
      :ok-text="'确定上传'"
      :cancel-text="'取消'"
    >
      <a-upload
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        :before-upload="beforeUpload"
        @success="handleUploadSuccess"
        @error="handleUploadError"
        :disabled="avatarUploading"
      >
        <template #upload-button>
          <div class="avatar-upload-area">
            <div v-if="tempAvatarUrl" class="avatar-preview">
              <a-avatar :size="100" :image-url="tempAvatarUrl" />
              <div class="preview-mask">点击重新选择</div>
            </div>
            <div v-else class="upload-placeholder">
              <icon-upload />
              <div class="upload-text">点击或拖拽上传头像</div>
              <div class="upload-tip">支持 JPG、PNG，大小不超过 2MB</div>
            </div>
          </div>
        </template>
      </a-upload>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/store/userStore'
import { Message } from '@arco-design/web-vue'
import { IconLeft, IconUpload } from '@arco-design/web-vue/es/icon'
import myAxios from '@/request'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const saving = ref(false)
const avatarUploading = ref(false)
const avatarModalVisible = ref(false)
const latestTestsLoading = ref(false)

// 头像上传相关
const tempAvatarUrl = ref('')
const uploadedAvatarUrl = ref('')
const uploadUrl = '/api/file/upload/avatar'
const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
})

// 表单数据（合并两个文件的所有字段）
const formData = reactive({
  id: '',
  avatar: '',
  realName: '',
  nickname: '',
  gender: '保密',
  email: '',
  phone: '',
  password: '',
  bio: ''
})

// 最新测评结果
interface TestResult {
  id: number
  name: string
  result: string
  date: string
  type: string
}

const latestTests = ref<TestResult[]>([])

/**
 * 获取头像首字母
 */
const getAvatarInitial = () => {
  const name = formData.realName || formData.nickname || 'U'
  return name.charAt(0).toUpperCase()
}

/**
 * 获取结果样式
 */
const getResultClass = (result: string) => {
  if (result.includes('焦虑') || result.includes('抑郁') || result.includes('压力')) return 'warning'
  if (result.includes('正常') || result.includes('良好')) return 'normal'
  if (result.includes('INTJ') || result.includes('型') || result.includes('性格')) return 'info'
  return ''
}

/**
 * 查看测评详情
 */
const viewTestDetail = (test: TestResult) => {
  router.push(`/answer/result/${test.id}`)
}

/**
 * 去测评
 */
const goToTest = () => {
  router.push('/test/list')
}

/**
 * 加载最新测评结果（真实API调用）
 */
const loadLatestTests = async () => {
  latestTestsLoading.value = true
  try {
    const res = await myAxios.get('/api/exam/record/latest', {
      params: { limit: 3 }
    })
    
    if (res.data.code === 20000 || res.data.code === 200) {
      const records = res.data.data || []
      latestTests.value = records.map((record: any) => ({
        id: record.id,
        name: record.scaleName || '心理测评',
        result: record.level || `${record.score}分`,
        date: record.createTime?.split('T')[0] || new Date().toLocaleDateString(),
        type: record.appType || ''
      }))
    }
  } catch (error) {
    console.error('加载测评记录失败:', error)
  } finally {
    latestTestsLoading.value = false
  }
}

/**
 * 加载用户信息（增强版，支持从API获取最新数据）
 */
const loadUserInfo = async () => {
  loading.value = true
  
  try {
    // 先从 API 获取最新用户信息
    const res = await myAxios.get('/api/user/current')
    
    if (res.data.code === 20000 || res.data.code === 200) {
      const user = res.data.data
      fillFormData(user)
    } else {
      // 降级：从 store 或 localStorage 获取
      let user = loginUserStore.loginUser
      
      if (!user || !user.id) {
        const localUser = localStorage.getItem('loginUser')
        if (localUser) {
          user = JSON.parse(localUser)
        }
      }
      
      if (user && user.id) {
        fillFormData(user)
      } else {
        Message.error('无法获取用户信息，请重新登录')
        setTimeout(() => {
          router.push('/user/login')
        }, 1000)
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    Message.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

/**
 * 填充表单数据
 */
const fillFormData = (user: any) => {
  formData.id = user.id
  formData.avatar = user.avatar || ''
  formData.realName = user.realName || user.userName || user.userAccount || ''
  formData.nickname = user.nickname || user.userName || ''
  formData.gender = user.gender || '保密'
  formData.email = user.email || ''
  formData.phone = user.phone || ''
  formData.bio = user.bio || user.userProfile || ''
  formData.password = '' // 清空密码字段
}

/**
 * 头像上传前校验
 */
const beforeUpload = (file: File) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    Message.error('只能上传 JPG 或 PNG 格式的图片')
    return false
  }
  
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    Message.error('图片大小不能超过 2MB')
    return false
  }
  
  avatarUploading.value = true
  return true
}

/**
 * 头像上传成功
 */
const handleUploadSuccess = (response: any) => {
  avatarUploading.value = false
  if (response.data && response.data.url) {
    uploadedAvatarUrl.value = response.data.url
    tempAvatarUrl.value = response.data.url
    Message.success('头像上传成功')
  } else {
    Message.error('上传失败：未获取到图片地址')
  }
}

/**
 * 头像上传失败
 */
const handleUploadError = (error: any) => {
  avatarUploading.value = false
  console.error('上传失败:', error)
  Message.error('上传失败，请重试')
}

/**
 * 确认更换头像
 */
const handleConfirmAvatar = () => {
  if (uploadedAvatarUrl.value) {
    formData.avatar = uploadedAvatarUrl.value
    avatarModalVisible.value = false
    tempAvatarUrl.value = ''
    uploadedAvatarUrl.value = ''
    Message.success('头像已更新')
  } else {
    Message.warning('请先上传图片')
  }
}

/**
 * 取消更换头像
 */
const handleCancelAvatar = () => {
  avatarModalVisible.value = false
  tempAvatarUrl.value = ''
  uploadedAvatarUrl.value = ''
}

/**
 * 更换头像
 */
const handleChangeAvatar = () => {
  avatarModalVisible.value = true
}

/**
 * 保存修改
 */
const handleSave = async () => {
  if (!formData.id) {
    Message.error('用户信息异常，请重新登录')
    setTimeout(() => {
      router.push('/user/login')
    }, 1000)
    return
  }
  
  saving.value = true
  try {
    const updateData: any = {
      id: formData.id,
      realName: formData.realName,
      nickname: formData.nickname,
      gender: formData.gender,
      email: formData.email,
      phone: formData.phone,
      bio: formData.bio,
      avatar: formData.avatar
    }
    
    // 只有填写了密码才更新密码
    if (formData.password && formData.password.trim()) {
      updateData.password = formData.password
    }
    
    console.log('发送的数据:', updateData)
    
    const res = await myAxios.post('/api/user/update', updateData)
    
    if (res.data.code === 20000 || res.data.code === 200) {
      Message.success('保存成功')
      
      // 更新 store 中的用户信息
      const storeUser = {
        ...loginUserStore.loginUser,
        id: formData.id,
        realName: formData.realName,
        userName: formData.realName,
        nickname: formData.nickname,
        gender: formData.gender,
        email: formData.email,
        phone: formData.phone,
        avatar: formData.avatar,
        bio: formData.bio,
        userProfile: formData.bio
      }
      loginUserStore.setLoginUser(storeUser)
      
      // 更新 localStorage 中的用户信息
      localStorage.setItem('loginUser', JSON.stringify(storeUser))
      
      setTimeout(() => {
        router.push('/user/profile')
      }, 500)
    } else {
      Message.error(res.data.message || '保存失败')
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    Message.error(error.message || '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

/**
 * 返回
 */
const goBack = () => {
  router.push('/user/profile')
}

onMounted(() => {
  loadUserInfo()
  loadLatestTests()
})
</script>

<style scoped lang="scss">
.user-profile-edit-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6f5 100%);
  padding: 32px 24px;
}

.edit-container {
  max-width: 800px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 32px;
  padding: 32px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
}

.loading-wrapper {
  display: block;
}

.edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8ecd9;
  
  :deep(.arco-btn-text) {
    color: #7cb87a;
    font-size: 14px;
    transition: all 0.3s;
    
    &:hover {
      color: #5a9e5b;
      background: #f6fbf4;
    }
  }
  
  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 500;
    color: #2c5f2d;
    letter-spacing: 1px;
  }
}

.edit-form {
  :deep(.arco-form-item-label) {
    font-weight: 500;
    color: #5a7c5b;
    font-size: 13px;
  }
  
  :deep(.arco-input-wrapper) {
    border-radius: 28px;
    border-color: #e2ecd9;
    transition: all 0.3s;
    
    &:hover {
      border-color: #9bcd9c;
    }
    
    &:focus-within {
      border-color: #9bcd9c;
      box-shadow: 0 0 0 3px rgba(155, 205, 156, 0.15);
    }
  }
  
  :deep(.arco-input),
  :deep(.arco-input-password),
  :deep(.arco-textarea) {
    background: #fefef8;
    font-size: 14px;
    
    &:focus {
      background: #ffffff;
    }
  }
  
  :deep(.arco-textarea-wrapper) {
    border-radius: 16px;
    border-color: #e2ecd9;
    
    &:focus-within {
      border-color: #9bcd9c;
      box-shadow: 0 0 0 3px rgba(155, 205, 156, 0.15);
    }
  }
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  
  :deep(.arco-avatar) {
    background: linear-gradient(135deg, #9bcd9c, #7cb87a);
    font-size: 28px;
    font-weight: 500;
  }
}

/* 头像上传弹窗样式 */
.avatar-upload-area {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  cursor: pointer;
}

.avatar-preview {
  position: relative;
  
  .preview-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    opacity: 0;
    transition: opacity 0.3s;
    cursor: pointer;
  }
  
  &:hover .preview-mask {
    opacity: 1;
  }
}

.upload-placeholder {
  text-align: center;
  padding: 40px;
  border: 2px dashed #e2ecd9;
  border-radius: 16px;
  transition: all 0.3s;
  
  &:hover {
    border-color: #9bcd9c;
    background: #f6fbf4;
  }
  
  .arco-icon {
    font-size: 48px;
    color: #9bcd9c;
  }
  
  .upload-text {
    margin-top: 12px;
    color: #4e5969;
  }
  
  .upload-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #86909c;
  }
}

/* 最新测评结果样式 */
.test-results {
  background: #f7f8fa;
  border-radius: 16px;
  padding: 16px;
}

.test-loading {
  text-align: center;
  padding: 20px;
  color: #86909c;
}

.no-test {
  text-align: center;
  padding: 20px;
  color: #86909c;
}

.test-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.test-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transform: translateX(4px);
  }
}

.test-name {
  flex: 1;
  font-weight: 500;
  color: #1d2129;
  font-size: 14px;
}

.test-result {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  
  &.warning {
    background: #fff7e6;
    color: #fa8c16;
  }
  
  &.normal {
    background: #f6ffed;
    color: #52c41a;
  }
  
  &.info {
    background: #e8f4ff;
    color: #3b82f6;
  }
}

.test-date {
  font-size: 12px;
  color: #86909c;
  min-width: 100px;
  text-align: right;
}

.edit-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 40px;
  
  .cancel-btn {
    border-radius: 32px;
    padding: 8px 28px;
    border-color: #9bcd9c;
    color: #7cb87a;
    background: transparent;
    transition: all 0.3s;
    
    &:hover {
      background: #f6fbf4;
      border-color: #7cb87a;
      color: #5a9e5b;
    }
  }
  
  .save-btn {
    border-radius: 32px;
    padding: 8px 28px;
    background: linear-gradient(135deg, #9bcd9c 0%, #7cb87a 100%);
    border: none;
    font-weight: 500;
    letter-spacing: 1px;
    transition: all 0.3s;
    
    &:hover {
      background: linear-gradient(135deg, #7cb87a 0%, #5a9e5b 100%);
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(124, 184, 122, 0.3);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

@media (max-width: 640px) {
  .user-profile-edit-page {
    padding: 16px;
  }
  
  .edit-container {
    padding: 20px;
    border-radius: 24px;
  }
  
  .edit-header h2 {
    font-size: 20px;
  }
  
  .edit-actions {
    margin-top: 32px;
    
    .cancel-btn,
    .save-btn {
      padding: 6px 20px;
    }
  }
  
  .test-item {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .test-date {
    min-width: auto;
    width: 100%;
    text-align: left;
  }
}
</style>