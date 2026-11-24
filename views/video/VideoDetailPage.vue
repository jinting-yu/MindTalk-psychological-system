<!-- views/video/VideoDetailPage.vue -->
<template>
  <div class="video-detail-page">
    <div class="back-btn" @click="goBack">
      ← 返回视频列表
    </div>
    
    <div class="video-container" v-if="video">
      <video 
        ref="videoPlayer"
        :src="video.url" 
        controls 
        autoplay
        class="video-player"
        controlsList="nodownload"
        @loadedmetadata="onVideoLoaded"
        @ended="onVideoEnded"
      >
        您的浏览器不支持视频播放
      </video>
      
      <div class="video-info">
        <h1>{{ video.title }}</h1>
        <div class="meta">
          <span class="teacher">👨‍🏫 {{ video.teacher }}</span>
          <span class="views">👁️ {{ formatViewCount(video.viewCount) }}次播放</span>
          <span class="duration">⏱️ {{ formatDuration(video.duration) }}</span>
          <span class="date">📅 {{ video.createTime }}</span>
        </div>
        
        <div class="description">
          <h3>视频简介</h3>
          <p>{{ video.description }}</p>
        </div>
        
        <div class="actions">
          <button class="action-btn like-btn" @click="handleLike" :class="{ liked: hasLiked }">
            <span>❤️</span> {{ hasLiked ? '已点赞' : '点赞' }} {{ likeCount > 0 ? likeCount : '' }}
          </button>
          <button class="action-btn share-btn" @click="handleShare">
            <span>📤</span> 分享
          </button>
        </div>
      </div>
    </div>

    <!-- 视频加载失败 -->
    <div v-else-if="!video && !loading" class="error-container">
      <div class="error-icon">⚠️</div>
      <h2>视频不存在</h2>
      <p>抱歉，该视频不存在或已被删除</p>
      <button class="back-home-btn" @click="goBack">返回列表</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getVideoById, incrementViewCount, VideoItem } from '@/config/videos'

const router = useRouter()
const route = useRoute()
const videoPlayer = ref<HTMLVideoElement | null>(null)
const video = ref<VideoItem | null>(null)
const loading = ref(true)
const likeCount = ref(0)
const hasLiked = ref(false)

// 显示提示消息（简单实现，不用ant-design-vue）
const showMessage = (msg: string, type: 'success' | 'error' | 'info' = 'info') => {
  // 创建一个临时提示
  const messageDiv = document.createElement('div')
  messageDiv.textContent = msg
  messageDiv.style.cssText = `
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    padding: 10px 20px;
    background: ${type === 'success' ? '#52c41a' : type === 'error' ? '#ff4d4f' : '#1890ff'};
    color: white;
    border-radius: 4px;
    z-index: 9999;
    font-size: 14px;
  `
  document.body.appendChild(messageDiv)
  setTimeout(() => {
    messageDiv.remove()
  }, 2000)
}

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

const goBack = () => {
  router.back()
}

const onVideoLoaded = () => {
  console.log('视频加载完成')
}

const onVideoEnded = () => {
  showMessage('视频播放完毕', 'info')
}

const handleLike = () => {
  if (hasLiked.value) {
    likeCount.value--
    hasLiked.value = false
    localStorage.setItem(`video_like_${video.value?.id}`, 'false')
    showMessage('已取消点赞', 'info')
  } else {
    likeCount.value++
    hasLiked.value = true
    localStorage.setItem(`video_like_${video.value?.id}`, 'true')
    localStorage.setItem(`video_like_count_${video.value?.id}`, likeCount.value.toString())
    showMessage('感谢点赞！', 'success')
  }
}

const handleShare = async () => {
  const url = window.location.href
  try {
    await navigator.clipboard.writeText(url)
    showMessage('视频链接已复制到剪贴板', 'success')
  } catch (err) {
    showMessage('复制失败，请手动复制', 'error')
  }
}

// 加载视频数据
const loadVideo = () => {
  loading.value = true
  const id = Number(route.params.id)
  const foundVideo = getVideoById(id)
  
  if (foundVideo) {
    video.value = foundVideo
    // 增加播放量
    incrementViewCount(id)
    
    // 从 localStorage 读取点赞状态和数量
    const liked = localStorage.getItem(`video_like_${id}`)
    const savedLikeCount = localStorage.getItem(`video_like_count_${id}`)
    
    hasLiked.value = liked === 'true'
    if (savedLikeCount) {
      likeCount.value = parseInt(savedLikeCount)
    }
  } else {
    video.value = null
  }
  
  loading.value = false
}

onMounted(() => {
  loadVideo()
})
</script>

<style scoped lang="scss">
.video-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  
  .back-btn {
    cursor: pointer;
    color: #7cb87a;
    margin-bottom: 20px;
    display: inline-block;
    font-size: 14px;
    padding: 8px 0;
    
    &:hover {
      color: #5a9e5b;
      transform: translateX(-4px);
    }
  }
  
  .video-container {
    background: #fff;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    
    .video-player {
      width: 100%;
      background: #000;
      outline: none;
    }
    
    .video-info {
      padding: 24px;
      
      h1 {
        font-size: 24px;
        margin-bottom: 16px;
        color: #2c5f2d;
      }
      
      .meta {
        display: flex;
        flex-wrap: wrap;
        gap: 24px;
        margin-bottom: 24px;
        padding-bottom: 16px;
        border-bottom: 1px solid #eee;
        
        span {
          font-size: 14px;
          color: #666;
        }
        
        .teacher {
          color: #7cb87a;
          font-weight: 500;
        }
      }
      
      .description {
        margin-bottom: 24px;
        
        h3 {
          font-size: 18px;
          margin-bottom: 12px;
          color: #333;
        }
        
        p {
          font-size: 15px;
          line-height: 1.8;
          color: #555;
        }
      }
      
      .actions {
        display: flex;
        gap: 16px;
        padding-top: 16px;
        border-top: 1px solid #eee;
        
        .action-btn {
          padding: 8px 24px;
          border: 1px solid #e0e0e0;
          background: #f8f9fa;
          border-radius: 20px;
          cursor: pointer;
          font-size: 14px;
          transition: all 0.3s;
          
          &:hover {
            background: #e9ecef;
            transform: translateY(-2px);
          }
          
          &.like-btn {
            &.liked {
              background: #ff6b6b;
              border-color: #ff6b6b;
              color: #fff;
            }
          }
        }
      }
    }
  }
  
  .error-container {
    text-align: center;
    padding: 60px 20px;
    background: #fff;
    border-radius: 16px;
    
    .error-icon {
      font-size: 64px;
      margin-bottom: 20px;
    }
    
    h2 {
      color: #333;
      margin-bottom: 12px;
    }
    
    p {
      color: #666;
      margin-bottom: 24px;
    }
    
    .back-home-btn {
      padding: 10px 24px;
      background: #7cb87a;
      color: #fff;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      
      &:hover {
        background: #5a9e5b;
      }
    }
  }
}
</style>