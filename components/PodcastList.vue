<template>
  <div class="podcast-list">
    <h2>心理播客</h2>
    <div class="podcast-items">
      <div v-for="podcast in podcasts" :key="podcast.id" class="podcast-item">
        <h3>{{ podcast.title }}</h3>
        <p>{{ podcast.content }}</p>
        <div class="podcast-info">
          <span class="duration">{{ formatDuration(podcast.duration) }}</span>
          <button @click="playPodcast(podcast)" class="play-btn">播放</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      podcasts: []
    };
  },
  mounted() {
    this.fetchPodcasts();
  },
  methods: {
    fetchPodcasts() {
      fetch('/api/resource/podcast/list')
        .then(response => response.json())
        .then(data => {
          if (data.code === 200) {
            this.podcasts = data.data;
          }
        });
    },
    formatDuration(seconds) {
      const minutes = Math.floor(seconds / 60);
      const remainingSeconds = seconds % 60;
      return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
    },
    playPodcast(podcast) {
      // 跳转到播客播放页面
      this.$router.push(`/podcast/${podcast.id}`);
    }
  }
};
</script>

<style scoped>
.podcast-list {
  padding: 20px;
}
.podcast-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.podcast-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  background-color: #f9f9f9;
}
.podcast-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.duration {
  color: #666;
  font-size: 14px;
}
.play-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
.play-btn:hover {
  background-color: #45a049;
}
</style>
