<template>
  <div class="article-create">
    <a-card :bordered="false">
      <template #title>
        <div class="header">
          <a-button type="text" @click="goBack">
            ← 返回
          </a-button>
          <span>{{ isEdit ? '编辑文章' : '发布新文章' }}</span>
        </div>
      </template>

      <a-form :model="articleForm" layout="vertical">
        <a-form-item label="文章标题" required>
          <a-input 
            v-model="articleForm.title" 
            placeholder="请输入文章标题"
            :max-length="100"
            show-word-limit
          />
        </a-form-item>

        <a-form-item label="文章分类" required>
          <a-select v-model="articleForm.category" placeholder="请选择分类">
            <a-option value="mental">心理健康</a-option>
            <a-option value="emotion">情绪管理</a-option>
            <a-option value="relationship">人际关系</a-option>
            <a-option value="career">职场心理</a-option>
            <a-option value="parenting">亲子教育</a-option>
            <a-option value="growth">自我成长</a-option>
          </a-select>
        </a-form-item>

        <a-form-item label="封面图片">
          <a-upload
            :action="uploadUrl"
            :show-file-list="false"
            @success="handleUploadSuccess"
          >
            <div class="cover-upload">
              <img v-if="articleForm.coverImage" :src="articleForm.coverImage" class="cover-preview" />
              <div v-else class="upload-placeholder">
                ➕ 点击上传封面图
              </div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item label="文章摘要" required>
          <a-textarea 
            v-model="articleForm.summary" 
            placeholder="请输入文章摘要，会显示在列表页"
            :rows="3"
            :max-length="200"
            show-word-limit
          />
        </a-form-item>

        <a-form-item label="文章内容" required>
          <a-textarea 
            v-model="articleForm.content" 
            placeholder="请输入文章内容"
            :rows="10"
            :max-length="5000"
            show-word-limit
          />
        </a-form-item>

        <a-form-item label="标签">
          <a-input-tag 
            v-model="articleForm.tags" 
            placeholder="输入标签后按回车"
            allow-clear
          />
        </a-form-item>

        <a-form-item>
          <div class="form-actions">
            <a-button @click="saveAsDraft">保存草稿</a-button>
            <a-button type="primary" @click="publishArticle">发布文章</a-button>
          </div>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()
const isEdit = ref(false)

const articleForm = ref({
  title: '',
  category: 'mental',
  coverImage: '',
  summary: '',
  content: '',
  tags: []
})

const uploadUrl = '/api/upload/image'

const goBack = () => {
  router.back()
}

const handleUploadSuccess = (res) => {
  articleForm.value.coverImage = res.data.url
  Message.success('上传成功')
}

// 保存草稿
const saveAsDraft = () => {
  if (!articleForm.value.title) {
    Message.warning('请输入文章标题')
    return
  }
  
  // 获取现有草稿
  const saved = localStorage.getItem('news_articles_drafts')
  const drafts = saved ? JSON.parse(saved) : []
  
  // 创建草稿
  const draftArticle = {
    id: Date.now(),
    title: articleForm.value.title,
    summary: articleForm.value.summary || articleForm.value.content.substring(0, 100),
    category: articleForm.value.category,
    cover: articleForm.value.coverImage || 'https://picsum.photos/id/20/400/300',
    content: articleForm.value.content,
    tags: articleForm.value.tags,
    createTime: new Date().toISOString().split('T')[0],
    status: 'draft'
  }
  
  drafts.unshift(draftArticle)
  localStorage.setItem('news_articles_drafts', JSON.stringify(drafts))
  
  Message.success('已保存到草稿箱')
  router.back()
}

// 发布文章
const publishArticle = () => {
  if (!articleForm.value.title) {
    Message.warning('请输入文章标题')
    return
  }
  if (!articleForm.value.content) {
    Message.warning('请输入文章内容')
    return
  }
  
  // 获取现有文章
  const saved = localStorage.getItem('news_articles')
  const articles = saved ? JSON.parse(saved) : []
  
  // 创建新文章
  const newArticle = {
    id: Date.now(),
    title: articleForm.value.title,
    summary: articleForm.value.summary || articleForm.value.content.substring(0, 100),
    category: articleForm.value.category,
    cover: articleForm.value.coverImage || 'https://picsum.photos/id/20/400/300',
    createTime: new Date().toISOString().split('T')[0],
    viewCount: 0,
    commentCount: 0,
    content: articleForm.value.content,
    tags: articleForm.value.tags
  }
  
  // 保存到 localStorage
  articles.unshift(newArticle)
  localStorage.setItem('news_articles', JSON.stringify(articles))
  
  Message.success('发布成功')
  router.push('/news')
}

onMounted(() => {
  if (route.params.id) {
    isEdit.value = true
    // 加载文章数据（从 localStorage 或后端）
    const saved = localStorage.getItem('news_articles')
    const articles = saved ? JSON.parse(saved) : []
    const article = articles.find(a => a.id == route.params.id)
    if (article) {
      articleForm.value = {
        title: article.title,
        category: article.category,
        coverImage: article.cover,
        summary: article.summary,
        content: article.content,
        tags: article.tags || []
      }
    }
  }
})
</script>

<style scoped>
.article-create {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cover-upload {
  width: 300px;
  height: 180px;
  border: 1px dashed #e5e6eb;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86909c;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}
</style>