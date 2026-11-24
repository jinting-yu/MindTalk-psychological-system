<template>
  <div class="consultation-dialog">
    <!-- 页面头部 -->
    <div class="dialog-header">
      <div class="header-left">
        <a-button @click="goBack" type="text" class="back-btn">
          <icon-left /> 返回
        </a-button>
        <div class="student-info">
          <a-avatar :size="40" :style="{ backgroundColor: '#3b82f6' }">
            {{ studentInfo.name?.charAt(0) || '张' }}
          </a-avatar>
          <div class="info">
            <h3>{{ studentInfo.name }}</h3>
            <p>{{ studentInfo.studentId }} | {{ studentInfo.className }}</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <a-tag :color="getStatusColor(consultationStatus)" size="large">
          {{ getStatusText(consultationStatus) }}
        </a-tag>
        <a-button type="outline" @click="endConsultation" v-if="consultationStatus === 'ongoing'" size="small">
          结束咨询
        </a-button>
        <a-button type="primary" @click="startConsultation" v-if="consultationStatus === 'pending'" size="small">
          开始咨询
        </a-button>
      </div>
    </div>

    <!-- 主体：聊天区域 -->
    <div class="dialog-body">
      <!-- 左侧：学生信息卡片 -->
      <div class="left-sidebar">
        <a-card :bordered="false" class="student-card">
          <div class="student-avatar">
            <a-avatar :size="80" :style="{ backgroundColor: '#3b82f6', fontSize: '32px' }">
              {{ studentInfo.name?.charAt(0) || '张' }}
            </a-avatar>
            <h3>{{ studentInfo.name }}</h3>
            <p>{{ studentInfo.className }}</p>
          </div>
          <div class="student-detail">
            <div class="detail-item">
              <span class="label">学号：</span>
              <span>{{ studentInfo.studentId }}</span>
            </div>
            <div class="detail-item">
              <span class="label">学院：</span>
              <span>{{ studentInfo.college }}</span>
            </div>
            <div class="detail-item">
              <span class="label">电话：</span>
              <span>{{ studentInfo.phone }}</span>
            </div>
            <div class="detail-item">
              <span class="label">紧急联系人：</span>
              <span>{{ studentInfo.emergencyContact }}</span>
            </div>
          </div>
          <div class="action-buttons">
            <a-button type="outline" size="small" @click="viewArchive" long>
              <icon-file /> 查看档案
            </a-button>
            <a-button type="outline" size="small" @click="callStudent" long>
              <icon-phone /> 联系学生
            </a-button>
          </div>
        </a-card>

        <!-- 测评记录简要 -->
        <a-card :bordered="false" class="test-summary">
          <template #title>
            <icon-bar-chart /> 近期测评
          </template>
          <div class="test-list">
            <div v-for="test in testRecords" :key="test.id" class="test-item">
              <div class="test-name">{{ test.name }}</div>
              <a-tag :color="getTestResultColor(test.result)" size="small">{{ test.result }}</a-tag>
            </div>
          </div>
          <a-button type="text" size="small" @click="viewAllTests" long>查看全部 →</a-button>
        </a-card>
      </div>

      <!-- 中间：聊天对话 -->
      <div class="chat-area">
        <a-card :bordered="false" class="chat-card">
          <div class="chat-messages" ref="chatContainer">
            <div v-for="(msg, index) in chatMessages" :key="index" :class="['message', msg.role]">
              <a-avatar :size="36" :style="{ backgroundColor: msg.role === 'teacher' ? '#3b82f6' : '#10b981' }">
                {{ msg.role === 'teacher' ? '师' : '生' }}
              </a-avatar>
              <div class="message-bubble">
                <div class="message-info">
                  <span class="sender">{{ msg.role === 'teacher' ? '张老师' : studentInfo.name }}</span>
                  <span class="time">{{ msg.time }}</span>
                </div>
                <div class="message-text">{{ msg.content }}</div>
              </div>
            </div>
            
            <!-- 正在输入提示 -->
            <div v-if="isTyping" class="message teacher typing">
              <a-avatar :size="36" :style="{ backgroundColor: '#3b82f6' }">师</a-avatar>
              <div class="message-bubble">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="chat-input-area">
            <a-textarea
              v-model="newMessage"
              placeholder="输入消息... (Enter 发送，Shift+Enter 换行)"
              :auto-size="{ minRows: 2, maxRows: 4 }"
              @keydown="handleKeydown"
            />
            <div class="input-tools">
             
              <a-button type="primary" @click="sendMessage">
                <icon-send /> 发送
              </a-button>
            </div>
          </div>
        </a-card>
      </div>

      <!-- 右侧：咨询笔记 -->
      <div class="right-sidebar">
        <a-card :bordered="false" class="notes-card">
          <template #title>
            <icon-edit /> 咨询笔记
          </template>
          <a-textarea
            v-model="consultationNotes"
            placeholder="记录咨询要点、观察、建议..."
            :rows="12"
            show-word-limit
            :max-length="2000"
          />
          <div class="notes-actions">
            <a-button type="primary" size="small" @click="saveNotes" long>保存笔记</a-button>
          </div>
        </a-card>

        <!-- 行动计划 -->
        <a-card :bordered="false" class="action-card">
          <template #title>
            <icon-bulb /> 行动计划
          </template>
          <div class="action-list">
            <div v-for="(action, index) in actionPlan" :key="index" class="action-item">
              <a-checkbox v-model="action.completed">
                {{ action.content }}
              </a-checkbox>
            </div>
          </div>
          <a-button type="dashed" size="small" @click="openAddActionModal" long>
            <icon-plus /> 添加任务
          </a-button>
        </a-card>
      </div>
    </div>

    <!-- 结束咨询弹窗 -->
    <a-modal v-model:visible="endModalVisible" title="结束咨询" @ok="confirmEndConsultation">
      <p>确认结束本次咨询吗？</p>
      <a-textarea v-model="endSummary" placeholder="请输入咨询总结（可选）" :rows="3" />
    </a-modal>

    <!-- 添加任务弹窗 -->
    <a-modal v-model:visible="addActionModalVisible" title="添加行动计划" @ok="confirmAddAction" @cancel="cancelAddAction">
      <a-textarea 
        v-model="newActionContent" 
        placeholder="请输入任务内容"
        :rows="3"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  IconLeft,
  IconUser,
  IconFile,
  IconPhone,
  IconBarChart,
  IconMessage,
  IconDownload,
  IconAttachment,
  IconSend,
  IconEdit,
  IconBulb,
  IconPlus,
  IconCalendar,
  IconClockCircle
} from '@arco-design/web-vue/es/icon'

