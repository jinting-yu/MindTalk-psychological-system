<template>
  <div class="teacher-workbench">
    <!-- 顶部统计卡片（合并：6个卡片） -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalAppointments }}</div>
          <div class="stat-label">总预约数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏳</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingAppointments }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalStudents }}</div>
          <div class="stat-label">服务学生数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📝</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalAssessments }}</div>
          <div class="stat-label">测评总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏱️</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalHours }}h</div>
          <div class="stat-label">服务总时长</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.satisfaction }}%</div>
          <div class="stat-label">学生满意度</div>
        </div>
      </div>
    </div>

    <!-- 第一行：今日预约 + 学生咨询（聊天）+ 近期测评（三列布局） -->
    <div class="main-content">
      <!-- 今日预约 -->
      <div class="content-card">
        <div class="card-header">
          <h3>今日预约</h3>
          <a-button type="text" size="small" @click="viewAllAppointments">查看全部 →</a-button>
        </div>
        <div class="appointment-list">
          <div v-for="item in todayAppointments" :key="item.id" class="appointment-item">
            <div class="appointment-time">{{ item.time }}</div>
            <div class="appointment-info">
              <div class="appointment-name">{{ item.name }}</div>
              <div class="appointment-title">{{ item.title }}</div>
            </div>
            <div class="appointment-status" :class="item.status === '待确认' ? 'pending' : 'confirmed'">
              {{ item.status }}
            </div>
            <div class="appointment-actions">
              <a-button size="small" type="primary" @click="confirmAppointment(item)">确认</a-button>
              <a-button size="small" @click="startConsultation(item)">开始咨询</a-button>
            </div>
          </div>
          <div v-if="todayAppointments.length === 0" class="empty-state">暂无今日预约</div>
        </div>
      </div>

      <!-- 学生咨询（聊天）- 从第一个文件合并 -->
      <div class="content-card chat-list-card">
        <div class="card-header">
          <h3>💬 学生咨询</h3>
          <a-badge :count="totalUnread" :offset="[10, 0]" v-if="totalUnread > 0">
            <span class="unread-tag">{{ totalUnread }}条未读</span>
          </a-badge>
          <a-button type="text" size="small" @click="viewAllChats">查看全部 →</a-button>
        </div>
        <div class="chat-sessions-list">
          <div 
            v-for="session in chatSessions" 
            :key="session.sessionId"
            class="chat-session-item"
            :class="{ 'has-unread': session.unreadCount > 0 }"
            @click="openChatWithStudent(session)"
          >
            <div class="session-avatar">
              <a-avatar :size="40" :style="{ backgroundColor: '#7eb09e' }">
                {{ session.otherUserName?.charAt(0) || '学' }}
              </a-avatar>
              <span class="online-dot" v-if="session.isOnline"></span>
            </div>
            <div class="session-info">
              <div class="session-name">
                <span>{{ session.otherUserName }}</span>
                <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
              </div>
              <div class="session-preview">
                <span class="last-message">{{ session.lastMessage || '暂无消息' }}</span>
                <a-badge :count="session.unreadCount" v-if="session.unreadCount > 0" />
              </div>
            </div>
          </div>
          <div v-if="chatSessions.length === 0" class="empty-state">
            <icon-message />
            <p>暂无学生咨询消息</p>
          </div>
        </div>
      </div>

      <!-- 近期测评记录 -->
      <div class="content-card">
        <div class="card-header">
          <h3>近期测评记录</h3>
          <a-button type="text" size="small" @click="viewAllRecords">查看更多 →</a-button>
        </div>
        <div class="record-list">
          <table class="record-table">
            <thead>
              <tr><th>学生</th><th>测评名称</th><th>结果</th><th>测评日期</th></tr>
            </thead>
            <tbody>
              <tr v-for="record in recentRecords" :key="record.id">
                <td>{{ record.studentName }}</td>
                <td>{{ record.assessmentName }}</td>
                <td :class="getResultClass(record.result)">{{ record.result }}</td>
                <td>{{ record.date }}</td>
              </tr>
              <tr v-if="recentRecords.length === 0">
                <td colspan="4" class="empty-state">暂无测评记录</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 第二行：我的预约 + 文章管理（两列布局） -->
    <div class="main-content">
      <!-- 我的预约 -->
      <div class="content-card">
        <div class="card-header">
          <h3>我的预约</h3>
          <a-button type="text" size="small" @click="viewMyAppointments">查看全部 →</a-button>
        </div>
        <div class="appointment-list">
          <div v-for="item in myAppointments" :key="item.id" class="appointment-item">
            <div class="appointment-time">{{ item.date }} {{ item.time }}</div>
            <div class="appointment-info">
              <div class="appointment-name">{{ item.studentName }}</div>
              <div class="appointment-title">{{ item.title }}</div>
            </div>
            <div class="appointment-status" :class="item.status === '待确认' ? 'pending' : 'confirmed'">
              {{ item.status }}
            </div>
            <div class="appointment-actions">
              <a-button v-if="item.status === '待确认'" size="small" type="primary" @click="confirmAppointment(item)">确认</a-button>
              <a-button v-if="item.status === '已确认'" size="small" @click="startConsultation(item)">开始咨询</a-button>
            </div>
          </div>
          <div v-if="myAppointments.length === 0" class="empty-state">暂无预约记录</div>
        </div>
      </div>

      <!-- 文章管理（从第二个文件合并） -->
      <div class="content-card article-card">
        <div class="card-header">
          <h3>📝 心理咨询文章</h3>
          <a-button type="primary" size="small" @click="goToCreateArticle">
            + 发布文章
          </a-button>
        </div>
        <div class="article-list">
          <div v-for="article in myArticles" :key="article.id" class="article-item">
            <div class="article-info">
              <div class="article-title">{{ article.title }}</div>
              <div class="article-meta">
                <a-tag :color="article.status === 'published' ? 'green' : 'orange'" size="small">
                  {{ article.status === 'published' ? '已发布' : '草稿' }}
                </a-tag>
                <span class="article-date">{{ article.createTime }}</span>
              </div>
            </div>
            <div class="article-actions">
              <a-button type="text" size="small" @click="editArticle(article)">编辑</a-button>
              <a-button type="text" size="small" status="danger" @click="deleteArticle(article)">删除</a-button>
            </div>
          </div>
          <div v-if="myArticles.length === 0" class="empty-state">
            暂无文章，点击上方按钮发布第一篇
          </div>
        </div>
        <div class="card-footer" v-if="myArticles.length > 0">
          <a-button type="text" size="small" @click="viewAllArticles" long>查看全部文章 →</a-button>
        </div>
      </div>
    </div>

    <!-- 第三行：数据统计 -->
    <div class="main-content">
      <div class="content-card">
        <div class="card-header">
          <h3>数据统计</h3>
          <a-button type="text" size="small" @click="viewStatistics">查看更多 →</a-button>
        </div>
        <div class="statistics-content">
          <div class="stat-item">
            <span class="stat-label">本周预约数</span>
            <span class="stat-number">{{ stats.weeklyAppointments }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">本月测评数</span>
            <span class="stat-number">{{ stats.monthlyAssessments }}</span>
          </div>
        </div>
      </div>
      <div class="content-card">
        <div class="card-header">
          <h3>快捷操作</h3>
        </div>
        <div class="quick-actions">
          <a-button type="outline" @click="goToCreateArticle" long>
            ✏️ 发布新文章
          </a-button>
          <a-button type="outline" @click="viewAllArticles" long>
            📖 管理文章
          </a-button>
          <a-button type="outline" @click="viewAllChats" long>
            💬 查看全部咨询
          </a-button>
        </div>
      </div>
    </div>

    <!-- 聊天窗口（从第一个文件合并） -->
    <ChatWindow
      v-if="selectedStudent"
      :otherUserId="selectedStudent.otherUserId"
      :otherUserName="selectedStudent.otherUserName"
      otherUserRole="USER"
      @close="closeChat"
      @newMessage="onNewMessage"
    />

    <!-- 删除确认弹窗（从第二个文件合并） -->
    <a-modal v-model:visible="deleteModalVisible" title="删除文章" @ok="confirmDelete">
      <p>确定要删除文章《{{ deleteTarget?.title }}》吗？</p>
      <p class="delete-warning">删除后无法恢复，请谨慎操作。</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'
import { IconMessage } from '@arco-design/web-vue/es/icon'
import ChatWindow from '@/components/ChatWindow.vue'
import { getUserSessions, getUnreadCount } from '@/api/chatController'

const router = useRouter()

// 统计数据（合并：6个卡片）
const stats = ref({
  totalAppointments: 28,
  pendingAppointments: 5,
  totalStudents: 45,
  totalAssessments: 151,
  weeklyAppointments: 12,
  monthlyAssessments: 45,
  totalHours: 86,
  satisfaction: 98
})

// 今日预约
const todayAppointments = ref([
  { id: 1, time: '09:00-10:00', name: '张三', title: '情绪管理咨询', status: '待确认' },
  { id: 2, time: '10:30-11:30', name: '李四', title: '职业规划咨询', status: '已确认' },
  { id: 3, time: '14:00-15:00', name: '王五', title: '人际关系咨询', status: '待确认' }
])

// 我的预约
const myAppointments = ref([
  { id: 1, date: '2025-03-25', time: '09:00-10:00', studentName: '张三', title: '情绪管理咨询', status: '待确认' },
  { id: 2, date: '2025-03-26', time: '10:30-11:30', studentName: '李四', title: '职业规划咨询', status: '已确认' }
])

// 近期测评记录
const recentRecords = ref([
  { id: 1, studentName: '张三', assessmentName: 'MBTI职业性格测试', result: 'INTJ型', date: '2025-03-25' },
  { id: 2, studentName: '李四', assessmentName: 'SAS焦虑自评量表', result: '轻度焦虑', date: '2025-03-24' },
  { id: 3, studentName: '王五', assessmentName: '抑郁自评量表', result: '正常范围', date: '2025-03-23' }
])

// 文章列表（从第二个文件合并）
const myArticles = ref([
  { id: 1, title: '如何缓解考试焦虑', status: 'published', createTime: '2025-03-20' },
  { id: 2, title: '青春期心理特点与应对', status: 'published', createTime: '2025-03-18' },
  { id: 3, title: '人际关系中的沟通技巧', status: 'draft', createTime: '2025-03-22' }
])

// 删除相关
const deleteModalVisible = ref(false)
const deleteTarget = ref<any>(null)

// 聊天相关（从第一个文件合并）
const chatSessions = ref<any[]>([])
const selectedStudent = ref<any>(null)
const totalUnread = ref(0)

let pollInterval: any = null

// 获取结果样式
const getResultClass = (result: string) => {
  if (result.includes('焦虑') || result.includes('抑郁')) return 'warning'
  if (result.includes('正常')) return 'normal'
  return ''
}

// 确认预约
const confirmAppointment = (item: any) => {
  Message.success(`已确认 ${item.name || item.studentName} 的预约`)
  item.status = '已确认'
}

// 开始咨询
const startConsultation = (item: any) => {
  router.push(`/teacher/consultation/${item.id}`)
}

// 路由跳转方法
const viewAllAppointments = () => router.push('/appointment/list')
const viewAllRecords = () => router.push('/assessment/records')
const viewMyAppointments = () => router.push('/teacher/appointments')
const viewStatistics = () => router.push('/teacher/statistics')
const viewAllChats = () => router.push('/teacher/chat')

// 文章相关方法（从第二个文件合并）
const goToCreateArticle = () => {
  router.push('/teacher/article/create')
}

const editArticle = (article: any) => {
  router.push(`/teacher/article/edit/${article.id}`)
}

const deleteArticle = (article: any) => {
  deleteTarget.value = article
  deleteModalVisible.value = true
}

const confirmDelete = () => {
  if (deleteTarget.value) {
    const index = myArticles.value.findIndex(a => a.id === deleteTarget.value.id)
    if (index !== -1) {
      myArticles.value.splice(index, 1)
      Message.success('删除成功')
    }
  }
  deleteModalVisible.value = false
  deleteTarget.value = null
}

const viewAllArticles = () => {
  router.push('/teacher/articles')
}

// 聊天相关方法（从第一个文件合并）
const openChatWithStudent = (session: any) => {
  selectedStudent.value = session
}

const closeChat = () => {
  selectedStudent.value = null
  loadChatSessions()
}

const onNewMessage = () => {
  loadChatSessions()
}

const loadChatSessions = async () => {
  try {
    const res = await getUserSessions()
    chatSessions.value = res.data || []
    calculateTotalUnread()
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
}

const calculateTotalUnread = () => {
  totalUnread.value = chatSessions.value.reduce((sum, session) => sum + (session.unreadCount || 0), 0)
  stats.value.pendingAppointments = Math.max(5, totalUnread.value)
}

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

const startPolling = () => {
  pollInterval = setInterval(() => {
    loadChatSessions()
  }, 5000)
}

onMounted(() => {
  loadChatSessions()
  startPolling()
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

<!-- 样式部分需要合并两个文件的样式，添加聊天和文章相关样式 -->