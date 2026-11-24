<template>
  <div class="chat-window" :class="{ 'is-open': isOpen }">
    <!-- 聊天头部 - 点击可展开/折叠 -->
    <div class="chat-header" @click="toggleChat">
      <div class="chat-header-info">
        <a-avatar :size="40" :style="{ backgroundColor: '#7eb09e' }">
          {{ teacherName?.charAt(0) || '师' }}
        </a-avatar>
        <div class="info">
          <h3>{{ teacherName || '心理老师' }}</h3>
          <p>在线</p>
        </div>
      </div>
      <div class="chat-actions" @click.stop="handleClose">
        <icon-close />
      </div>
    </div>

    <!-- 聊天内容区域 - 只有展开时才显示 -->
    <div class="chat-body" ref="chatBodyRef" v-if="isOpen">
      <div class="messages-container">
        <div 
          v-for="msg in messages" 
          :key="msg.id" 
          class="message"
          :class="{ 'message-sent': msg.senderId === currentUserId }"
        >
          <div class="message-content">
            <p>{{ msg.content }}</p>
            <span class="message-time">{{ formatTime(msg.createTime) }}</span>
          </div>
        </div>
        <div v-if="messages.length === 0" class="empty-message">
          暂无消息，开始聊天吧~
        </div>
      </div>
    </div>

    <!-- 输入区域 - 只有展开时才显示 -->
    <div class="chat-footer" v-if="isOpen">
      <div class="input-wrapper">
        <a-textarea
          v-model="inputMessage"
          placeholder="输入消息..."
          :auto-size="{ minRows: 1, maxRows: 4 }"
          @press-enter="handleSendMessage"
        />
        <a-button type="primary" @click="handleSendMessage" :loading="sending">
          <icon-send />
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconClose, IconSend } from '@arco-design/web-vue/es/icon'
import { getMessages, sendMessage, markAsRead, getOrCreateSession } from '@/api/chatController'
import { useLoginUserStore } from '@/store/userStore'

const props = defineProps<{
  teacherId: number
  teacherName: string
}>()

const emit = defineEmits(['close'])

const loginUserStore = useLoginUserStore()
const chatBodyRef = ref<HTMLElement | null>(null)

const visible = ref(true)
const isOpen = ref(false)  // 新增：控制窗口展开/折叠
const messages = ref<any[]>([])
const inputMessage = ref('')
const sending = ref(false)
const sessionId = ref<number | null>(null)

const currentUserId = computed(() => loginUserStore.loginUser?.id)

// 切换聊天窗口展开/折叠
const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    // 展开后滚动到底部
    nextTick(() => scrollToBottom())
  }
}

const handleClose = () => {
  visible.value = false
  emit('close')
}

// 初始化会话
const initSession = async () => {
  try {
    console.log('初始化会话, teacherId:', props.teacherId)
    const res = await getOrCreateSession(props.teacherId)
    console.log('会话创建成功:', res.data)
    sessionId.value = res.data.id
    await loadMessages()
  } catch (error) {
    console.error('初始化会话失败', error)
    Message.error('连接失败，请重试')
  }
}

// 加载消息
const loadMessages = async () => {
  if (!sessionId.value) return
  try {
    const res = await getMessages(sessionId.value, 1, 50)
    messages.value = (res.data.messages || []).reverse()
    await nextTick()
    scrollToBottom()
    // 标记为已读
    await markAsRead(sessionId.value)
  } catch (error) {
    console.error('加载消息失败', error)
  }
}

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim()) return
  if (sending.value) return
  if (!props.teacherId) {
    Message.error('请选择心理老师')
    return
  }
  
  sending.value = true
  try {
    const content = inputMessage.value.trim()
    console.log('发送消息:', { receiverId: props.teacherId, content, receiverRole: 'TEACHER' })
    
    const res = await sendMessage(props.teacherId, content, 'TEACHER')
    console.log('发送成功:', res.data)
    
    messages.value.push({
      id: res.data.id || Date.now(),
      senderId: currentUserId.value,
      content: content,
      createTime: new Date().toISOString()
    })
    
    inputMessage.value = ''
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送失败:', error)
    Message.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)}分钟前`
  return `${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 轮询新消息
let pollInterval: any = null
const startPolling = () => {
  pollInterval = setInterval(async () => {
    if (sessionId.value && isOpen.value) {  // 只在展开时轮询
      await loadMessages()
    }
  }, 3000)
}

onMounted(() => {
  if (props.teacherId) {
    initSession()
    startPolling()
  } else {
    console.error('teacherId 为空')
    Message.error('请选择心理老师')
  }
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

<style scoped>
.chat-window {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 380px;
  height: 60px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
  cursor: pointer;
}

/* 展开状态 - 放在悬浮按钮上方 */
.chat-window.is-open {
  height: 520px;
  bottom: 100px;
  right: 30px;
  cursor: default;
}

/* 聊天头部 - 只有未展开时可点击 */
.chat-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #7eb09e 0%, #6b8c7c 100%);
  color: white;
  border-radius: 16px 16px 0 0;
}

/* 展开时头部圆角正常，未展开时整个窗口都是圆角 */
.chat-window:not(.is-open) .chat-header {
  border-radius: 16px;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.info h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.info p {
  font-size: 12px;
  margin: 0;
  opacity: 0.8;
}

.chat-actions {
  cursor: pointer;
  font-size: 18px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.chat-actions:hover {
  opacity: 1;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
  justify-content: flex-end;
}

.message-sent {
  justify-content: flex-end;
}

.message-received {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 18px;
  background: linear-gradient(135deg, #7eb09e 0%, #6b8c7c 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-received .message-content {
  background: white;
  color: #1e293b;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-content p {
  margin: 0;
  word-wrap: break-word;
}

.message-time {
  font-size: 10px;
  opacity: 0.6;
  margin-top: 4px;
  display: block;
}

.empty-message {
  text-align: center;
  color: #94a3b8;
  padding: 40px;
}

.chat-footer {
  flex-shrink: 0;
  padding: 12px 16px;
  border-top: 1px solid #eef2ff;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper :deep(.arco-textarea) {
  flex: 1;
  border-radius: 20px;
}

.input-wrapper :deep(.arco-btn) {
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
</style>