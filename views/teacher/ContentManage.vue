<template>
  <div class="content-manage">
    <div class="page-header">
      <h2>内容管理</h2>
      <p>管理心理资讯中的文章、博客和视频</p>
    </div>

    <!-- 类型切换标签 -->
    <div class="content-tabs">
      <a-tabs v-model:active-key="activeTab" @change="handleTabChange">
        <a-tab-pane key="article" title="📝 文章管理" />
        <a-tab-pane key="blog" title="📖 博客管理" />
        <a-tab-pane key="video" title="🎬 视频管理" />
      </a-tabs>
      <a-button type="primary" @click="openAddModal">
        <template #icon><icon-plus /></template>
        新增{{ getTypeName() }}
      </a-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-input-search
        v-model="searchKeyword"
        placeholder="搜索标题"
        style="width: 300px"
        @search="loadContentList"
        allow-clear
      />
      <a-select v-model="filterStatus" placeholder="全部状态" style="width: 120px" @change="loadContentList" allow-clear>
        <a-option value="">全部状态</a-option>
        <a-option value="published">已发布</a-option>
        <a-option value="draft">草稿</a-option>
      </a-select>
    </div>

    <!-- 内容列表 -->
    <a-table
      :columns="columns"
      :data="contentList"
      :loading="loading"
      :pagination="pagination"
      @page-change="handlePageChange"
      row-key="id"
    >
      <template #cover="{ record }">
        <img v-if="record.coverImage" :src="record.coverImage" class="cover-thumb" />
        <div v-else class="cover-placeholder">无封面</div>
      </template>
      <template #status="{ record }">
        <a-tag :color="record.status === 'published' ? 'green' : 'orange'">
          {{ record.status === 'published' ? '已发布' : '草稿' }}
        </a-tag>
      </template>
      <template #actions="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="viewContent(record)">查看</a-button>
          <a-button type="text" size="small" @click="editContent(record)">编辑</a-button>
          <a-popconfirm content="确定要删除吗？删除后无法恢复" @ok="deleteContent(record.id)">
            <a-button type="text" size="small" status="danger">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="saveContent"
      @cancel="closeModal"
      :width="700"
      :mask-closable="false"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model="formData.title" placeholder="请输入标题" :max-length="100" show-word-limit />
        </a-form-item>

        <a-form-item label="类型">
          <a-radio-group v-model="formData.type" disabled>
            <a-radio value="article">文章</a-radio>
            <a-radio value="blog">博客</a-radio>
            <a-radio value="video">视频</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="封面图片">
          <a-upload
            :action="uploadUrl"
            :show-file-list="false"
            :headers="uploadHeaders"
            @success="handleUploadSuccess"
            @error="handleUploadError"
          >
            <div class="cover-upload">
              <img v-if="formData.coverImage" :src="formData.coverImage" class="cover-preview" />
              <div v-else class="upload-placeholder">
                <icon-plus />
                <span>点击上传封面图</span>
              </div>
            </div>
          </a-upload>
          <div class="upload-tip">支持 JPG、PNG 格式，建议尺寸 16:9</div>
        </a-form-item>

        <a-form-item label="摘要">
          <a-textarea v-model="formData.summary" placeholder="请输入摘要" :rows="2" :max-length="200" show-word-limit />
        </a-form-item>

        <a-form-item label="内容" required>
          <a-textarea v-model="formData.content" placeholder="请输入内容" :rows="8" :max-length="5000" show-word-limit />
        </a-form-item>

        <a-form-item label="视频链接" v-if="formData.type === 'video'">
          <a-input v-model="formData.videoUrl" placeholder="请输入视频链接（如：https://...）" />
        </a-form-item>

        <a-form-item label="状态">
          <a-radio-group v-model="formData.status">
            <a-radio value="draft">存为草稿</a-radio>
            <a-radio value="published">立即发布</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看详情弹窗 -->
    <a-modal v-model:visible="viewModalVisible" :title="viewData.title" :footer="false" :width="800">
      <div class="view-detail">
        <img v-if="viewData.coverImage" :src="viewData.coverImage" class="view-cover" />
        <div class="view-meta">
          <a-tag :color="viewData.status === 'published' ? 'green' : 'orange'">
            {{ viewData.status === 'published' ? '已发布' : '草稿' }}
          </a-tag>
          <span>{{ viewData.createTime }}</span>
          <span>阅读量：{{ viewData.viewCount || 0 }}</span>
        </div>
        <div class="view-summary">{{ viewData.summary }}</div>
        <div class="view-content">{{ viewData.content }}</div>
        <div v-if="viewData.videoUrl" class="view-video">
          <video :src="viewData.videoUrl" controls style="width: 100%"></video>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'

