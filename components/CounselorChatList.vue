<template>
  <div class="counselor-chat-list">
    <div class="chat-list-header">
      <h3>学生咨询列表</h3>
      <a-badge :count="totalUnread" :offset="[10, 0]">
        <span class="total-unread">未读消息</span>
      </a-badge>
    </div>
    
    <div class="chat-list-body">
      <div 
        v-for="session in sessions" 
        :key="session.sessionId"
        class="chat-item"
        :class="{ 'active': activeSessionId === session.sessionId, 'has-unread': session.unreadCount > 0 }"
        @click="openChat(session)"
      >
        <div class="chat-avatar">
          <a-avatar :size="48" :style="{ backgroundColor: '#7eb09e' }">
            {{ session.otherUserName ? session.otherUserName.charAt(0) : '学' }}
          </a-avatar>
        </div>
        
        <div class="chat-info">
          <div class="chat-name">
            <span class="student-name">{{ session.otherUserName }}</span>
            <span class="chat-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="chat-preview">
            <span class="last-message">{{ session.lastMessage }}</span>
            <a-badge :count="session.unreadCount" v-if="session.unreadCount > 0" />
          </div>
        </div>
      </div>
      
      <div v-if="sessions.length === 0" class="empty-state">
        <icon-message />
        <p>暂无学生咨询</p>
      </div>
    </div>
    
    <!-- 聊天窗口 -->
    <ChatWindow
      v-if="currentChat"
      :otherUserId="currentChat.otherUserId"
      :otherUserName="currentChat.otherUserName"
      otherUserRole="USER"
      @close="closeChat"
      @newMessage="onNewMessage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { IconMessage } from '@arco-design/web-vue/es/icon'
import ChatWindow from './ChatWindow.vue'
import { getUserSessions, getUnreadCount } from '@/api/chatController'

const sessions = ref<any[]>([])
const activeSessionId = ref<number | null>(null)
const currentChat = ref<any>(null)
const totalUnread = ref(0)

// 加载会话列表
const loadSessions = async () => {
  try {
    const res = await getUserSessions()
    sessions.value = res.data || []
    calculateTotalUnread()
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
}

// 计算总未读数
const calculateTotalUnread = () => {
  totalUnread.value = sessions.value.reduce((sum, session) => sum + (session.unreadCount || 0), 0)
}

// 打开聊天
const openChat = (session: any) => {
  activeSessionId.value = session.sessionId
  currentChat.value = session
}

// 关闭聊天
const closeChat = () => {
  currentChat.value = null
  activeSessionId.value = null
  loadSessions() // 刷新列表
}

// 新消息回调
const onNewMessage = () => {
  loadSessions()
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 24 * 60 * 60 * 1000) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 轮询获取新消息
let pollInterval: any = null
const startPolling = () => {
  pollInterval = setInterval(() => {
    loadSessions()
  }, 5000)
}

onMounted(() => {
  loadSessions()
  startPolling()
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

<style scoped>
.counselor-chat-list {
  width: 100%;
  height: 100%;
  background: white;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-list-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #7eb09e 0%, #6b8c7c 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-list-header h3 {
  margin: 0;
  font-size: 18px;
}

.total-unread {
  font-size: 12px;
}

.chat-list-body {
  flex: 1;
  overflow-y: auto;
  background: #f8fafc;
}

.chat-item {
  display: flex;
  padding: 16px;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #eef2ff;
}

.chat-item:hover {
  background: #f1f5f9;
}

.chat-item.active {
  background: #e2e8f0;
}

.chat-item.has-unread {
  background: #fef9e7;
}

.chat-avatar {
  position: relative;
  flex-shrink: 0;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-name {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}

.student-name {
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
}

.chat-time {
  font-size: 11px;
  color: #94a3b8;
}

.chat-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.last-message {
  font-size: 13px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #94a3b8;
}

.empty-state svg {
  font-size: 48px;
  margin-bottom: 16px;
}
</style>