import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAudioStore = defineStore('audio', () => {
  // 状态
  const audioElement = ref<HTMLAudioElement | null>(null)
  const currentPodcast = ref<any>(null)
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const currentDuration = ref(0)
  
  // 初始化音频元素
  const initAudio = () => {
    if (!audioElement.value) {
      audioElement.value = new Audio()
      audioElement.value.addEventListener('timeupdate', () => {
        if (audioElement.value) {
          currentTime.value = audioElement.value.currentTime
        }
      })
      audioElement.value.addEventListener('loadedmetadata', () => {
        if (audioElement.value) {
          currentDuration.value = audioElement.value.duration
        }
      })
      audioElement.value.addEventListener('ended', () => {
        isPlaying.value = false
        currentTime.value = 0
      })
    }
  }
  
  // 播放播客
  const playPodcast = (podcast: any) => {
    initAudio()
    
    if (currentPodcast.value?.id === podcast.id && isPlaying.value) {
      pause()
    } else {
      currentPodcast.value = podcast
      if (audioElement.value) {
        audioElement.value.src = podcast.audioUrl
        audioElement.value.play()
        isPlaying.value = true
      }
    }
  }
  
  // 播放
  const play = () => {
    if (audioElement.value) {
      audioElement.value.play()
      isPlaying.value = true
    }
  }
  
  // 暂停
  const pause = () => {
    if (audioElement.value) {
      audioElement.value.pause()
      isPlaying.value = false
    }
  }
  
  // 切换播放/暂停
  const togglePlay = () => {
    if (isPlaying.value) {
      pause()
    } else {
      play()
    }
  }
  
  // 跳转进度
  const seekTo = (time: number) => {
    if (audioElement.value) {
      audioElement.value.currentTime = time
      currentTime.value = time
    }
  }
  
  // 格式化时间
  const formatTime = (seconds: number) => {
    if (isNaN(seconds)) return '00:00'
    const mins = Math.floor(seconds / 60)
    const secs = Math.floor(seconds % 60)
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }
  
  return {
    audioElement,
    currentPodcast,
    isPlaying,
    currentTime,
    currentDuration,
    playPodcast,
    play,
    pause,
    togglePlay,
    seekTo,
    formatTime,
    initAudio
  }
})