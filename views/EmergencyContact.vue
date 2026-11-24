<template>
  <div class="emergency-contact-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-button class="back-btn" @click="goBack">
        <icon-left /> 返回
      </a-button>
    </div>

    <div class="header">
      <h2>📞 紧急联系人</h2>
      <a-button type="primary" @click="showAddModal = true">
        <icon-plus /> 添加联系人
      </a-button>
    </div>

    <!-- 紧急提示卡片 -->
    <div class="emergency-tip" v-if="primaryContact">
      <div class="tip-icon">🆘</div>
      <div class="tip-content">
        <h3>需要帮助吗？</h3>
        <p>如果感到非常不适，可以立即联系你的主要紧急联系人</p>
        <div class="emergency-buttons">
          <a-button type="primary" danger size="large" @click="callEmergency">
            📞 立即联系 {{ primaryContact.name }}
          </a-button>
          <a-button size="large" @click="sendSOS">
            ✉️ 发送SOS短信
          </a-button>
        </div>
      </div>
    </div>

    <!-- 联系人列表 -->
    <div class="contact-list">
      <div 
        v-for="contact in contacts" 
        :key="contact.id" 
        class="contact-card" 
        :class="{ primary: contact.isPrimary === 1 }"
      >
        <div class="contact-avatar">
          <a-avatar :size="48" :style="{ backgroundColor: getAvatarColor(contact.relationship) }">
            {{ contact.name?.charAt(0) || '?' }}
          </a-avatar>
        </div>
        
        <div class="contact-info">
          <div class="contact-name">
            {{ contact.name }}
            <a-tag v-if="contact.isPrimary === 1" color="red" size="small">主要联系人</a-tag>
            <a-tag v-if="contact.relationship" :color="getRelationColor(contact.relationship)" size="small">
              {{ contact.relationship }}
            </a-tag>
          </div>
          <div class="contact-detail">
            <div class="contact-phone">
              <icon-phone /> {{ formatPhone(contact.phone) }}
            </div>
          </div>
        </div>
        
        <div class="contact-actions">
          <a-button type="outline" size="small" @click="makeCall(contact.phone)">
            📞 拨打
          </a-button>
          <a-button type="text" size="small" @click="editContact(contact)">
            ✏️ 编辑
          </a-button>
          <a-popconfirm content="确定删除该联系人吗？" @ok="deleteContact(contact.id)">
            <a-button type="text" size="small" status="danger">
              🗑️ 删除
            </a-button>
          </a-popconfirm>
          <a-button 
            v-if="contact.isPrimary !== 1" 
            type="text" 
            size="small" 
            @click="setPrimary(contact.id)"
          >
            ⭐ 设为主要
          </a-button>
        </div>
      </div>
      
      <div v-if="contacts.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无紧急联系人</p>
        <p class="empty-tip">添加一位紧急联系人，在你需要时能快速获得帮助</p>
        <a-button type="primary" @click="showAddModal = true">
          <icon-plus /> 添加联系人
        </a-button>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <a-modal 
      v-model:visible="showAddModal" 
      :title="editMode ? '编辑联系人' : '添加联系人'" 
      @ok="saveContact" 
      @cancel="cancelEdit"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="姓名" required>
          <a-input v-model="formData.name" placeholder="请输入联系人姓名" />
        </a-form-item>
        <a-form-item label="联系电话" required>
          <a-input v-model="formData.phone" placeholder="请输入手机号码" />
        </a-form-item>
        <a-form-item label="关系">
          <a-select v-model="formData.relationship" placeholder="请选择关系" allow-clear>
            <a-option value="父亲">👨 父亲</a-option>
            <a-option value="母亲">👩 母亲</a-option>
            <a-option value="配偶">💑 配偶</a-option>
            <a-option value="兄弟姐妹">👥 兄弟姐妹</a-option>
            <a-option value="朋友">👫 朋友</a-option>
            <a-option value="老师">👨‍🏫 老师</a-option>
            <a-option value="辅导员">📚 辅导员</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-checkbox v-model="isPrimaryCheckbox">⭐ 设为主要联系人</a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- SOS弹窗 -->
    <a-modal 
      v-model:visible="showSOSModal" 
      title="🆘 发送SOS求助" 
      @ok="sendSOSConfirm" 
      ok-text="发送" 
      cancel-text="取消"
    >
      <p>你即将向 <strong>{{ primaryContact?.name }}</strong> 发送以下求助短信：</p>
      <div class="sos-message">
        {{ sosMessage }}
      </div>
      <p class="sos-tip">⚠️ 请仅在真正需要帮助时使用此功能</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import { IconLeft, IconPlus, IconPhone } from '@arco-design/web-vue/es/icon'

