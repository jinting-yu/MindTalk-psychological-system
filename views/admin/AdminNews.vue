<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>资讯管理</h2>
      <a-button type="primary" @click="handleAdd">
        <template #icon><icon-plus /></template>
        发布资讯
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data="newsList"
      :loading="loading"
      :pagination="pagination"
      @page-change="onPageChange"
    >
      <template #status="{ record }">
        <a-tag :color="record.isPublished ? 'green' : 'orange'">
          {{ record.isPublished ? '已发布' : '草稿' }}
        </a-tag>
      </template>
      <template #actions="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
          <a-button type="text" size="small" @click="handlePreview(record)">预览</a-button>
          <a-button type="text" size="small" @click="handlePublish(record)" v-if="!record.isPublished">发布</a-button>
          <a-button type="text" size="small" @click="handleUnpublish(record)" v-if="record.isPublished">下架</a-button>
          <a-popconfirm content="确定删除吗？" @ok="handleDelete(record)">
            <a-button type="text" size="small" status="danger">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="modalVisible" :title="modalTitle" @ok="handleSubmit" @cancel="modalVisible = false" :width="700">
      <a-form :model="formData" layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model="formData.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item label="分类" required>
          <a-select v-model="formData.category" placeholder="请选择分类">
            <a-option value="心理健康">心理健康</a-option>
            <a-option value="焦虑管理">焦虑管理</a-option>
            <a-option value="抑郁科普">抑郁科普</a-option>
            <a-option value="压力管理">压力管理</a-option>
            <a-option value="人际关系">人际关系</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="封面图片">
          <a-input v-model="formData.coverImage" placeholder="请输入图片URL" />
        </a-form-item>
        <a-form-item label="内容" required>
          <a-textarea v-model="formData.content" placeholder="请输入内容" :max-length="2000" show-word-limit :rows="8" />
        </a-form-item>
        <a-form-item label="摘要">
          <a-textarea v-model="formData.summary" placeholder="请输入摘要" :max-length="200" show-word-limit :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预览弹窗 -->
    <a-modal v-model:visible="previewVisible" title="资讯预览" :footer="false" :width="800">
      <div v-if="previewData">
        <h2>{{ previewData.title }}</h2>
        <div style="color: #999; margin-bottom: 16px;">
          <span>分类：{{ previewData.category }}</span>
          <span style="margin-left: 16px;">发布时间：{{ previewData.publishTime || '未发布' }}</span>
        </div>
        <div style="border-top: 1px solid #eee; padding-top: 16px; white-space: pre-wrap;">
          {{ previewData.content }}
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';

const loading = ref(false);
const modalVisible = ref(false);
const previewVisible = ref(false);
const modalTitle = ref('发布资讯');
const editingId = ref(null);
const previewData = ref(null);

const newsList = ref([
  { id: 1, title: '心理健康小贴士', category: '心理健康', viewCount: 1234, isPublished: true, publishTime: '2024-01-10', content: '心理健康小贴士内容...', summary: '心理健康的重要性' },
  { id: 2, title: '如何缓解焦虑情绪', category: '焦虑管理', viewCount: 2345, isPublished: true, publishTime: '2024-01-12', content: '缓解焦虑的方法...', summary: '实用缓解焦虑技巧' },
  { id: 3, title: '抑郁症的早期信号', category: '抑郁科普', viewCount: 3456, isPublished: false, publishTime: '', content: '抑郁症早期信号...', summary: '及早识别抑郁症' }
]);

const formData = ref({
  title: '',
  category: '',
  coverImage: '',
  content: '',
  summary: ''
});

const pagination = ref({ current: 1, pageSize: 10, total: newsList.value.length });

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '标题', dataIndex: 'title' },
  { title: '分类', dataIndex: 'category', width: 120 },
  { title: '浏览量', dataIndex: 'viewCount', width: 100 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '发布时间', dataIndex: 'publishTime', width: 120 },
  { title: '操作', slotName: 'actions', width: 300 }
];

const handleAdd = () => {
  editingId.value = null;
  modalTitle.value = '发布资讯';
  formData.value = { title: '', category: '', coverImage: '', content: '', summary: '' };
  modalVisible.value = true;
};

const handleEdit = (record) => {
  editingId.value = record.id;
  modalTitle.value = '编辑资讯';
  formData.value = { ...record };
  modalVisible.value = true;
};

const handlePreview = (record) => {
  previewData.value = record;
  previewVisible.value = true;
};

const handleSubmit = () => {
  if (!formData.value.title || !formData.value.category || !formData.value.content) {
    Message.warning('请填写标题、分类和内容');
    return;
  }
  
  if (editingId.value) {
    const index = newsList.value.findIndex(item => item.id === editingId.value);
    if (index !== -1) {
      newsList.value[index] = { ...newsList.value[index], ...formData.value };
      Message.success('更新成功');
    }
  } else {
    const newId = Math.max(...newsList.value.map(item => item.id)) + 1;
    newsList.value.push({
      id: newId,
      ...formData.value,
      viewCount: 0,
      isPublished: false,
      publishTime: ''
    });
    Message.success('添加成功');
  }
  modalVisible.value = false;
  pagination.value.total = newsList.value.length;
};

const handlePublish = (record) => {
  record.isPublished = true;
  record.publishTime = new Date().toISOString().split('T')[0];
  Message.success(`已发布：${record.title}`);
};

const handleUnpublish = (record) => {
  record.isPublished = false;
  Message.warning(`已下架：${record.title}`);
};

const handleDelete = (record) => {
  const index = newsList.value.findIndex(item => item.id === record.id);
  if (index !== -1) {
    newsList.value.splice(index, 1);
    Message.success(`删除成功：${record.title}`);
    pagination.value.total = newsList.value.length;
  }
};

const onPageChange = (page) => {
  pagination.value.current = page;
};
</script>

<style scoped>
.admin-page { padding: 20px; background: #fff; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; }
</style>