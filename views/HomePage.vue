<template>
  <div class="home-page">
    <!-- 滚动资讯栏 -->
    <div class="news-ticker">
      <div class="ticker-container">
        <span class="ticker-label">📢 心理资讯</span>
        <div class="ticker-wrapper">
          <div class="ticker-content" :class="{ 'animate': isAnimating }">
            <span v-for="(news, idx) in newsList" :key="idx" class="ticker-item">
              {{ news }}
            </span>
            <span v-for="(news, idx) in newsList" :key="'dup-'+idx" class="ticker-item">
              {{ news }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Hero 区域 -->
    <div class="hero-section">
      <h1>探索内心，遇见更好的自己</h1>
      <p>专业心理测评 · 一对一心理咨询 · 科学了解自己</p>
      <div class="hero-buttons">
        <a-button type="primary" size="large" @click="goToTests">
          🔍 开始心理测评
        </a-button>
        <a-button size="large" @click="goToBooking">
          📅 预约心理咨询
        </a-button>
        <a-button size="large" @click="openChatSelector" class="chat-btn">
          💬 在线咨询老师
        </a-button>
      </div>
    </div>

    <!-- 热门心理测评 -->
    <div class="section">
      <div class="section-header">
        <h2>🔥 热门心理测评</h2>
        <a-link @click="goToTests">查看全部 →</a-link>
      </div>
      <div class="card-grid">
        <div v-for="test in hotTests" :key="test.id" class="test-card" @click="goToTest(test.id)">
          <div class="card-icon">{{ test.icon }}</div>
          <h3>{{ test.name }}</h3>
          <p>{{ test.description }}</p>
          <div class="test-meta">
            <span>⏰ {{ test.duration }}分钟</span>
            <span>📝 {{ test.questions }}题</span>
            <span>👥 {{ test.testCount }}人测过</span>
          </div>
          <a-button type="primary" size="small">开始测评</a-button>
        </div>
      </div>
    </div>

    <!-- 心理科普文章（带图片） -->
    <div class="section">
      <div class="section-header">
        <h2>📚 心理科普文章</h2>
        <a-link @click="goToNews">更多文章 →</a-link>
      </div>
      <div class="article-grid">
        <div v-for="article in articles" :key="article.id" class="article-card" @click="goToArticle(article.id)">
          <div class="article-cover">
            <img :src="article.cover" :alt="article.title" />
            <span class="article-category">{{ article.categoryName }}</span>
          </div>
          <div class="article-content">
            <h3>{{ article.title }}</h3>
            <p>{{ article.summary }}</p>
            <div class="article-meta">
              <span>📅 {{ article.date }}</span>
              <span>👁️ {{ article.views }}阅读</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 专业咨询师推荐 -->
    <div class="section bg-light">
      <div class="section-header">
        <h2>👨‍⚕️ 专业咨询师推荐</h2>
        <a-link @click="goToBooking">查看全部 →</a-link>
      </div>
      <div class="counselor-grid">
        <div v-for="counselor in counselors" :key="counselor.id" class="counselor-card">
          <a-avatar :size="80" :style="{ backgroundColor: counselor.color }">
            {{ counselor.name.charAt(0) }}
          </a-avatar>
          <h3>{{ counselor.name }}</h3>
          <p>{{ counselor.title }}</p>
          <div class="rating">
            <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= counselor.rating }">★</span>
          </div>
          <a-button type="outline" size="small" @click="bookCounselor(counselor.id)">
            预约咨询
          </a-button>
          <a-button type="primary" size="small" @click="startChatWithCounselor(counselor)" style="margin-top: 8px;">
            💬 立即咨询
          </a-button>
        </div>
      </div>
    </div>

    <!-- 快速自测工具 -->
    <div class="section">
      <div class="section-header">
        <h2>⚡ 快速自测工具</h2>
        <p class="section-desc">3-5分钟，快速了解你的心理状态</p>
      </div>
      <div class="quick-test-grid">
        <div v-for="tool in quickTests" :key="tool.id" class="quick-test-card" @click="goToTest(tool.id)">
          <div class="quick-test-icon">{{ tool.icon }}</div>
          <div class="quick-test-info">
            <h3>{{ tool.name }}</h3>
            <p>{{ tool.desc }}</p>
            <span class="quick-test-time">⏱️ {{ tool.time }}分钟 · 📝 {{ tool.questions }}题</span>
          </div>
          <a-button type="text">开始测试 →</a-button>
        </div>
      </div>
    </div>

    <!-- 音乐播放器 -->
    <MusicPlayer />

    <!-- 聊天悬浮按钮 -->
    <div class="chat-fab" @click="openChatSelector">
      <icon-message />
      <span class="unread-badge" v-if="totalUnread > 0">{{ totalUnread > 99 ? '99+' : totalUnread }}</span>
    </div>

    <!-- 咨询师选择弹窗 -->
    <a-modal 
      v-model:visible="showCounselorModal" 
      title="选择咨询老师" 
      :footer="false"
      width="400px"
    >
      <div class="counselor-list-modal">
        <div 
          v-for="counselor in counselorList" 
          :key="counselor.id" 
          class="counselor-item-modal"
          @click="selectCounselor(counselor)"
        >
          <a-avatar :size="48" :style="{ backgroundColor: counselor.color || '#7eb09e' }">
            {{ counselor.name?.charAt(0) || '师' }}
          </a-avatar>
          <div class="counselor-info-modal">
            <h4>{{ counselor.name }}</h4>
            <p>{{ counselor.title }}</p>
          </div>
          <a-button type="primary" size="small">发消息</a-button>
        </div>
      </div>
    </a-modal>

    <!-- 聊天窗口组件 -->
    <ChatWindow 
      v-if="selectedCounselor"
      :teacherId="selectedCounselor.id"
      :teacherName="selectedCounselor.name || selectedCounselor.realName"
      @close="closeChat"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { IconMessage } from '@arco-design/web-vue/es/icon'
