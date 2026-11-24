<template>
  <div v-if="audioStore.currentPodcast" class="global-audio-player" :class="{ minimized: isMinimized }">
    <!-- 最小化模式 -->
    <div v-if="isMinimized" class="minimized-player" @click="toggleMinimize">
      <div class="minimized-cover">
        <img :src="audioStore.currentPodcast.cover" :alt="audioStore.currentPodcast.title" />
        <div class="playing-wave" v-if="audioStore.isPlaying">
          <span></span><span></span><span></span>
        </div>
      </div>
      <div class="minimized-info">
        <h4>{{ audioStore.currentPodcast.title }}</h4>
        <p>{{ audioStore.currentPodcast.speaker }}</p>
      </div>
      <div class="minimized-controls">
        <button class="control-btn" @click.stop="audioStore.togglePlay">
          {{ audioStore.isPlaying ? '⏸️' : '▶️' }}
        </button>
        <button class="control-btn close-btn" @click.stop="closePlayer">✕</button>
      </div>
    </div>

    <!-- 展开模式 -->
    <div v-else class="expanded-player">
      <div class="player-content">
        <div class="player-cover">
          <img :src="audioStore.currentPodcast.cover" :alt="audioStore.currentPodcast.title" />
        </div>
        <div class="player-info">
          <h4>{{ audioStore.currentPodcast.title }}</h4>
          <p>{{ audioStore.currentPodcast.speaker }}</p>
        </div>
        <div class="player-controls">
          <button class="control-btn play-btn" @click="audioStore.togglePlay">
            {{ audioStore.isPlaying ? '⏸️' : '▶️' }}
          </button>
        </div>
        <div class="player-progress">
          <span>{{ audioStore.formatTime(audioStore.currentTime) }}</span>
          <input 
            type="range" 
            v-model="audioStore.currentTime"
            :max="audioStore.currentDuration"
            @input="handleSeek"
            class="progress-slider"
          />
          <span>{{ audioStore.formatTime(audioStore.currentDuration) }}</span>
        </div>
        <div class="player-actions">
          <button class="action-btn" @click="toggleMinimize">−</button>
          <button class="action-btn close-btn" @click="closePlayer">✕</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAudioStore } from '@/store/audioStore'

const audioStore = useAudioStore()
const isMinimized = ref(false)

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}

const closePlayer = () => {
  audioStore.pause()
  audioStore.currentPodcast.value = null
}

const handleSeek = (e: Event) => {
  const target = e.target as HTMLInputElement
  audioStore.seekTo(parseFloat(target.value))
}
</script>

<style scoped>
.global-audio-player {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  background: linear-gradient(135deg, #9bcd9c 0%, #7cb87a 100%);
  border-radius: 60px;
  box-shadow: 0 8px 24px rgba(124, 184, 122, 0.3);
  transition: all 0.3s ease;
}

.global-audio-player.minimized {
  border-radius: 50px;
}

.minimized-player {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px 8px 8px;
  cursor: pointer;
}

.minimized-cover {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
}

.minimized-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.playing-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 2px;
  padding: 4px;
  background: rgba(0, 0, 0, 0.5);
}

.playing-wave span {
  width: 3px;
  height: 8px;
  background: white;
  border-radius: 1px;
  animation: wave 0.8s ease infinite;
}

.playing-wave span:nth-child(2) { animation-delay: 0.2s; }
.playing-wave span:nth-child(3) { animation-delay: 0.4s; }

@keyframes wave {
  0%, 100% { height: 4px; }
  50% { height: 12px; }
}

.minimized-info {
  flex: 1;
}

.minimized-info h4 {
  font-size: 13px;
  margin: 0 0 2px;
  color: white;
}

.minimized-info p {
  font-size: 10px;
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
}

.minimized-controls {
  display: flex;
  gap: 8px;
}

.control-btn {
  background: white;
  border: none;
  color: #7cb87a;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px;
}

.control-btn:hover {
  transform: scale(1.05);
}

.close-btn:hover {
  background: #ff4d4f;
  color: white;
}

.expanded-player {
  width: 350px;
  padding: 12px 16px;
  border-radius: 20px;
}

.player-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.player-cover {
  width: 45px;
  height: 45px;
  border-radius: 12px;
  overflow: hidden;
}

.player-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.player-info {
  flex: 1;
}

.player-info h4 {
  font-size: 13px;
  margin: 0 0 2px;
  color: white;
}

.player-info p {
  font-size: 10px;
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
}

.player-controls {
  display: flex;
  align-items: center;
}

.play-btn {
  width: 32px;
  height: 32px;
  background: white;
  color: #7cb87a;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
}

.player-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-top: 8px;
}

.player-progress span {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.85);
}

.progress-slider {
  flex: 1;
  height: 4px;
  -webkit-appearance: none;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  outline: none;
}

.progress-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: white;
  cursor: pointer;
}

.player-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  background: rgba(255, 255, 255, 0.25);
  border: none;
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.4);
}

@media (max-width: 600px) {
  .expanded-player {
    width: calc(100vw - 40px);
  }
}
</style>