<template>
  <div id="app">
    <GlobalHeader />
    <router-view />
    <!-- 全局音乐悬浮球 -->
    <MusicPlayer />
    <!-- 全局播客播放器 -->
    <GlobalAudioPlayer />
    <!-- 添加 AI 悬浮球 -->
    <AIFloat />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import MusicPlayer from '@/components/MusicPlayer.vue'
import GlobalAudioPlayer from '@/components/GlobalAudioPlayer.vue'
import AIFloat from '@/components/AIFloat.vue'

const route = useRoute()

const doInit = () => {
  // 初始化代码
}

onMounted(() => {
  doInit()
})
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6f5 100%);
}

#app {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6f5 100%);
}
</style>