import MusicPlayer from '@/components/MusicPlayer.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import { getCounselors, getUnreadCount } from '@/api/chatController'
import { useLoginUserStore } from '@/store/userStore'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 滚动资讯控制
const isAnimating = ref(true)

// 聊天相关
const showCounselorModal = ref(false)
const counselorList = ref<any[]>([])
const selectedCounselor = ref<any>(null)
const totalUnread = ref(0)

// 滚动资讯数据
const newsList = ref([
  '✨ 全新MBTI性格测试上线！16种人格类型深度解读',
  '🧠 焦虑自评量表(SAS)免费开放，3分钟了解你的焦虑水平',
  '📊 专业心理咨询服务已上线，资深咨询师在线守候',
  '💡 心理健康小贴士：每天冥想10分钟，有效缓解压力',
  '🎯 人际关系综合诊断量表，帮你改善社交困扰'
])

// 热门测评 - 扩展到6个
const hotTests = ref([
  { 
    id: 1, 
    name: 'MBTI职业性格测试', 
    icon: '🔮', 
    description: '经典93题，解读你的性格偏好与职业方向，帮助找到适合的工作领域',
    duration: 15,
    questions: 93,
    testCount: 12580
  },
  { 
    id: 2, 
    name: 'SAS焦虑自评量表', 
    icon: '😟', 
    description: '专业焦虑水平评估，提供缓解建议和放松技巧',
    duration: 5,
    questions: 20,
    testCount: 8920
  },
  { 
    id: 3, 
    name: '抑郁自评量表(SDS)', 
    icon: '🌧️', 
    description: '了解近期情绪状态，评估抑郁倾向，获取专业支持',
    duration: 5,
    questions: 20,
    testCount: 10500
  },
  { 
    id: 4, 
    name: '人际关系综合诊断', 
    icon: '👥', 
    description: '评估人际交往能力与困扰程度，提升社交技巧',
    duration: 8,
    questions: 28,
    testCount: 5680
  },
  { 
    id: 5, 
    name: '艾森克人格问卷(EPQ)', 
    icon: '🧬', 
    description: '测量内外向、神经质、精神质三个人格维度',
    duration: 10,
    questions: 88,
    testCount: 4320
  },
  { 
    id: 6, 
    name: '压力应对方式问卷', 
    icon: '💪', 
    description: '了解你面对压力时的应对策略，学习更有效的应对方式',
    duration: 6,
    questions: 36,
    testCount: 3780
  }
])

// 文章数据（带配图）
const articles = ref([
  {
    id: 1,
    title: '如何识别焦虑症的早期信号？',
    summary: '焦虑症是现代人常见的心理问题，了解早期信号有助于及时干预。本文详细介绍焦虑症的10个早期信号...',
    categoryName: '心理健康',
    cover: 'https://picsum.photos/id/20/400/240',
    date: '2025-03-25',
    views: 1250
  },
  {
    id: 2,
    title: 'MBTI性格测试结果解读指南',
    summary: '16种人格类型各有特点，找到适合自己的发展路径。本文将详细解读每种性格类型的优势和适合职业...',
    categoryName: '自我成长',
    cover: 'https://picsum.photos/id/26/400/240',
    date: '2025-03-24',
    views: 3420
  },
  {
    id: 3,
    title: '建立健康人际关系的5个关键',
    summary: '无论是亲密关系还是职场关系，这5个要素都至关重要。学会沟通、尊重、信任、支持和边界...',
    categoryName: '人际关系',
    cover: 'https://picsum.photos/id/100/400/240',
    date: '2025-03-23',
    views: 2150
  }
])

