<template>
  <div class="teacher-profile">
    <!-- 左侧边栏 -->
    <div class="profile-sidebar">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-wrapper" @click="handleChangeAvatar">
          <a-avatar :size="100" :style="{ backgroundColor: '#3b82f6', fontSize: '40px' }">
            {{ userInfo.name?.charAt(0) || '张' }}
          </a-avatar>
          <div class="avatar-edit">
            📷  <!-- 替换 icon-camera -->
          </div>
        </div>
        <h3>{{ userInfo.name }}</h3>
        <p class="username">用户名：{{ userInfo.username }}</p>
        
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-value">{{ userInfo.completedTests }}</div>
            <div class="stat-label">完成测评</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userInfo.appointmentCount }}</div>
            <div class="stat-label">预约次数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userInfo.favorites }}</div>
            <div class="stat-label">收藏内容</div>
          </div>
        </div>
      </div>

      <!-- 联系信息 -->
      <div class="info-section">
        <div class="info-item">
          📞  <!-- 替换 icon-phone -->
          <div class="info-content">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ userInfo.phone || '未绑定' }}</span>
          </div>
          <a-button type="text" size="small" @click="handleEditPhone">更换</a-button>
        </div>
        <div class="info-item">
          📧  <!-- 替换 icon-mail -->
          <div class="info-content">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo.email || '未绑定' }}</span>
          </div>
          <a-button type="text" size="small" @click="handleEditEmail">更换</a-button>
        </div>
        <div class="info-item">
          📅  <!-- 替换 icon-calendar -->
          <div class="info-content">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ userInfo.registerTime }}</span>
          </div>
        </div>
      </div>

      <!-- 编辑资料按钮 -->
      <a-button type="outline" long class="edit-btn" @click="openEditModal">
        ✏️ 编辑资料  <!-- 替换 icon-edit -->
      </a-button>
    </div>

    <!-- 右侧内容 -->
    <div class="profile-content">
      <!-- 最近测评 -->
      <a-card :bordered="false" class="section-card">
        <template #title>
          <div class="card-header">
            <span>最近测评</span>
            <a-link @click="goToTests">查看全部 →</a-link>
          </div>
        </template>
        <div v-if="recentTests.length > 0" class="test-list">
          <div v-for="test in recentTests" :key="test.id" class="test-item">
            <div class="test-name">{{ test.name }}</div>
            <div class="test-result">{{ test.result }}</div>
            <div class="test-date">{{ test.date }}</div>
          </div>
        </div>
        <div v-else class="empty-state">
          📄  <!-- 替换 icon-file -->
          <p>暂无测评记录</p>
          <a-button type="primary" size="small" @click="goToTests">去完成一个测评</a-button>
        </div>
      </a-card>

      <!-- 最近预约 -->
      <a-card :bordered="false" class="section-card">
        <template #title>
          <div class="card-header">
            <span>最近预约</span>
            <a-link @click="goToAppointments">查看全部 →</a-link>
          </div>
        </template>
        <div v-if="recentAppointments.length > 0" class="appointment-list">
          <div v-for="apt in recentAppointments" :key="apt.id" class="appointment-item">
            <div class="appointment-info">
              <span class="counselor">{{ apt.counselorName }}</span>
              <a-tag :color="apt.status === 'confirmed' ? 'green' : 'orange'" size="small">
                {{ apt.status === 'confirmed' ? '已确认' : '待确认' }}
              </a-tag>
            </div>
            <div class="appointment-time">{{ apt.date }} {{ apt.timeSlot }}</div>
          </div>
        </div>
        <div v-else class="empty-state">
          📅  <!-- 替换 icon-calendar -->
          <p>暂无预约记录</p>
          <a-button type="primary" size="small" @click="goToAppointments">去预约</a-button>
        </div>
      </a-card>

      <!-- 紧急联系人 -->
      <a-card :bordered="false" class="section-card emergency-card">
        <template #title>
          <div class="card-header">
            <span>紧急联系人</span>
            <a-link @click="goToEmergency">去设置 →</a-link>
          </div>
        </template>
        <div v-if="emergencyContact.name" class="emergency-info">
          <div class="emergency-name">
            👤  <!-- 替换 icon-user -->
            <span>{{ emergencyContact.name }}（{{ emergencyContact.relation }}）</span>
          </div>
          <div class="emergency-phone">
            📞  <!-- 替换 icon-phone -->
            <span>{{ emergencyContact.phone }}</span>
          </div>
        </div>
        <div v-else class="empty-state">
          🔒  <!-- 替换 icon-safe -->
          <p>紧急联系人设置</p>
          <span class="empty-tip">添加紧急联系人，在需要时能快速获得帮助</span>
          <a-button type="primary" size="small" @click="openEmergencyModal">去设置</a-button>
        </div>
      </a-card>
    </div>

    <!-- 弹窗部分保持不变，但更换头像弹窗中的 icon-plus 也需要替换 -->
    <a-modal v-model:visible="avatarModalVisible" title="更换头像" @ok="confirmUploadAvatar" @cancel="avatarModalVisible = false">
      <a-upload
        :action="uploadUrl"
        :show-file-list="false"
        :before-upload="beforeUpload"
        @success="handleUploadSuccess"
        @error="handleUploadError"
      >
        <div class="upload-area">
          ➕  <!-- 替换 icon-plus -->
          <div>点击或拖拽上传图片</div>
          <div class="upload-tip">支持 JPG、PNG 格式，不超过 5MB</div>
        </div>
      </a-upload>
      <div class="preview-area" v-if="tempAvatar">
        <h4>预览：</h4>
        <a-avatar :size="80" :image-url="tempAvatar" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'


