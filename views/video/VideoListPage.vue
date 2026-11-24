<template>
  <div class="video-list-page">
    <div class="page-header">
      <h1>心理健康视频</h1>
      <p>大学生心理健康知识科普，助你了解心理健康，提升自我认知</p>
    </div>

    <!-- 视频网格 - 每页6个 -->
    <div class="video-grid">
      <div 
        v-for="video in paginatedVideos" 
        :key="video.id" 
        class="video-card"
        @click="goToDetail(video.id)"
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

    <!-- 空状态 -->
    <div v-if="videoList.length === 0" class="empty-state">
      <p>暂无视频</p>
    </div>

    <!-- 分页组件 - 只有超过6个视频才显示 -->
    <div v-if="videoList.length > 6" class="pagination-wrapper">
      <button class="page-btn prev" @click="prevPage" :disabled="currentPage === 1">
        ◀ 上一页
      </button>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <button class="page-btn next" @click="nextPage" :disabled="currentPage === totalPages">
        下一页 ▶
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { videoListData, VideoItem, loadViewCounts } from '@/config/videos'

const router = useRouter()
const videoList = ref<VideoItem[]>([])

// 每页显示6个
const PAGE_SIZE = 6
const currentPage = ref(1)

// 总页数
const totalPages = computed(() => {
  return Math.ceil(videoList.value.length / PAGE_SIZE)
})

// 当前页显示的视频（分页）
const paginatedVideos = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  const end = start + PAGE_SIZE
  return videoList.value.slice(start, end)
})

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 格式化时长
const formatDuration = (seconds: number) => {
  if (!seconds) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 格式化观看次数
const formatViewCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  return count.toString()
}

// 图片加载失败时的处理
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/300x169?text=视频封面'
}

// 跳转详情
const goToDetail = (id: number) => {
  router.push(`/video/detail/${id}`)
}

onMounted(() => {
  loadViewCounts()
  videoList.value = videoListData
  console.log('视频总数:', videoList.value.length)
  console.log('总页数:', totalPages.value)
})
</script>

<style scoped>
.video-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #2c5f2d;
  margin-bottom: 12px;
}

.page-header p {
  font-size: 16px;
  color: #8ba888;
}

/* 视频网格 - 一行3个 */
.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-height: 520px;
}

/* 视频卡片 */
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
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: #999;
}

/* 分页样式 */
.pagination-wrapper {
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

@media (max-width: 600px) {
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style>