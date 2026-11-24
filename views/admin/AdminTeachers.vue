<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>咨询师管理</h2>
      <a-button type="primary" @click="handleAdd">
        <template #icon><icon-plus /></template>
        新增咨询师
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data="teacherList"
      :loading="loading"
      :pagination="pagination"
      @page-change="onPageChange"
    >
      <template #status="{ record }">
        <a-tag :color="record.status === 'active' ? 'green' : 'red'">
          {{ record.status === 'active' ? '在职' : '离职' }}
        </a-tag>
      </template>
      <template #actions="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
          <a-button type="text" size="small" @click="handleSchedule(record)">排班</a-button>
          <a-button type="text" size="small" @click="handleView(record)">查看</a-button>
          <a-popconfirm content="确定删除吗？" @ok="handleDelete(record)">
            <a-button type="text" size="small" status="danger">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:visible="modalVisible" :title="modalTitle" @ok="handleSubmit" @cancel="modalVisible = false" :width="600">
      <a-form :model="formData" layout="vertical">
        <a-form-item label="姓名" required>
          <a-input v-model="formData.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="联系电话" required>
          <a-input v-model="formData.phone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="专长领域">
          <a-textarea v-model="formData.specialty" placeholder="请输入专长领域" />
        </a-form-item>
        <a-form-item label="从业年限">
          <a-input-number v-model="formData.experience" :min="0" :max="50" />
        </a-form-item>
        <a-form-item label="个人简介">
          <a-textarea v-model="formData.bio" placeholder="请输入个人简介" :max-length="500" show-word-limit />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 排班弹窗 -->
    <a-modal v-model:visible="scheduleVisible" title="排班管理" @ok="scheduleVisible = false" :width="700">
      <a-table :columns="scheduleColumns" :data="scheduleList" :pagination="false">
        <template #time="{ record }">
          <a-time-picker v-model="record.time" />
        </template>
      </a-table>
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
const scheduleVisible = ref(false);
const modalTitle = ref('新增咨询师');
const editingId = ref(null);

const teacherList = ref([
  { id: 1, name: '张老师', specialty: '焦虑症、抑郁症', experience: '5年', status: 'active', phone: '13800138001', bio: '国家二级心理咨询师' },
  { id: 2, name: '李老师', specialty: '人际关系、职业规划', experience: '8年', status: 'active', phone: '13800138002', bio: '资深心理咨询师' },
  { id: 3, name: '王老师', specialty: '儿童心理、家庭教育', experience: '6年', status: 'inactive', phone: '13800138003', bio: '家庭教育指导师' }
]);

const scheduleList = ref([
  { id: 1, weekday: '周一', time: '09:00-12:00', status: 'available' },
  { id: 2, weekday: '周二', time: '09:00-12:00', status: 'available' },
  { id: 3, weekday: '周三', time: '14:00-17:00', status: 'available' }
]);

const formData = ref({
  name: '',
  phone: '',
  specialty: '',
  experience: 0,
  bio: ''
});

const pagination = ref({ current: 1, pageSize: 10, total: teacherList.value.length });

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '姓名', dataIndex: 'name' },
  { title: '专长领域', dataIndex: 'specialty' },
  { title: '从业年限', dataIndex: 'experience', width: 100 },
  { title: '联系电话', dataIndex: 'phone' },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '操作', slotName: 'actions', width: 250 }
];

const scheduleColumns = [
  { title: '星期', dataIndex: 'weekday', width: 100 },
  { title: '时间段', slotName: 'time' },
  { title: '状态', dataIndex: 'status' }
];

const handleAdd = () => {
  editingId.value = null;
  modalTitle.value = '新增咨询师';
  formData.value = { name: '', phone: '', specialty: '', experience: 0, bio: '' };
  modalVisible.value = true;
};

const handleEdit = (record) => {
  editingId.value = record.id;
  modalTitle.value = '编辑咨询师';
  formData.value = { ...record };
  modalVisible.value = true;
};

const handleView = (record) => {
  Message.info(`查看详情：${record.name}`);
};

const handleSchedule = (record) => {
  scheduleVisible.value = true;
};

const handleSubmit = () => {
  if (!formData.value.name || !formData.value.phone) {
    Message.warning('请填写姓名和联系电话');
    return;
  }
  
  if (editingId.value) {
    const index = teacherList.value.findIndex(item => item.id === editingId.value);
    if (index !== -1) {
      teacherList.value[index] = { ...teacherList.value[index], ...formData.value };
      Message.success('更新成功');
    }
  } else {
    const newId = Math.max(...teacherList.value.map(item => item.id)) + 1;
    teacherList.value.push({
      id: newId,
      ...formData.value,
      status: 'active'
    });
    Message.success('添加成功');
  }
  modalVisible.value = false;
  pagination.value.total = teacherList.value.length;
};

const handleDelete = (record) => {
  const index = teacherList.value.findIndex(item => item.id === record.id);
  if (index !== -1) {
    teacherList.value.splice(index, 1);
    Message.success(`删除成功：${record.name}`);
    pagination.value.total = teacherList.value.length;
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