const router = useRouter()

// 用户信息
const userInfo = ref({
  name: '张老师',
  username: 'teacher',
  phone: '13800138000',
  email: '',
  registerTime: '2025-01-01',
  completedTests: 3,
  appointmentCount: 5,
  favorites: 3,
  gender: '男',
  bio: ''
})

// 最近测评
const recentTests = ref<any[]>([])

// 最近预约
const recentAppointments = ref([
  {
    id: 1,
    counselorName: '张老师',
    status: 'confirmed',
    date: '2025-03-28',
    timeSlot: '10:30-11:30'
  }
])

// 紧急联系人
const emergencyContact = ref({
  name: '',
  relation: '',
  phone: ''
})

// 弹窗状态
const editModalVisible = ref(false)
const phoneModalVisible = ref(false)
const emailModalVisible = ref(false)
const emergencyModalVisible = ref(false)
const avatarModalVisible = ref(false)

// 验证码相关
const captchaSending = ref(false)
const captchaText = ref('获取验证码')
const emailCaptchaSending = ref(false)
const emailCaptchaText = ref('获取验证码')

// 表单数据
const editForm = ref({
  name: '',
  gender: '',
  bio: ''
})

const phoneForm = ref({
  phone: '',
  captcha: ''
})

const emailForm = ref({
  email: '',
  captcha: ''
})

const emergencyForm = ref({
  name: '',
  relation: '',
  phone: ''
})

// 头像上传
const tempAvatar = ref('')
const uploadUrl = '/api/teacher/upload-avatar'

// 编辑资料
const openEditModal = () => {
  editForm.value = {
    name: userInfo.value.name,
    gender: userInfo.value.gender,
    bio: userInfo.value.bio
  }
  editModalVisible.value = true
}

const saveProfile = () => {
  if (!editForm.value.name) {
    Message.warning('请输入姓名')
    return
  }
  userInfo.value.name = editForm.value.name
  userInfo.value.gender = editForm.value.gender
  userInfo.value.bio = editForm.value.bio
  Message.success('保存成功')
  editModalVisible.value = false
}

// 更换手机号
const handleEditPhone = () => {
  phoneForm.value = { phone: '', captcha: '' }
  phoneModalVisible.value = true
}

const sendCaptcha = async () => {
  if (!phoneForm.value.phone) {
    Message.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phoneForm.value.phone)) {
    Message.warning('请输入正确的手机号')
    return
  }
  captchaSending.value = true
  try {
    // TODO: 调用发送验证码API
    await new Promise(resolve => setTimeout(resolve, 1000))
    Message.success('验证码已发送')
    let countdown = 60
    const timer = setInterval(() => {
      countdown--
      if (countdown <= 0) {
        clearInterval(timer)
        captchaText.value = '获取验证码'
        captchaSending.value = false
      } else {
        captchaText.value = `${countdown}秒后重试`
      }
    }, 1000)
  } catch (error) {
    Message.error('发送失败')
    captchaSending.value = false
  }
}

const savePhone = () => {
  if (!phoneForm.value.phone || !phoneForm.value.captcha) {
    Message.warning('请填写完整信息')
    return
  }
  userInfo.value.phone = phoneForm.value.phone
  Message.success('手机号更换成功')
  phoneModalVisible.value = false
}