// 咨询师推荐
const counselors = ref([
  { id: 1, name: '张老师', title: '情绪管理专家', rating: 5, color: '#3b82f6' },
  { id: 2, name: '李老师', title: '职业心理咨询师', rating: 5, color: '#a855f7' },
  { id: 3, name: '王老师', title: '人际关系指导师', rating: 4, color: '#10b981' },
  { id: 4, name: '陈老师', title: '家庭治疗师', rating: 5, color: '#f59e0b' }
])

// 快速自测工具
const quickTests = ref([
  { id: 5, name: '压力水平测试', icon: '💪', desc: '了解当前压力程度', time: 3, questions: 15 },
  { id: 6, name: '睡眠质量评估', icon: '😴', desc: '评估睡眠状况', time: 2, questions: 10 },
  { id: 7, name: '自尊水平测试', icon: '✨', desc: '了解自我价值感', time: 3, questions: 12 }
])

// 调整滚动速度
const adjustScrollSpeed = () => {
  const tickerContent = document.querySelector('.ticker-content') as HTMLElement
  if (tickerContent) {
    const contentWidth = tickerContent.scrollWidth / 2
    let duration = Math.min(45, Math.max(8, contentWidth / 60))
    tickerContent.style.animationDuration = `${duration}s`
  }
}

const handleResize = () => {
  adjustScrollSpeed()
}

// 跳转
const goToTests = () => router.push('/test/list')
const goToBooking = () => router.push('/booking/list')
const goToNews = () => router.push('/news')
const goToTest = (id: number) => router.push(`/answer/do/${id}`)
const goToArticle = (id: number) => router.push(`/news/detail/${id}`)
const bookCounselor = (id: number) => router.push(`/booking/create?counselorId=${id}`)

// 聊天功能
// 聊天功能
const openChatSelector = async () => {
  if (!loginUserStore.isLogin()) {
    Message.warning('请先登录')
    router.push('/user/login')
    return
  }
  
  try {
    const res = await getCounselors()
    console.log('完整响应:', res)
    console.log('咨询师列表数据:', res.data)
    // 如果 res.data 是数组，打印第一个元素的所有字段
    if (res.data && res.data.length > 0) {
      console.log('第一个咨询师的所有字段:', JSON.stringify(res.data[0], null, 2))
    }
    counselorList.value = res.data || []
    if (counselorList.value.length === 0) {
      Message.info('暂无在线咨询师')
      return
    }
    showCounselorModal.value = true
  } catch (error) {
    console.error('获取咨询师列表失败', error)
    Message.error('获取咨询师列表失败')
  }
}

// 开始聊天（从咨询师卡片点击）
const startChatWithCounselor = (counselor: any) => {
  if (!loginUserStore.isLogin()) {
    Message.warning('请先登录')
    router.push('/user/login')
    return
  }
  console.log('开始聊天, 完整老师对象:', counselor)
  // 打印所有字段名
  console.log('老师对象的字段:', Object.keys(counselor))
  
  // 尝试多种可能的字段名
  const teacherId = counselor.id || counselor.userId || counselor.counselorId || counselor.teacherId
  const teacherName = counselor.name || counselor.realName || counselor.nickname || counselor.userName || counselor.teacherName
  
  console.log('提取的ID:', teacherId)
  console.log('提取的Name:', teacherName)
  
  selectedCounselor.value = {
    id: teacherId,
    name: teacherName
  }
}

// 选择咨询师（从弹窗选择）
const selectCounselor = (counselor: any) => {
  showCounselorModal.value = false
  console.log('选择的老师, 完整对象:', counselor)
  console.log('老师对象的字段:', Object.keys(counselor))
  
  const teacherId = counselor.id || counselor.userId || counselor.counselorId || counselor.teacherId
  const teacherName = counselor.name || counselor.realName || counselor.nickname || counselor.userName || counselor.teacherName
  
  console.log('提取的ID:', teacherId)
  console.log('提取的Name:', teacherName)
  
  selectedCounselor.value = {
    id: teacherId,
    name: teacherName
  }
}

// 关闭聊天
const closeChat = () => {
  selectedCounselor.value = null
}

// 获取未读消息数
const fetchUnreadCount = async () => {
  if (!loginUserStore.isLogin()) return
  try {
    const res = await getUnreadCount()
    totalUnread.value = res.data || 0
  } catch (error) {
    console.error('获取未读消息数失败', error)
  }
}

