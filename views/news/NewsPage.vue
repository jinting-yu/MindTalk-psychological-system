<template>
  <div class="news-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>心理资讯</h1>
      <p>了解心理健康知识，关注内心成长</p>
    </div>

    <!-- 操作按钮栏（仅老师/管理员可见） -->
    <div class="action-bar" v-if="isTeacherOrAdmin">
      <a-button type="primary" size="large" @click="goToCreateArticle">
        <template #icon><icon-plus /></template>
        发布文章
      </a-button>
      <a-button size="large" @click="goToManageArticles">
        📋 管理文章
      </a-button>
    </div>

    <!-- 标签页切换：文章 | 播客 | 视频 -->
    <div class="tabs-container">
      <a-tabs v-model:active-key="activeTab" type="rounded" @change="handleTabChange">
        <a-tab-pane key="articles" title="📖 心理文章">
          <!-- 搜索框 + 分类导航 -->
          <div class="search-and-category">
            <div class="search-wrapper">
              <a-input-search
                v-model="searchKeyword"
                placeholder="搜索文章标题、内容..."
                allow-clear
                size="large"
                :style="{ width: '320px' }"
                @search="handleSearch"
                @clear="handleClearSearch"
              />
            </div>
            <div class="category-nav">
              <a-button 
                v-for="cat in categories" 
                :key="cat.value"
                :type="activeCategory === cat.value ? 'primary' : 'outline'"
                @click="handleCategoryChange(cat.value)"
              >
                {{ cat.label }}
              </a-button>
            </div>
          </div>

          <!-- 资讯列表 -->
          <div class="news-list" v-loading="loading">
            <div v-for="news in paginatedArticles" :key="news.id" class="news-item" @click="viewDetail(news.id)">
              <div class="news-cover">
                <img :src="news.cover" :alt="news.title" />
              </div>
              <div class="news-content">
                <div class="news-category">
                  <a-tag :color="getCategoryColor(news.category)">{{ getCategoryName(news.category) }}</a-tag>
                </div>
                <h3>{{ news.title }}</h3>
                <p>{{ news.summary }}</p>
                <div class="news-meta">
                  <span><icon-calendar /> {{ news.createTime }}</span>
                  <span><icon-eye /> {{ news.viewCount }}阅读</span>
                  <span><icon-message /> {{ news.commentCount }}评论</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页组件 -->
          <div class="pagination-container" v-if="totalPages > 1">
            <a-pagination
              v-model:current="currentPage"
              :total="filteredNewsList.length"
              :page-size="pageSize"
              show-total
              show-jumper
              @change="handlePageChange"
            />
          </div>

          <!-- 空状态 -->
          <a-empty v-if="!loading && filteredNewsList.length === 0" description="暂无相关资讯" />
        </a-tab-pane>

        <a-tab-pane key="podcast" title="🎧 心理播客">
          <div class="podcast-section">
            <!-- 正在播放器 -->
            <div class="currently-playing" v-if="audioStore.currentPodcast">
              <div class="playing-header">
                <h3>🎵 正在播放</h3>
              </div>
              <div class="playing-card">
                <div class="podcast-cover">
                  <img :src="audioStore.currentPodcast.cover" :alt="audioStore.currentPodcast.title" />
                  <div class="playing-animation" v-if="audioStore.isPlaying">
                    <span></span><span></span><span></span>
                  </div>
                </div>
                <div class="podcast-info-playing">
                  <h4>{{ audioStore.currentPodcast.title }}</h4>
                  <p>{{ audioStore.currentPodcast.speaker }}</p>
                  <div class="player-controls">
                    <a-button class="control-btn" @click="audioStore.togglePlay">
                      <icon-play-arrow v-if="!audioStore.isPlaying" />
                      <icon-pause v-else />
                    </a-button>
                    <a-button class="control-btn" @click="toggleFavorite">
                      <icon-heart :style="{ color: isFavorite ? '#ff4d4f' : '#ccc' }" />
                    </a-button>
                    <a-button class="control-btn" @click="sharePodcast">
                      <icon-share-alt />
                    </a-button>
                  </div>
                  <div class="progress-bar">
                    <span>{{ audioStore.formatTime(audioStore.currentTime) }}</span>
                    <a-slider
                      :model-value="audioStore.currentTime"
                      :max="audioStore.currentDuration"
                      :style="{ flex: 1 }"
                      @change="handleSeek"
                    />
                    <span>{{ audioStore.formatTime(audioStore.currentDuration) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 播客列表 -->
            <div class="podcast-list">
              <h3>📻 推荐播客</h3>
              <div class="podcast-grid">
                <div 
                  v-for="podcast in podcastList" 
                  :key="podcast.id" 
                  class="podcast-card"
                  :class="{ active: audioStore.currentPodcast?.id === podcast.id && audioStore.isPlaying }"
                  @click="playPodcast(podcast)"
                >
                  <div class="podcast-cover-small">
                    <img :src="podcast.cover" :alt="podcast.title" />
                    <div class="play-overlay">
                      <icon-play-arrow />
                    </div>
                  </div>
                  <div class="podcast-info">
                    <h4>{{ podcast.title }}</h4>
                    <p>{{ podcast.speaker }}</p>
                    <div class="podcast-meta">
                      <span>🕐 {{ podcast.duration }}</span>
                      <span>🎧 {{ podcast.plays }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-tab-pane>

        <!-- 心理视频标签页 -->
        <a-tab-pane key="video" title="🎬 心理视频">
          <div class="video-section">
            <div class="video-grid">
              <div 
                v-for="video in paginatedVideoList" 
                :key="video.id" 
                class="video-card"
                @click="goToVideoDetail(video.id)"
              >
                <div class="video-cover">
                  <img 
                    :src="video.cover" 
                    :alt="video.title"
                    @error="handleImageError"
                  >
                  <div class="video-duration">{{ formatDuration(video.duration) }}</div>
                  <div class="play-icon">▶</div>
                </div>
                <div class="video-info">
                  <h3>{{ video.title }}</h3>
                  <p class="teacher">{{ video.teacher }}</p>
                  <div class="video-stats">
                    <span>👁️ {{ formatViewCount(video.viewCount) }}次播放</span>
                    <span>📅 {{ video.createTime }}</span>
                  </div>
                  <p class="description">{{ video.description }}</p>
                </div>
              </div>
            </div>

            <div v-if="videoList.length === 0" class="empty-state">
              <p>暂无视频</p>
            </div>

            <div v-if="videoList.length > 6" class="video-pagination">
              <button class="page-btn" @click="videoPrevPage" :disabled="videoCurrentPage === 1">
                ◀ 上一页
              </button>
              <span class="page-info">{{ videoCurrentPage }} / {{ videoTotalPages }}</span>
              <button class="page-btn" @click="videoNextPage" :disabled="videoCurrentPage === videoTotalPages">
                下一页 ▶
              </button>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { 
  IconPlus, 
  IconCalendar, 
  IconEye, 
  IconMessage, 
  IconPlayArrow, 
  IconPause, 
  IconHeart, 
  IconShareAlt 
} from '@arco-design/web-vue/es/icon'
import { useAudioStore } from '@/store/audioStore'
import { videoListData, VideoItem, loadViewCounts } from '@/config/videos'

const router = useRouter()
const route = useRoute()
const audioStore = useAudioStore()

// ==================== 角色判断 ====================
// 判断是否是老师或管理员（从 store 或 localStorage 获取）
const isTeacherOrAdmin = computed(() => {
  const userRole = localStorage.getItem('userRole') || sessionStorage.getItem('userRole')
  return userRole === 'COUNSELOR' || userRole === 'ADMIN' || userRole === 'teacher'
})

// ==================== 管理员功能 ====================
const goToCreateArticle = () => {
  router.push('/teacher/article/create')
}

const goToManageArticles = () => {
  router.push('/teacher/articles')
}

// ==================== 标签页状态 ====================
const activeTab = ref('articles')

// ==================== 文章相关 ====================
const loading = ref(false)
const activeCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(4)
const newsList = ref<any[]>([])
const isFavorite = ref(false)
const searchKeyword = ref('')

// 默认文章数据
const defaultArticles = [
  {
    id: 1,
    title: '如何识别焦虑症的早期信号？',
    summary: '焦虑症是现代人常见的心理问题，了解早期信号有助于及时干预。本文详细介绍焦虑症的10个早期信号...',
    category: 'mental',
    cover: 'https://picsum.photos/id/20/400/300',
    createTime: '2025-03-25',
    viewCount: 1250,
    commentCount: 32
  },
  {
    id: 2,
    title: 'MBTI性格测试结果解读指南',
    summary: '16种人格类型各有特点，找到适合自己的发展路径。本文将详细解读每种性格类型的优势和适合职业...',
    category: 'growth',
    cover: 'https://picsum.photos/id/26/400/300',
    createTime: '2025-03-24',
    viewCount: 3420,
    commentCount: 128
  },
  {
    id: 3,
    title: '建立健康人际关系的5个关键',
    summary: '无论是亲密关系还是职场关系，这5个要素都至关重要。学会沟通、尊重、信任、支持和边界...',
    category: 'relationship',
    cover: 'https://picsum.photos/id/100/400/300',
    createTime: '2025-03-23',
    viewCount: 2150,
    commentCount: 67
  },
  {
    id: 4,
    title: '职场焦虑自救指南：5个方法帮你找回平静',
    summary: '职场压力大、焦虑情绪难以缓解？本文为你介绍5个科学有效的应对方法，帮助你重新掌控情绪...',
    category: 'career',
    cover: 'https://picsum.photos/id/30/400/300',
    createTime: '2025-03-22',
    viewCount: 1890,
    commentCount: 56
  },
  {
    id: 5,
    title: '正念冥想：每天10分钟，改变你的生活',
    summary: '正念冥想被证明能有效缓解压力、提升专注力，让我们从每天10分钟开始，感受内心的平静...',
    category: 'mental',
    cover: 'https://picsum.photos/id/38/400/300',
    createTime: '2025-03-21',
    viewCount: 2340,
    commentCount: 89
  },
  {
    id: 6,
    title: '大学生心理健康指南：如何度过迷茫期',
    summary: '大学时期是人生的重要阶段，面对学业、就业、人际等多重压力，如何保持心理健康？...',
    category: 'mental',
    cover: 'https://picsum.photos/id/0/400/300',
    createTime: '2025-03-20',
    viewCount: 3120,
    commentCount: 156
  },
  {
    id: 7,
    title: '情绪管理入门：识别并接纳你的情绪',
    summary: '情绪没有好坏之分，关键在于如何管理。学会识别和接纳情绪，是情绪管理的第一步...',
    category: 'emotion',
    cover: 'https://picsum.photos/id/85/400/300',
    createTime: '2025-03-19',
    viewCount: 2780,
    commentCount: 94
  },
  {
    id: 8,
    title: '亲子沟通的艺术：如何与孩子有效对话',
    summary: '良好的亲子沟通是家庭和谐的基石，本文分享几个实用的亲子沟通技巧...',
    category: 'parenting',
    cover: 'https://picsum.photos/id/44/400/300',
    createTime: '2025-03-18',
    viewCount: 1560,
    commentCount: 43
  }
]

// 文章数据（支持从 localStorage 读取，实现动态增删改）
const allArticles = ref<any[]>([])

// 加载文章
const loadArticles = () => {
  const saved = localStorage.getItem('news_articles')
  if (saved && JSON.parse(saved).length > 0) {
    allArticles.value = JSON.parse(saved)
    console.log('从 localStorage 加载文章:', allArticles.value.length)
  } else {
    allArticles.value = [...defaultArticles]
    localStorage.setItem('news_articles', JSON.stringify(allArticles.value))
    console.log('初始化默认文章:', allArticles.value.length)
  }
}

// 保存文章到 localStorage
const saveArticles = () => {
  localStorage.setItem('news_articles', JSON.stringify(allArticles.value))
}

// 监听文章变化，自动保存
watch(allArticles, () => {
  saveArticles()
}, { deep: true })

// ==================== 播客数据 ====================
const podcastList = ref([
  {
    id: 1,
    title: '正念冥想 - 缓解焦虑',
    speaker: '李老师',
    cover: 'https://picsum.photos/id/38/200/200',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    duration: '15:30',
    plays: '2.3w',
    description: '通过正念冥想练习，帮助你缓解焦虑情绪'
  },
  {
    id: 2,
    title: '情绪管理小课堂',
    speaker: '王老师',
    cover: 'https://picsum.photos/id/85/200/200',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    duration: '12:45',
    plays: '1.8w',
    description: '学习识别和管理自己的情绪'
  },
  {
    id: 3,
    title: '睡眠改善指南',
    speaker: '张老师',
    cover: 'https://picsum.photos/id/20/200/200',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    duration: '18:20',
    plays: '3.1w',
    description: '改善睡眠质量的实用技巧'
  },
  {
    id: 4,
    title: '人际关系心理学',
    speaker: '陈老师',
    cover: 'https://picsum.photos/id/100/200/200',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
    duration: '20:15',
    plays: '2.7w',
    description: '建立健康人际关系的方法'
  },
  {
    id: 5,
    title: '压力应对技巧',
    speaker: '刘老师',
    cover: 'https://picsum.photos/id/30/200/200',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
    duration: '14:50',
    plays: '1.5w',
    description: '科学应对压力的方法'
  },
  {
    id: 6,
    title: '积极心理学',
    speaker: '赵老师',
    cover: 'https://picsum.photos/id/26/200/200',
    audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
    duration: '16:40',
    plays: '2.1w',
    description: '培养幸福感的方法'
  }
])

// ==================== 分类配置 ====================
const categories = [
  { label: '全部', value: 'all' },
  { label: '心理健康', value: 'mental' },
  { label: '情绪管理', value: 'emotion' },
  { label: '人际关系', value: 'relationship' },
  { label: '职场心理', value: 'career' },
  { label: '亲子教育', value: 'parenting' },
  { label: '自我成长', value: 'growth' }
]

// ==================== 计算属性 ====================
// 筛选后的文章列表
const filteredNewsList = computed(() => {
  let filtered = [...allArticles.value]
  
  if (activeCategory.value !== 'all') {
    filtered = filtered.filter(item => item.category === activeCategory.value)
  }
  
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    filtered = filtered.filter(item => 
      item.title.toLowerCase().includes(keyword) || 
      item.summary.toLowerCase().includes(keyword)
    )
  }
  
  return filtered
})

// 分页后的文章列表
const paginatedArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredNewsList.value.slice(start, end)
})