// 定义类型接口
interface ContentItem {
  id: number
  title: string
  type: string
  coverImage: string
  summary: string
  content: string
  videoUrl?: string
  status: string
  createTime: string
  updateTime: string
  viewCount: number
}

// 当前激活的标签
const activeTab = ref<string>('article')
const searchKeyword = ref<string>('')
const filterStatus = ref<string>('')
const loading = ref<boolean>(false)

// 弹窗状态
const modalVisible = ref<boolean>(false)
const viewModalVisible = ref<boolean>(false)
const isEdit = ref<boolean>(false)
const editId = ref<number | null>(null)
const viewData = ref<ContentItem>({
  id: 0,
  title: '',
  type: '',
  coverImage: '',
  summary: '',
  content: '',
  status: '',
  createTime: '',
  updateTime: '',
  viewCount: 0
})

// 上传配置
const uploadUrl = '/api/upload/image'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

// 表格列配置
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '封面', slotName: 'cover', width: 100 },
  { title: '标题', dataIndex: 'title', ellipsis: true },
  { title: '摘要', dataIndex: 'summary', ellipsis: true, width: 250 },
  { title: '状态', slotName: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'createTime', width: 120 },
  { title: '更新时间', dataIndex: 'updateTime', width: 120 },
  { title: '操作', slotName: 'actions', width: 150, fixed: 'right' as const }
]

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const formData = ref<ContentItem>({
  id: 0,
  title: '',
  type: 'article',
  coverImage: '',
  summary: '',
  content: '',
  videoUrl: '',
  status: 'draft',
  createTime: '',
  updateTime: '',
  viewCount: 0
})

// 内容列表数据
const contentList = ref<ContentItem[]>([])

// 模拟数据（实际替换为后端接口）
const mockData: Record<string, ContentItem[]> = {
  article: [
    { id: 1, title: '如何缓解考试焦虑', type: 'article', summary: '考试焦虑是许多学生面临的常见问题...', content: '详细内容：考试焦虑的缓解方法包括深呼吸、合理规划时间...', coverImage: '', status: 'published', createTime: '2025-03-20', updateTime: '2025-03-20', viewCount: 156 },
    { id: 2, title: '青春期心理特点与应对', type: 'article', summary: '青春期是人生重要的心理发展阶段...', content: '详细内容：青春期心理特点包括自我意识增强、情绪波动大...', coverImage: '', status: 'published', createTime: '2025-03-18', updateTime: '2025-03-19', viewCount: 89 },
    { id: 3, title: '人际关系沟通技巧', type: 'article', summary: '良好的人际关系需要有效的沟通技巧...', content: '详细内容：倾听、表达、共情是沟通的核心...', coverImage: '', status: 'draft', createTime: '2025-03-22', updateTime: '2025-03-22', viewCount: 0 }
  ],
  blog: [
    { id: 4, title: '每周心理小贴士 - 第1期', type: 'blog', summary: '本周心理建议：学会接纳不完美的自己', content: '详细内容...', coverImage: '', status: 'published', createTime: '2025-03-21', updateTime: '2025-03-21', viewCount: 45 },
    { id: 5, title: '心理咨询师手记', type: 'blog', summary: '一位来访者的成长故事', content: '详细内容...', coverImage: '', status: 'published', createTime: '2025-03-19', updateTime: '2025-03-19', viewCount: 78 }
  ],
  video: [
    { id: 6, title: '正念冥想入门指导', type: 'video', summary: '10分钟正念冥想练习', content: '视频介绍：正念冥想可以帮助缓解焦虑...', videoUrl: 'https://example.com/video1.mp4', coverImage: '', status: 'published', createTime: '2025-03-17', updateTime: '2025-03-17', viewCount: 234 },
    { id: 7, title: '如何应对压力', type: 'video', summary: '压力管理技巧分享', content: '视频介绍：压力来源分析与应对策略...', videoUrl: 'https://example.com/video2.mp4', coverImage: '', status: 'draft', createTime: '2025-03-23', updateTime: '2025-03-23', viewCount: 0 }
  ]
}