onMounted(() => {
  setTimeout(() => adjustScrollSpeed(), 100)
  window.addEventListener('resize', handleResize)
  fetchUnreadCount()
  // 每10秒刷新未读消息数
  const interval = setInterval(fetchUnreadCount, 10000)
  onBeforeUnmount(() => clearInterval(interval))
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.home-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

/* 滚动资讯栏 */
.news-ticker {
  background: linear-gradient(135deg, #7cb87a 0%, #9bcd9c 100%);
  border-radius: 16px;
  margin: 20px 0 32px;
  padding: 0 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.ticker-container {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 52px;
}

.ticker-label {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: 4px 16px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

.ticker-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
  height: 52px;
  display: flex;
  align-items: center;
}

.ticker-content {
  display: inline-flex;
  white-space: nowrap;
  gap: 48px;
  will-change: transform;
  animation: scroll-left linear infinite;
}

.ticker-content.animate {
  animation: scroll-left linear infinite;
}

@keyframes scroll-left {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.ticker-item {
  display: inline-block;
  font-size: 14px;
  color: #fff;
  font-weight: 500;
}

.ticker-item::before {
  content: "•";
  margin-right: 12px;
  font-weight: bold;
  opacity: 0.8;
}

/* Hero 区域 */
.hero-section {
  text-align: center;
  margin: 40px 0 60px;
  padding: 60px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
  border-radius: 32px;
}

.hero-section h1 {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, #1e293b, #7cb87a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
}

.hero-section p {
  font-size: 18px;
  color: #475569;
  margin-bottom: 32px;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.chat-btn {
  background: linear-gradient(135deg, #a855f7 0%, #7c3aed 100%);
  border: none;
  color: white;
}

.chat-btn:hover {
  background: linear-gradient(135deg, #b86ff8 0%, #8b5cf6 100%);
}

/* 通用区块样式 */
.section {
  margin-bottom: 64px;
}

.section.bg-light {
  background: #f8fafc;
  padding: 48px 32px;
  border-radius: 32px;
  margin: 0 -8px 64px -8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  flex-wrap: wrap;
  margin-bottom: 32px;
  gap: 16px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #0f172a;
  margin: 0;
}

.section-desc {
  color: #64748b;
  font-size: 14px;
}

/* 测评卡片 - 一排三个 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.test-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  border: 1px solid #eef2ff;
  cursor: pointer;
  text-align: center;
}

.test-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 30px -12px rgba(0, 0, 0, 0.1);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.test-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.test-card p {
  font-size: 13px;
  color: #475569;
  margin-bottom: 16px;
  min-height: 40px;
}

.test-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  color: #86909c;
  font-size: 12px;
  margin-bottom: 20px;
}

/* 文章网格 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.article-card {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eef2ff;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 30px -12px rgba(0, 0, 0, 0.1);
}

.article-cover {
  height: 160px;
  position: relative;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.article-card:hover .article-cover img {
  transform: scale(1.05);
}

.article-category {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: #fff;
}

.article-content {
  padding: 20px;
}

.article-content h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-content p {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #94a3b8;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 咨询师卡片 */
.counselor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}

.counselor-card {
  text-align: center;
  padding: 24px;
  background: #fff;
  border-radius: 20px;
  transition: all 0.3s;
}

.counselor-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.counselor-card h3 {
  margin: 16px 0 4px;
  font-size: 18px;
}

.counselor-card p {
  color: #64748b;
  font-size: 13px;
}

.rating .star {
  color: #e2e8f0;
  font-size: 16px;
}

.rating .star.active {
  color: #fbbf24;
}

/* 快速自测 */
.quick-test-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-test-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #eef2ff;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-test-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #7cb87a;
}

.quick-test-icon {
  font-size: 40px;
}

.quick-test-info {
  flex: 1;
}

.quick-test-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.quick-test-info p {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}

.quick-test-time {
  font-size: 11px;
  color: #94a3b8;
}

/* 聊天悬浮按钮 */
.chat-fab {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #a855f7 0%, #7c3aed 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(124, 58, 237, 0.4);
  transition: all 0.3s ease;
  z-index: 999;
}

.chat-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(124, 58, 237, 0.5);
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4d4f;
  color: white;
  font-size: 11px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

/* 咨询师选择弹窗 */
.counselor-list-modal {
  max-height: 400px;
  overflow-y: auto;
}

.counselor-item-modal {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  cursor: pointer;
  border-radius: 12px;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.counselor-item-modal:hover {
  background: #f5f7f5;
}

.counselor-info-modal {
  flex: 1;
}

.counselor-info-modal h4 {
  margin: 0 0 4px;
  font-size: 16px;
}

.counselor-info-modal p {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}

/* 响应式 */
@media (max-width: 1024px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-page { padding: 0 16px 32px; }
  .hero-section { padding: 40px 20px; }
  .hero-section h1 { font-size: 32px; }
  .hero-buttons { flex-direction: column; align-items: center; }
  .section-header h2 { font-size: 22px; }
  .article-grid { grid-template-columns: 1fr; }
  .card-grid {
    grid-template-columns: 1fr;
  }
  .chat-fab {
    bottom: 20px;
    right: 20px;
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
}
</style>