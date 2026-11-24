<template>
  <div class="my-articles">
    <div class="page-header">
      <h2>我的文章</h2>
      <a-button type="primary" @click="goToCreate">
        + 发布文章
      </a-button>
    </div>

    <a-table :columns="columns" :data="articleList" row-key="id">
      <template #status="{ record }">
        <a-tag :color="record.status === 'published' ? 'green' : 'orange'">
          {{ record.status === 'published' ? '已发布' : '草稿' }}
        </a-tag>
      </template>
      <template #actions="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="editArticle(record)">编辑</a-button>
          <a-popconfirm content="确定删除吗？" @ok="deleteArticle(record.id)">
            <a-button type="text" size="small" status="danger">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '标题', dataIndex: 'title' },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 120 },
  { title: '操作', slotName: 'actions', width: 150 }
]

const articleList = ref([
  { id: 1, title: '如何缓解考试焦虑', status: 'published', createTime: '2025-03-20' },
  { id: 2, title: '青春期心理特点', status: 'published', createTime: '2025-03-18' },
  { id: 3, title: '人际关系技巧', status: 'draft', createTime: '2025-03-22' }
])

const goToCreate = () => {
  router.push('/teacher/article/create')
}

const editArticle = (article) => {
  router.push(`/teacher/article/edit/${article.id}`)
}

const deleteArticle = (id) => {
  const index = articleList.value.findIndex(a => a.id === id)
  if (index !== -1) {
    articleList.value.splice(index, 1)
    Message.success('删除成功')
  }
}
</script>

<style scoped>
.my-articles {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
}
</style>