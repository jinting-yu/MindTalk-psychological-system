<template>
  <div class="music-player-wrapper">
    <div class="music-float-ball" @click="togglePlayer" :class="{ playing: isPlaying }">
      <div class="ball-icon">
        <span v-if="!isPlaying">🎵</span>
        <span v-else class="rotating">🎶</span>
      </div>
      
      <!-- 展开的播放器面板 -->
      <div class="music-panel" v-show="showPanel" @click.stop>
        <div class="panel-header">
          <span>🎧 放松音乐</span>
          <span class="close-btn" @click="showPanel = false">✕</span>
        </div>
        
        <div class="music-info">
          <div class="music-title">{{ currentMusic.title }}</div>
          <div class="music-artist">{{ currentMusic.artist }}</div>
        </div>
        
        <div class="progress-bar" @click="handleProgressClick">
          <div class="progress" :style="{ width: progressPercent + '%' }"></div>
        </div>
        
        <div class="controls">
          <button @click="prevMusic" class="control-btn">⏮️</button>
          <button @click="togglePlay" class="control-btn play-btn">
            {{ isPlaying ? '⏸️' : '▶️' }}
          </button>
          <button @click="nextMusic" class="control-btn">⏭️</button>
        </div>
        
        <div class="volume-control">
          <span>🔊</span>
          <input type="range" v-model="volume" min="0" max="1" step="0.01" @change="changeVolume">
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import kano from '@/assets/卡农_爱给网_aigei_com.mp3'
import xiju from '@/assets/喜剧之王.mp3'

// 音乐列表
const musicList = [
  { title: '卡农', artist: 'Pachelbel', url: kano },
  // 如果你有其他本地歌曲，可以继续添加：
   { title: '喜剧之王', artist: 'Bach', url: xiju },
  // { title: '月光奏鸣曲', artist: 'Beethoven', url: moonlight },
  // 暂时只播放这一首歌，或者你可以添加更多
]


const showPanel = ref(false)
const isPlaying = ref(false)
const volume = ref(0.5)
const currentMusicIndex = ref(0)
const progressPercent = ref(0)

let audio: HTMLAudioElement | null = null

const currentMusic = computed(() => musicList[currentMusicIndex.value])

const togglePlayer = () => {
  showPanel.value = !showPanel.value
}

const togglePlay = () => {
  if (!audio) return
  
  if (isPlaying.value) {
    audio.pause()
    isPlaying.value = false
  } else {
    audio.play()
    isPlaying.value = true
  }
}

const nextMusic = () => {
  currentMusicIndex.value = (currentMusicIndex.value + 1) % musicList.length
  if (audio) {
    audio.src = currentMusic.value.url
    if (isPlaying.value) {
      audio.play()
    }
  }
}

const prevMusic = () => {
  currentMusicIndex.value = (currentMusicIndex.value - 1 + musicList.length) % musicList.length
  if (audio) {
    audio.src = currentMusic.value.url
    if (isPlaying.value) {
      audio.play()
    }
  }
}

const changeVolume = () => {
  if (audio) {
    audio.volume = volume.value
  }
}

const updateProgress = () => {
  if (audio && audio.duration) {
    progressPercent.value = (audio.currentTime / audio.duration) * 100
  }
}

const handleProgressClick = (event: MouseEvent) => {
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect()
  const x = event.clientX - rect.left
  const percent = x / rect.width
  if (audio) {
    audio.currentTime = percent * audio.duration
  }
}

onMounted(() => {
  audio = new Audio()
  audio.volume = volume.value
  audio.loop = false
  audio.src = currentMusic.value.url
  
  audio.addEventListener('timeupdate', updateProgress)
  audio.addEventListener('ended', nextMusic)
})

onBeforeUnmount(() => {
  if (audio) {
    audio.pause()
    audio.removeEventListener('timeupdate', updateProgress)
    audio = null
  }
})
</script>

<style scoped>
.music-player-wrapper {
  position: relative;
}

.music-float-ball {
  position: fixed;
  top: 80px;
  left: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #9bcd9c 0%, #7cb87a 100%);
  box-shadow: 0 4px 15px rgba(124, 184, 122, 0.3);
  cursor: pointer;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.music-float-ball:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(124, 184, 122, 0.4);
}

.ball-icon {
  font-size: 28px;
}

.rotating {
  animation: rotate 2s linear infinite;
  display: inline-block;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.music-panel {
  position: absolute;
  top: 70px;
  left: 0;
  width: 280px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  padding: 15px;
  animation: slideUp 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(155, 205, 156, 0.3);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: bold;
  color: #2c5f2d;
}

.close-btn {
  cursor: pointer;
  color: #9bcd9c;
  font-size: 18px;
}

.close-btn:hover {
  color: #7cb87a;
}

.music-info {
  text-align: center;
  margin-bottom: 12px;
}

.music-title {
  font-size: 14px;
  font-weight: bold;
  color: #2c5f2d;
}

.music-artist {
  font-size: 12px;
  color: #8ba888;
  margin-top: 4px;
}

.progress-bar {
  height: 4px;
  background: #e8ecd9;
  border-radius: 2px;
  cursor: pointer;
  margin: 12px 0;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #9bcd9c, #7cb87a);
  border-radius: 2px;
  width: 0%;
}

.controls {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 15px 0;
}

.control-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: white;
  color: #7cb87a;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.control-btn:hover {
  background: #f6fbf4;
  transform: scale(1.05);
  color: #5a9e5b;
}

.play-btn {
  background: white;
  color: #5a9e5b;
  width: 48px;
  height: 48px;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(124, 184, 122, 0.3);
}

.play-btn:hover {
  background: white;
  transform: scale(1.08);
  box-shadow: 0 6px 16px rgba(124, 184, 122, 0.4);
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #5a7c5b;
}

.volume-control input {
  flex: 1;
  height: 4px;
  -webkit-appearance: none;
  background: #e8ecd9;
  border-radius: 2px;
  outline: none;
}

.volume-control input::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #7cb87a;
  cursor: pointer;
}
</style>