const router = useRouter()

interface EmergencyContact {
  id?: number
  name: string
  phone: string
  relationship: string
  isPrimary: number
}

const contacts = ref<EmergencyContact[]>([])
const primaryContact = ref<EmergencyContact | null>(null)
const showAddModal = ref(false)
const showSOSModal = ref(false)
const editMode = ref(false)
const currentEditId = ref<number | null>(null)

const formData = ref({
  name: '',
  phone: '',
  relationship: '',
  isPrimary: 0
})

// 用于 checkbox 的计算属性
const isPrimaryCheckbox = computed({
  get: () => formData.value.isPrimary === 1,
  set: (val: boolean) => {
    formData.value.isPrimary = val ? 1 : 0
  }
})

// 获取头像颜色
const getAvatarColor = (relationship: string) => {
  const colors: Record<string, string> = {
    '父亲': '#165dff',
    '母亲': '#f53f3f',
    '配偶': '#ff7d00',
    '兄弟姐妹': '#00b42a',
    '朋友': '#14c9c9',
    '老师': '#722ed1',
    '辅导员': '#eb2f96'
  }
  return colors[relationship] || '#7eb09e'
}

// 获取关系标签颜色
const getRelationColor = (relationship: string) => {
  const colors: Record<string, string> = {
    '父亲': 'blue',
    '母亲': 'pink',
    '配偶': 'orange',
    '兄弟姐妹': 'green',
    '朋友': 'cyan',
    '老师': 'purple',
    '辅导员': 'magenta'
  }
  return colors[relationship] || 'gray'
}