// 总页数
const totalPages = computed(() => {
  return Math.ceil(filteredNewsList.value.length / pageSize.value)
})

// ==================== 视频相关 ====================
const videoList = ref<VideoItem[]>([])
const VIDEO_PAGE_SIZE = 6
const videoCurrentPage = ref(1)

const videoTotalPages = computed(() => {
  return Math.ceil(videoList.value.length / VIDEO_PAGE_SIZE)
})

const paginatedVideoList = computed(() => {
  const start = (videoCurrentPage.value - 1) * VIDEO_PAGE_SIZE
  const end = start + VIDEO_PAGE_SIZE
  return videoList.value.slice(start, end)
})

const videoPrevPage = () => {
  if (videoCurrentPage.value > 1) {
    videoCurrentPage.value--
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const videoNextPage = () => {
  if (videoCurrentPage.value < videoTotalPages.value) {
    videoCurrentPage.value++
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// ==================== 方法 ====================
const handleSearch = () => {
  currentPage.value = 1
}

const handleClearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const getCategoryName = (value: string) => {
  const cat = categories.find(c => c.value === value)
  return cat?.label || value
}

const getCategoryColor = (value: string) => {
  const colorMap: Record<string, string> = {
    mental: 'blue',
    emotion: 'orange',
    relationship: 'green',
    career: 'purple',
    parenting: 'red',
    growth: 'cyan'
  }
  return colorMap[value] || 'gray'
}

const handleTabChange = (key: string) => {
  console.log('切换到:', key)
  if (key === 'video') {
    videoCurrentPage.value = 1
  }
}

const handleCategoryChange = (category: string) => {
  activeCategory.value = category
  currentPage.value = 1
  router.replace({
    path: '/news',
    query: { category: category !== 'all' ? category : undefined }
  })
}

const viewDetail = (id: number) => {
  router.push(`/news/detail/${id}`)
}

// 播客功能
const playPodcast = (podcast: any) => {
  audioStore.playPodcast(podcast)
}

const handleSeek = (value: number) => {
  audioStore.seekTo(value)
}

const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  Message.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
}

const sharePodcast = () => {
  Message.success('分享链接已复制')
}

// 视频功能
const formatDuration = (seconds: number) => {
  if (!seconds) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const formatViewCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  return count.toString()
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/300x169?text=视频封面'
}

const goToVideoDetail = (id: number) => {
  router.push(`/video/detail/${id}`)
}

const getCategoryFromUrl = () => {
  const categoryParam = route.query.category as string
  if (categoryParam && categories.some(c => c.value === categoryParam)) {
    activeCategory.value = categoryParam
  }
}

// ==================== 监听器 ====================
watch(() => route.query.category, (newCategory) => {
  if (newCategory && categories.some(c => c.value === newCategory)) {
    activeCategory.value = newCategory as string
  } else if (!newCategory) {
    activeCategory.value = 'all'
  }
})

// ==================== 生命周期 ====================
onMounted(() => {
  loadArticles()      // 从 localStorage 加载文章
  getCategoryFromUrl()
  loadViewCounts()
  videoList.value = videoListData
  console.log('视频总数:', videoList.value.length)
})
</script>

<style scoped>
.news-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #1e293b;
}

.page-header p {
  color: #64748b;
  font-size: 14px;
}

/* 操作按钮栏 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-bottom: 20px;
}

/* 标签页样式 */
.tabs-container {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
}

:deep(.arco-tabs-nav) {
  margin-bottom: 24px;
}

:deep(.arco-tabs-tab) {
  font-size: 16px;
  padding: 8px 20px;
}

/* 搜索框和分类导航布局 */
.search-and-category {
  margin-bottom: 32px;
}

.search-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

/* 文章列表 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 600px;
}

.news-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.news-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  background: #fff;
}

.news-cover {
  width: 200px;
  height: 140px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
}

.news-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.news-item:hover .news-cover img {
  transform: scale(1.05);
}

.news-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-category {
  margin-bottom: 12px;
}

.news-content h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #1e293b;
  line-height: 1.4;
}

.news-content p {
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  display: flex;
  gap: 20px;
  color: #94a3b8;
  font-size: 13px;
  margin-top: auto;
}

.news-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页组件样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px 0;
}

:deep(.arco-pagination) .arco-pagination-item-active {
  background: linear-gradient(135deg, #7eb09e 0%, #6b8c7c 100%);
  border-color: #7eb09e;
}

:deep(.arco-pagination) .arco-pagination-item-active a {
  color: white;
}

/* 播客样式 */
.podcast-section {
  margin-top: 20px;
}

.currently-playing {
  margin-bottom: 40px;
  padding: 20px;
  background: linear-gradient(135deg, #9bcd9c 0%, #7cb87a 100%);
  border-radius: 20px;
  color: white;
}

.playing-header h3 {
  margin-bottom: 16px;
  font-size: 18px;
}

.playing-card {
  display: flex;
  gap: 24px;
  align-items: center;
}

.podcast-cover {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 16px;
  overflow: hidden;
}

.podcast-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.playing-animation {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 4px;
}

.playing-animation span {
  width: 4px;
  height: 16px;
  background: white;
  border-radius: 2px;
  animation: playing 0.8s ease infinite;
}

.playing-animation span:nth-child(2) {
  animation-delay: 0.2s;
}

.playing-animation span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes playing {
  0%, 100% { height: 8px; }
  50% { height: 20px; }
}

.podcast-info-playing {
  flex: 1;
}

.podcast-info-playing h4 {
  font-size: 20px;
  margin-bottom: 8px;
}

.podcast-info-playing p {
  color: rgba(255,255,255,0.8);
  margin-bottom: 16px;
}

.player-controls {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.control-btn {
  background: rgba(255,255,255,0.9);
  border: none;
  color: #7cb87a;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 18px;
}

.control-btn:hover {
  background: white;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.progress-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar span {
  font-size: 12px;
  color: rgba(255,255,255,0.9);
}

:deep(.arco-slider-track) {
  background: rgba(255,255,255,0.3);
}

:deep(.arco-slider-track-bar) {
  background: white;
}

:deep(.arco-slider-handle) {
  background: white;
  border-color: white;
}

.podcast-list h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #1e293b;
}

.podcast-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.podcast-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e8ecd9;
}

.podcast-card:hover,
.podcast-card.active {
  background: linear-gradient(135deg, #9bcd9c 0%, #7cb87a 100%);
  color: white;
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(124, 184, 122, 0.3);
}

.podcast-card:hover .podcast-info p,
.podcast-card.active .podcast-info p {
  color: rgba(255,255,255,0.8);
}

.podcast-card:hover .podcast-meta,
.podcast-card.active .podcast-meta {
  color: rgba(255,255,255,0.7);
}

.podcast-cover-small {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.podcast-cover-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.podcast-card:hover .play-overlay {
  opacity: 1;
}

.podcast-info {
  flex: 1;
}

.podcast-info h4 {
  font-size: 16px;
  margin-bottom: 4px;
  color: #2c5f2d;
}

.podcast-card.active .podcast-info h4 {
  color: white;
}

.podcast-info p {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.podcast-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #9bcd9c;
}

/* 视频样式 */
.video-section {
  margin-top: 20px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-height: 520px;
}

.video-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8ecd9;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
  border-color: #9bcd9c;
}

.video-card:hover .play-icon {
  opacity: 1;
  transform: scale(1);
}

.video-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: #1a1a2e;
}

.video-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.video-card:hover .video-cover img {
  transform: scale(1.05);
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0.8);
  width: 44px;
  height: 44px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  opacity: 0;
  transition: all 0.3s ease;
}

.video-info {
  padding: 14px;
}

.video-info h3 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px;
  color: #2c5f2d;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-info .teacher {
  font-size: 12px;
  color: #7cb87a;
  margin: 0 0 8px;
}

.video-stats {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.video-info .description {
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: #999;
}

.video-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  margin-top: 40px;
  padding: 20px 0;
}

.page-btn {
  background: linear-gradient(135deg, #7eb09e 0%, #6b8c7c 100%);
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(126, 176, 158, 0.4);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 16px;
  color: #5a6e66;
  font-weight: 500;
  padding: 8px 20px;
  background: #f5f7f5;
  border-radius: 24px;
}

/* 响应式 */
@media (max-width: 900px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .news-page {
    padding: 16px;
  }
  
  .action-bar {
    justify-content: center;
  }
  
  .search-wrapper {
    justify-content: center;
  }
  
  .search-wrapper .arco-input-search {
    width: 100% !important;
  }
  
  .news-item {
    flex-direction: column;
  }
  
  .news-cover {
    width: 100%;
    height: 160px;
  }
  
  .playing-card {
    flex-direction: column;
    text-align: center;
  }
  
  .progress-bar {
    flex-wrap: wrap;
  }
  
  .podcast-grid,
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style>