// 类型定义
interface ChatMessage {
  role: 'teacher' | 'student'
  content: string
  time: string
}

interface ActionItem {
  content: string
  completed: boolean
}

interface TestRecord {
  id: number
  name: string
  result: string
  date: string
}

const router = useRouter()

// 状态
const consultationStatus = ref<'pending' | 'ongoing' | 'completed'>('ongoing')
const endModalVisible = ref(false)
const endSummary = ref('')
const newMessage = ref('')
const consultationNotes = ref('')
const chatContainer = ref<HTMLElement>()
const isTyping = ref(false)

// 添加任务弹窗
const addActionModalVisible = ref(false)
const newActionContent = ref('')

// 学生信息
const studentInfo = ref({
  name: '李四',
  studentId: '2024002',
  className: '心理学院1班',
  college: '教育学院',
  phone: '13900139000',
  emergencyContact: '王五（母亲）13700137000'
})

// 测评记录
const testRecords = ref<TestRecord[]>([
  { id: 1, name: 'MBTI职业性格测试', result: 'INTJ型', date: '2025-03-25' },
  { id: 2, name: 'SAS焦虑自评量表', result: '轻度焦虑', date: '2025-03-20' },
  { id: 3, name: '抑郁自评量表', result: '正常范围', date: '2025-03-15' }
])

// 聊天记录
const chatMessages = ref<ChatMessage[]>([
  {
    role: 'student',
    content: '老师您好，我最近总是感觉很焦虑，睡不着觉...',
    time: '10:30'
  },
  {
    role: 'teacher',
    content: '你好，能具体说说是什么时候开始有这样的感觉吗？',
    time: '10:32'
  },
  {
    role: 'student',
    content: '大概两周前，期末考试压力很大，然后就开始失眠了',
    time: '10:34'
  },
  {
    role: 'teacher',
    content: '我理解你的感受。压力大的时候确实容易影响睡眠。能告诉我你平时有什么缓解压力的方式吗？',
    time: '10:36'
  }
])

// 行动计划
const actionPlan = ref<ActionItem[]>([
  { content: '每天进行15分钟深呼吸练习', completed: false },
  { content: '保持规律的作息时间，晚上11点前入睡', completed: false },
  { content: '减少咖啡因摄入', completed: false }
])

