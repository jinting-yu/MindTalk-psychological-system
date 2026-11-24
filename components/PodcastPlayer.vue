<template>
  <div class="podcast-player">
    <div v-if="podcast" class="player-content">
      <h2>{{ podcast.title }}</h2>
      <p>{{ podcast.content }}</p>
      <audio :src="podcast.audioUrl" controls autoplay>
        您的浏览器不支持音频播放。
      </audio>
      <div class="player-info">
        <span>时长: {{ formatDuration(podcast.duration) }}</span>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      podcast: null
    };
  },
  mounted() {
    this.fetchPodcast();
  },
  methods: {
    fetchPodcast() {
      const id = this.$route.params.id;
      fetch(`/api/resource/podcast/${id}`)
        .then(response => response.json())
        .then(data => {
          if (data.code === 200) {
            this.podcast = data.data;
          }
        });
    },
    formatDuration(seconds) {
      const minutes = Math.floor(seconds / 60);
      const remainingSeconds = seconds % 60;
      return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.podcast-player {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.player-content {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
audio {
  width: 100%;
  margin: 20px 0;
}
.player-info {
  margin-top: 10px;
  color: #666;
}
.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}
</style>