// 更换邮箱
const handleEditEmail = () => {
  emailForm.value = { email: '', captcha: '' }
  emailModalVisible.value = true
}

const sendEmailCaptcha = async () => {
  if (!emailForm.value.email) {
    Message.warning('请输入邮箱')
    return
  }
  if (!/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(emailForm.value.email)) {
    Message.warning('请输入正确的邮箱')
    return
  }
  emailCaptchaSending.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    Message.success('验证码已发送')
    let countdown = 60
    const timer = setInterval(() => {
      countdown--
      if (countdown <= 0) {
        clearInterval(timer)
        emailCaptchaText.value = '获取验证码'
        emailCaptchaSending.value = false
      } else {
        emailCaptchaText.value = `${countdown}秒后重试`
      }
    }, 1000)
  } catch (error) {
    Message.error('发送失败')
    emailCaptchaSending.value = false
  }
}

const saveEmail = () => {
  if (!emailForm.value.email || !emailForm.value.captcha) {
    Message.warning('请填写完整信息')
    return
  }
  userInfo.value.email = emailForm.value.email
  Message.success('邮箱更换成功')
  emailModalVisible.value = false
}

// 紧急联系人
const goToEmergency = () => {
  emergencyModalVisible.value = true
}

const openEmergencyModal = () => {
  emergencyModalVisible.value = true
}

const saveEmergency = () => {
  if (!emergencyForm.value.name || !emergencyForm.value.relation || !emergencyForm.value.phone) {
    Message.warning('请填写完整信息')
    return
  }
  emergencyContact.value = { ...emergencyForm.value }
  Message.success('紧急联系人保存成功')
  emergencyModalVisible.value = false
}

// 头像上传
const handleChangeAvatar = () => {
  avatarModalVisible.value = true
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    Message.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    Message.error('图片不能超过5MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    tempAvatar.value = response.data.url
    Message.success('上传成功')
  }
}

const handleUploadError = () => {
  Message.error('上传失败')
}

const confirmUploadAvatar = () => {
  if (tempAvatar.value) {
    // TODO: 保存头像URL
    Message.success('头像已更新')
  }
  avatarModalVisible.value = false
}

// 页面跳转
const goToTests = () => {
  router.push('/test/list')
}

const goToAppointments = () => {
  router.push('/booking/list')
}

// 初始化
onMounted(() => {
  // TODO: 加载用户数据
})
</script>

<style scoped>
.teacher-profile {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: #f0f2f6;
  min-height: 100vh;
}

/* 左侧边栏 */
.profile-sidebar {
  width: 320px;
  flex-shrink: 0;
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.avatar-section {
  text-align: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e6eb;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.avatar-section h3 {
  margin: 12px 0 4px;
  font-size: 18px;
}

.username {
  color: #86909c;
  font-size: 13px;
  margin-bottom: 16px;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  background: #f7f8fa;
  border-radius: 12px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.stat-label {
  font-size: 12px;
  color: #86909c;
  margin-top: 4px;
}

.info-section {
  padding: 16px 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item .info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 12px;
  color: #86909c;
}

.info-value {
  font-size: 14px;
  color: #1e293b;
}

.edit-btn {
  margin-top: 16px;
}

/* 右侧内容 */
.profile-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  border-radius: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #86909c;
}

.empty-state p {
  margin: 12px 0;
}

.empty-tip {
  display: block;
  font-size: 12px;
  color: #c9cdd4;
  margin: 8px 0;
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
  padding: 12px;
  background: #f7f8fa;
  border-radius: 12px;
}

.test-name {
  font-size: 14px;
  font-weight: 500;
}

.test-result {
  font-size: 13px;
  color: #3b82f6;
}

.test-date {
  font-size: 12px;
  color: #86909c;
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.appointment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 12px;
}

.appointment-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.counselor {
  font-weight: 500;
}

.appointment-time {
  font-size: 13px;
  color: #86909c;
}

.emergency-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 12px;
}

.emergency-name, .emergency-phone {
  display: flex;
  align-items: center;
  gap: 8px;
}

.captcha-wrapper {
  display: flex;
  gap: 12px;
}

.captcha-wrapper .a-input {
  flex: 1;
}

.upload-area {
  border: 2px dashed #e5e6eb;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #3b82f6;
  background: #f7f8fa;
}

.upload-tip {
  font-size: 12px;
  color: #86909c;
  margin-top: 8px;
}

.preview-area {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 800px) {
  .teacher-profile {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
}
</style>