// 计算属性
const modalTitle = computed(() => {
  const typeName = getTypeName()
  return isEdit.value ? `编辑${typeName}` : `新增${typeName}`
})

const getTypeName = (): string => {
  const map: Record<string, string> = { article: '文章', blog: '博客', video: '视频' }
  return map[activeTab.value] || '内容'
}

// 加载列表
const loadContentList = async (): Promise<void> => {
  loading.value = true
  // 模拟后端请求（实际替换为 API 调用）
  setTimeout(() => {
    let list = [...mockData[activeTab.value]]
    
    // 搜索过滤
    if (searchKeyword.value) {
      list = list.filter(item => item.title.includes(searchKeyword.value))
    }
    
    // 状态过滤
    if (filterStatus.value) {
      list = list.filter(item => item.status === filterStatus.value)
    }
    
    contentList.value = list
    pagination.total = list.length
    loading.value = false
  }, 300)
}

// 切换标签
const handleTabChange = (): void => {
  searchKeyword.value = ''
  filterStatus.value = ''
  pagination.current = 1
  loadContentList()
}

// 分页切换
const handlePageChange = (page: number): void => {
  pagination.current = page
  loadContentList()
}

// 打开新增弹窗
const openAddModal = (): void => {
  isEdit.value = false
  editId.value = null
  formData.value = {
    id: 0,
    title: '',
    type: activeTab.value,
    coverImage: '',
    summary: '',
    content: '',
    videoUrl: '',
    status: 'draft',
    createTime: '',
    updateTime: '',
    viewCount: 0
  }
  modalVisible.value = true
}

// 编辑内容
const editContent = (record: ContentItem): void => {
  isEdit.value = true
  editId.value = record.id
  formData.value = { ...record }
  modalVisible.value = true
}

// 保存内容
const saveContent = async (): Promise<void> => {
  if (!formData.value.title) {
    Message.warning('请输入标题')
    return
  }
  if (!formData.value.content) {
    Message.warning('请输入内容')
    return
  }
  
  const now = new Date().toISOString().split('T')[0]
  
  if (isEdit.value) {
    // 更新
    const index = contentList.value.findIndex(i => i.id === editId.value)
    if (index !== -1) {
      contentList.value[index] = {
        ...contentList.value[index],
        ...formData.value,
        updateTime: now
      }
      Message.success('更新成功')
    }
  } else {
    // 新增
    const newItem: ContentItem = {
      ...formData.value,
      id: Date.now(),
      createTime: now,
      updateTime: now,
      viewCount: 0
    }
    contentList.value.unshift(newItem)
    Message.success('添加成功')
  }
  modalVisible.value = false
  loadContentList()
}

// 删除内容
const deleteContent = async (id: number): Promise<void> => {
  const index = contentList.value.findIndex(i => i.id === id)
  if (index !== -1) {
    contentList.value.splice(index, 1)
    Message.success('删除成功')
  }
}

// 查看详情
const viewContent = (record: ContentItem): void => {
  viewData.value = record
  viewModalVisible.value = true
}

// 上传成功
const handleUploadSuccess = (res: any): void => {
  formData.value.coverImage = res.data.url
  Message.success('上传成功')
}

const handleUploadError = (): void => {
  Message.error('上传失败')
}

// 关闭弹窗
const closeModal = (): void => {
  modalVisible.value = false
}

onMounted(() => {
  loadContentList()
})
</script>

<style scoped>
.content-manage {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #1d2129;
}

.page-header p {
  margin: 0;
  color: #86909c;
}

.content-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 0 20px;
  border-radius: 8px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.cover-thumb {
  width: 60px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-placeholder {
  width: 60px;
  height: 40px;
  background: #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
  border-radius: 4px;
}

.cover-upload {
  width: 200px;
  height: 120px;
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
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #86909c;
}

.upload-tip {
  font-size: 12px;
  color: #86909c;
  margin-top: 8px;
}

.view-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.view-cover {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 16px;
}

.view-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  color: #86909c;
  font-size: 13px;
}

.view-summary {
  background: #f7f8fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #4e5969;
}

.view-content {
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>