// 格式化手机号（中间四位隐藏）
const formatPhone = (phone: string) => {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const sosMessage = computed(() => {
  const userName = localStorage.getItem('userName') || '我'
  return `【心理求助】${userName}正在经历心理困扰，需要你的帮助和支持。请尽快联系${userName}`
})

const API_BASE_URL = 'http://localhost:8081'

// 获取请求配置
const getRequestConfig = () => {
  const token = localStorage.getItem('token')
  return {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 获取联系人列表
const fetchContacts = async () => {
  try {
    const config = getRequestConfig()
    const response = await fetch(`${API_BASE_URL}/api/emergency/list`, {
      method: 'GET',
      headers: config.headers
    })
    
    const responseText = await response.text()
    if (!responseText || responseText.trim() === '') {
      throw new Error('服务器返回空响应')
    }
    
    const data = JSON.parse(responseText)
    
    if (data.code === 200) {
      contacts.value = data.data || []
      primaryContact.value = contacts.value.find(c => c.isPrimary === 1) || null
    } else if (data.code === 500 && data.msg === '未登录') {
      Message.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('loginUser')
      router.push('/user/login')
    } else if (data.code === 401) {
      Message.error('请先登录')
      router.push('/user/login')
    } else {
      Message.error(data.msg || '获取联系人失败')
    }
  } catch (error: any) {
    console.error('获取联系人失败:', error)
    Message.error('获取联系人失败: ' + (error.message || '请检查网络连接'))
  }
}

// 保存联系人
const saveContact = async () => {
  if (!formData.value.name || !formData.value.phone) {
    Message.warning('请填写姓名和电话')
    return
  }
  
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(formData.value.phone)) {
    Message.warning('请输入正确的手机号码')
    return
  }
  
  const requestData = {
    name: formData.value.name,
    phone: formData.value.phone,
    relationship: formData.value.relationship || '朋友',
    isPrimary: formData.value.isPrimary
  }
  
  if (editMode.value && currentEditId.value) {
    Object.assign(requestData, { id: currentEditId.value })
  }
  
  try {
    const config = getRequestConfig()
    const url = editMode.value && currentEditId.value 
      ? `${API_BASE_URL}/api/emergency/update`
      : `${API_BASE_URL}/api/emergency/add`
    
    const method = editMode.value && currentEditId.value ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method: method,
      headers: config.headers,
      body: JSON.stringify(requestData)
    })
    
    const responseText = await response.text()
    if (!responseText || responseText.trim() === '') {
      throw new Error('服务器返回空响应')
    }
    
    const data = JSON.parse(responseText)
    
    if (data.code === 200) {
      Message.success(editMode.value ? '更新成功' : '添加成功')
      showAddModal.value = false
      await fetchContacts()
      resetForm()
    } else {
      Message.error(data.msg || '操作失败')
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    Message.error('操作失败: ' + (error.message || '网络错误'))
  }
}

// 编辑联系人
const editContact = (contact: EmergencyContact) => {
  editMode.value = true
  currentEditId.value = contact.id || null
  formData.value = {
    name: contact.name,
    phone: contact.phone,
    relationship: contact.relationship || '',
    isPrimary: contact.isPrimary === 1 ? 1 : 0
  }
  showAddModal.value = true
}

// 删除联系人
const deleteContact = async (id: number | undefined) => {
  if (!id) return
  
  try {
    const config = getRequestConfig()
    const response = await fetch(`${API_BASE_URL}/api/emergency/delete/${id}`, {
      method: 'DELETE',
      headers: config.headers
    })
    
    const responseText = await response.text()
    const data = JSON.parse(responseText)
    
    if (data.code === 200) {
      Message.success('删除成功')
      await fetchContacts()
    } else {
      Message.error(data.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    Message.error('删除失败')
  }
}

// 设为主要联系人
const setPrimary = async (id: number | undefined) => {
  if (!id) return
  
  try {
    const config = getRequestConfig()
    const response = await fetch(`${API_BASE_URL}/api/emergency/setPrimary/${id}`, {
      method: 'PUT',
      headers: config.headers
    })
    
    const responseText = await response.text()
    const data = JSON.parse(responseText)
    
    if (data.code === 200) {
      Message.success('设置成功')
      await fetchContacts()
    } else {
      Message.error(data.msg || '设置失败')
    }
  } catch (error) {
    console.error('设置失败:', error)
    Message.error('设置失败')
  }
}

// 拨打电话
const makeCall = (phone: string) => {
  window.location.href = `tel:${phone}`
}

// 紧急呼叫
const callEmergency = () => {
  if (primaryContact.value) {
    makeCall(primaryContact.value.phone)
  } else {
    Message.warning('请先设置主要联系人')
  }
}

// 发送SOS
const sendSOS = () => {
  if (!primaryContact.value) {
    Message.warning('请先设置主要联系人')
    return
  }
  showSOSModal.value = true
}

const sendSOSConfirm = () => {
  Message.success('SOS求助已发送，请保持电话畅通')
  showSOSModal.value = false
}

const resetForm = () => {
  formData.value = {
    name: '',
    phone: '',
    relationship: '',
    isPrimary: 0
  }
  editMode.value = false
  currentEditId.value = null
}

const cancelEdit = () => {
  resetForm()
  showAddModal.value = false
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    Message.warning('请先登录')
    router.push('/user/login')
    return
  }
  fetchContacts()
})
</script>

<style scoped>
.emergency-contact-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 返回按钮样式 */
.page-header {
  margin-bottom: 16px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 28px;
  border-color: #9bcd9c;
  color: #7cb87a;
  background: white;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #f6fbf4;
  border-color: #7cb87a;
  color: #5a9e5b;
  transform: translateX(-2px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h2 {
  margin: 0;
  font-size: 20px;
  color: #1d2129;
}

/* 紧急提示卡片 */
.emergency-tip {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border-radius: 16px;
  padding: 20px;
  color: white;
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.tip-icon {
  font-size: 48px;
}

.tip-content h3 {
  margin: 0 0 8px 0;
}

.tip-content p {
  margin: 0 0 16px 0;
  opacity: 0.9;
}

.emergency-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 联系人列表 */
.contact-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-card {
  background: white;
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e8ecd9;
  transition: all 0.3s;
}

.contact-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.contact-card.primary {
  border-left: 4px solid #f53f3f;
  background: #fff2f0;
}

.contact-avatar {
  flex-shrink: 0;
}

.contact-info {
  flex: 1;
}

.contact-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.contact-detail {
  color: #86909c;
  font-size: 13px;
}

.contact-phone {
  display: flex;
  align-items: center;
  gap: 6px;
}

.contact-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
  color: #86909c;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-tip {
  font-size: 12px;
  margin: 8px 0 20px;
  color: #a9aeb8;
}

/* SOS消息 */
.sos-message {
  background: #f7f8fa;
  padding: 16px;
  border-radius: 12px;
  margin: 16px 0;
  color: #1d2129;
  line-height: 1.6;
}

.sos-tip {
  font-size: 12px;
  color: #f53f3f;
  margin-top: 8px;
}

/* 响应式 */
@media (max-width: 640px) {
  .emergency-contact-page {
    padding: 16px;
  }
  
  .contact-card {
    flex-wrap: wrap;
    padding: 16px;
  }
  
  .contact-actions {
    width: 100%;
    justify-content: flex-start;
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid #e8ecd9;
  }
  
  .emergency-buttons {
    flex-direction: column;
  }
  
  .emergency-buttons button {
    width: 100%;
  }
}
</style>