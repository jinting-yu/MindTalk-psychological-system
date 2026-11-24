<template> 
   <div class="blog-list-page"> 
     <h1 class="page-title">心理博客</h1> 
     <div class="blog-list" v-if="blogs.length > 0"> 
       <div v-for="blog in blogs" :key="blog.id" class="blog-card"> 
         <div class="blog-card-img">📝</div> 
         <div class="blog-card-content"> 
           <h3 class="blog-card-title">{{ blog.title }}</h3> 
           <p class="blog-card-content-full">{{ blog.content }}</p> 
           <div class="blog-card-meta"> 
             <span>发布时间: {{ formatDate(blog.createTime) }}</span> 
             <span v-if="blog.duration">音频时长: {{ formatDuration(blog.duration) }}</span> 
           </div> 
           <div v-if="blog.audioUrl" class="audio-player"> 
             <PodcastPlayer :audioUrl="blog.audioUrl" :duration="blog.duration" /> 
           </div> 
         </div> 
       </div> 
     </div> 
     <div v-else class="loading"> 
       <p>加载中...</p> 
     </div> 
   </div> 
 </template> 


 <script lang="ts"> 
 import { defineComponent, ref, onMounted } from 'vue'; 
 import { blogController } from '@/api'; 
 import PodcastPlayer from '@/components/PodcastPlayer.vue'; 


 export default defineComponent({ 
   name: 'BlogListPage', 
   components: { 
     PodcastPlayer 
   }, 
   setup() { 
     const blogs = ref<any[]>([]); 


     onMounted(() => { 
       loadBlogs(); 
     }); 


     const loadBlogs = async () => { 
       try { 
         const response = await blogController.getBlogList(); 
         console.log('API 响应:', response); // 添加日志
         if (response.data.code === 200) { 
           blogs.value = response.data.data; 
         } 
       } catch (error) { 
         console.error('获取博客列表失败:', error); 
       } 
     }; 

     const formatDate = (dateString: string) => {
       const date = new Date(dateString);
       return date.toLocaleString('zh-CN', {
         year: 'numeric',
         month: '2-digit',
         day: '2-digit',
         hour: '2-digit',
         minute: '2-digit'
       });
     };

     const formatDuration = (seconds: number) => {
       const minutes = Math.floor(seconds / 60);
       const remainingSeconds = seconds % 60;
       return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
     };

     return {
       blogs,
       formatDate,
       formatDuration
     };
   } 
 }); 
 </script> 

<style scoped>
.blog-list-page {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  text-align: center;
  color: #4a6fa5;
  margin-bottom: 40px;
}

.blog-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.blog-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  display: flex;
  flex-direction: column;
}

.blog-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.blog-card-img {
  width: 100%;
  height: 150px;
  background-color: #e0e7ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: #4a6fa5;
}

.blog-card-content {
  padding: 20px;
  flex: 1;
}

.blog-card-title {
  font-size: 1.3rem;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.blog-card-content-full {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}

.blog-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: #999;
  margin-bottom: 20px;
}

.audio-player {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.loading {
  text-align: center;
  padding: 50px;
  color: #999;
}
</style>