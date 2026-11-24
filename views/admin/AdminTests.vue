<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>测评管理</h2>
      <a-button type="primary" @click="handleAdd">
        <template #icon><icon-plus /></template>
        新增测评
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data="testList"
      :loading="loading"
      :pagination="pagination"
      @page-change="onPageChange"
    >
      <template #status="{ record }">
        <a-tag :color="record.status === 'published' ? 'green' : 'orange'">
          {{ record.status === 'published' ? '已发布' : '草稿' }}
        </a-tag>
      </template>
      <template #actions="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
          <a-button type="text" size="small" @click="handleView(record)">查看</a-button>
          <a-button type="text" size="small" @click="handlePublish(record)" v-if="record.status === 'draft'">发布</a-button>
          <a-popconfirm content="确定删除吗？" @ok="handleDelete(record)">
            <a-button type="text" size="small" status="danger">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="modalVisible" :title="modalTitle" @ok="handleSubmit" @cancel="modalVisible = false" :width="600">
      <a-form :model="formData" layout="vertical">
        <a-form-item label="测评名称" required>
          <a-input v-model="formData.name" placeholder="请输入测评名称" />
        </a-form-item>
        <a-form-item label="测评类型" required>
          <a-select v-model="formData.type" placeholder="请选择类型">
            <a-option value="心理健康">心理健康</a-option>
            <a-option value="性格测试">性格测试</a-option>
            <a-option value="能力评估">能力评估</a-option>
            <a-option value="压力评估">压力评估</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="题目数量">
          <a-input-number v-model="formData.questionCount" :min="1" :max="100" />
        </a-form-item>
        <a-form-item label="测评描述">
          <a-textarea v-model="formData.description" placeholder="请输入测评描述" :max-length="500" show-word-limit />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看详情弹窗 -->
    <a-modal v-model:visible="detailVisible" title="测评详情" :footer="false">
      <a-descriptions :data="detailData" :column="1" bordered />
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
const detailVisible = ref(false);
const modalTitle = ref('新增测评');
const editingId = ref(null);

const testList = ref([
  { id: 1, name: '焦虑自评量表', type: '心理健康', questionCount: 20, status: 'published', description: '用于评估焦虑程度的自评量表', createTime: '2024-01-01' },
  { id: 2, name: '抑郁自评量表', type: '心理健康', questionCount: 20, status: 'published', description: '用于评估抑郁程度的自评量表', createTime: '2024-01-02' },
  { id: 3, name: '压力测试量表', type: '压力评估', questionCount: 15, status: 'draft', description: '评估当前压力水平', createTime: '2024-01-03' }
]);

const formData = ref({
  name: '',
  type: '',
  questionCount: 10,
  description: ''
});

const pagination = ref({ current: 1, pageSize: 10, total: testList.value.length });

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '测评名称', dataIndex: 'name' },
  { title: '类型', dataIndex: 'type', width: 120 },
  { title: '题目数', dataIndex: 'questionCount', width: 100 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 120 },
  { title: '操作', slotName: 'actions', width: 250 }
];

const detailData = ref([]);

const handleAdd = () => {
  editingId.value = null;
  modalTitle.value = '新增测评';
  formData.value = { name: '', type: '', questionCount: 10, description: '' };
  modalVisible.value = true;
};

const handleEdit = (record) => {
  editingId.value = record.id;
  modalTitle.value = '编辑测评';
  formData.value = { ...record };
  modalVisible.value = true;
};

const handleView = (record) => {
  detailData.value = [
    { label: '测评ID', value: record.id },
    { label: '测评名称', value: record.name },
    { label: '类型', value: record.type },
    { label: '题目数量', value: record.questionCount },
    { label: '状态', value: record.status === 'published' ? '已发布' : '草稿' },
    { label: '创建时间', value: record.createTime },
    { label: '描述', value: record.description || '无' }
  ];
  detailVisible.value = true;
};

const handleSubmit = () => {
  if (!formData.value.name || !formData.value.type) {
    Message.warning('请填写必填项');
    return;
  }
  
  if (editingId.value) {
    const index = testList.value.findIndex(item => item.id === editingId.value);
    if (index !== -1) {
      testList.value[index] = { ...testList.value[index], ...formData.value };
      Message.success('更新成功');
    }
  } else {
    const newId = Math.max(...testList.value.map(item => item.id)) + 1;
    testList.value.push({
      id: newId,
      ...formData.value,
      status: 'draft',
      createTime: new Date().toISOString().split('T')[0]
    });
    Message.success('添加成功');
  }
  modalVisible.value = false;
  pagination.value.total = testList.value.length;
};

const handlePublish = (record) => {
  record.status = 'published';
  Message.success(`已发布：${record.name}`);
};

const handleDelete = (record) => {
  const index = testList.value.findIndex(item => item.id === record.id);
  if (index !== -1) {
    testList.value.splice(index, 1);
    Message.success(`删除成功：${record.name}`);
    pagination.value.total = testList.value.length;
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