// 方法
const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'orange',
    ongoing: 'blue',
    completed: 'green'
  }
  return map[status] || 'gray'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待开始',
    ongoing: '进行中',
    completed: '已结束'
  }
  return map[status] || status
}

const getTestResultColor = (result: string) => {
  if (result.includes('焦虑') || result.includes('抑郁')) return 'orange'
  if (result === '正常范围') return 'green'
  return 'blue'
}

const goBack = () => {
  router.back()
}

const viewArchive = () => {
  router.push(`/user/archive/${studentInfo.value.studentId}`)
}

const callStudent = () => {
  window.location.href = `tel:${studentInfo.value.phone}`
}

const startConsultation = () => {
  consultationStatus.value = 'ongoing'
  Message.success('咨询已开始')
}

const endConsultation = () => {
  endModalVisible.value = true
}

const confirmEndConsultation = () => {
  consultationStatus.value = 'completed'
  endModalVisible.value = false
  Message.success('咨询已结束')
  if (endSummary.value) {
    consultationNotes.value += `\n\n【咨询总结】\n${endSummary.value}`
  }
  saveNotes()
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const sendMessage = () => {
  if (!newMessage.value.trim()) return
  
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  
  chatMessages.value.push({
    role: 'teacher',
    content: newMessage.value,
    time: time
  })
  
  newMessage.value = ''
  
  // 模拟学生正在输入
  setTimeout(() => {
    scrollToBottom()
  }, 100)
  
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const uploadAttachment = () => {
  Message.info('附件上传功能开发中')
}

const saveNotes = () => {
  localStorage.setItem(`consultation_notes_${Date.now()}`, consultationNotes.value)
  Message.success('笔记已保存')
}

const openAddActionModal = () => {
  addActionModalVisible.value = true
}

const confirmAddAction = () => {
  if (newActionContent.value.trim()) {
    actionPlan.value.push({ 
      content: newActionContent.value, 
      completed: false 
    })
    Message.success('任务已添加')
    newActionContent.value = ''
    addActionModalVisible.value = false
  } else {
    Message.warning('请输入任务内容')
  }
}

const cancelAddAction = () => {
  newActionContent.value = ''
  addActionModalVisible.value = false
}

const viewAllTests = () => {
  router.push(`/teacher/test-records?studentId=${studentInfo.value.studentId}`)
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.consultation-dialog {
  height: 100vh;
  background: #f0f2f6;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.dialog-header {
  background: white;
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e6eb;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-info .info h3 {
  margin: 0 0 2px;
  font-size: 16px;
}

.student-info .info p {
  margin: 0;
  font-size: 12px;
  color: #86909c;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 主体 */
.dialog-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

/* 左侧边栏 */
.left-sidebar {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.student-card {
  border-radius: 16px;
}

.student-avatar {
  text-align: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.student-avatar h3 {
  margin: 12px 0 4px;
  font-size: 18px;
}

.student-avatar p {
  margin: 0;
  font-size: 13px;
  color: #86909c;
}

.student-detail {
  padding: 16px 0;
}

.detail-item {
  display: flex;
  padding: 8px 0;
  font-size: 13px;
}

.detail-item .label {
  width: 80px;
  color: #86909c;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.test-summary {
  border-radius: 16px;
}

.test-list {
  max-height: 200px;
  overflow-y: auto;
}

.test-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.test-name {
  font-size: 13px;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
}

.message.teacher {
  flex-direction: row;
}

.message.student {
  flex-direction: row-reverse;
}

.message-bubble {
  max-width: 70%;
}

.message.student .message-bubble {
  text-align: right;
}

.message-info {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
}

.sender {
  font-weight: 500;
  color: #1d2129;
}

.time {
  color: #86909c;
}

.message-text {
  background: #f7f8fa;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
}

.message.student .message-text {
  background: #3b82f6;
  color: white;
}

/* 正在输入动画 */
.typing .message-bubble {
  padding: 12px 16px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #86909c;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* 输入区域 */
.chat-input-area {
  padding: 16px;
  border-top: 1px solid #e5e6eb;
  flex-shrink: 0;
}

.input-tools {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

/* 右侧边栏 */
.right-sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.notes-card, .action-card {
  border-radius: 16px;
}

.notes-actions {
  margin-top: 12px;
}

.action-list {
  margin-bottom: 12px;
}

.action-item {
  padding: 8px 0;
}

/* 响应式 */
@media (max-width: 1000px) {
  .left-sidebar, .right-sidebar {
    width: 260px;
  }
}

@media (max-width: 800px) {
  .left-sidebar, .right-sidebar {
    display: none;
  }
}
</style>