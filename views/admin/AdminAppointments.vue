<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>预约管理</h2>
      <a-space>
        <a-select v-model="statusFilter" placeholder="预约状态" allow-clear style="width: 150px" @change="handleFilterChange">
          <a-option value="">全部</a-option>
          <a-option value="pending">待确认</a-option>
          <a-option value="confirmed">已确认</a-option>
          <a-option value="completed">已完成</a-option>
          <a-option value="cancelled">已取消</a-option>
        </a-select>
        <a-input-search 
          v-model="searchKeyword" 
          placeholder="搜索用户/咨询师" 
          style="width: 250px" 
          @search="handleSearch" 
          allow-clear
        />
      </a-space>
    </div>

    <a-table
      :columns="columns"
      :data="filteredAppointmentList"
      :loading="loading"
      :pagination="pagination"
      @page-change="onPageChange"
    >
      <template #status="{ record }">
        <a-tag :color="getStatusColor(record.status)">
          {{ getStatusName(record.status) }}
        </a-tag>
      </template>
      <template #actions="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="handleView(record)">查看</a-button>
          <a-button 
            v-if="record.status === 'pending'" 
            type="text" 
            size="small" 
            status="success" 
            @click="handleConfirm(record)"
          >
            确认
          </a-button>
          <a-button 
            v-if="record.status === 'pending'" 
            type="text" 
            size="small" 
            status="danger" 
            @click="handleCancel(record)"
          >
            取消
          </a-button>
          <a-button 
            v-if="record.status === 'confirmed'" 
            type="text" 
            size="small" 
            @click="handleComplete(record)"
          >
            完成
          </a-button>
        </a-space>
      </template>
    </a-table>

    <!-- 查看预约详情弹窗 -->
    <a-modal v-model:visible="detailVisible" title="预约详情" @cancel="detailVisible = false">
      <a-descriptions :data="detailData" :column="1" bordered />
      <template #footer>
        <a-button @click="detailVisible = false">关闭</a-button>
      </template>
    </a-modal>

    <!-- 确认预约弹窗 -->
    <a-modal v-model:visible="confirmVisible" title="确认预约" @ok="doConfirm">
      <p>确定要确认该预约吗？</p>
      <p>用户：{{ currentRecord?.userName }}</p>
      <p>时间：{{ currentRecord?.bookingTime }}</p>
    </a-modal>

    <!-- 取消预约弹窗 -->
    <a-modal v-model:visible="cancelVisible" title="取消预约" @ok="doCancel">
      <p>确定要取消该预约吗？</p>
      <p>用户：{{ currentRecord?.userName }}</p>
      <a-textarea v-model="cancelReason" placeholder="请输入取消原因" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { Message, Modal } from '@arco-design/web-vue';

const loading = ref(false);
const statusFilter = ref('');
const searchKeyword = ref('');
const detailVisible = ref(false);
const confirmVisible = ref(false);
const cancelVisible = ref(false);
const currentRecord = ref<any>(null);
const cancelReason = ref('');

// 预约列表数据
const appointmentList = ref([
  { id: 1, userName: '张三', teacherName: '张老师', bookingTime: '2024-01-15 14:00', status: 'pending', phone: '13800138001', remark: '第一次咨询' },
  { id: 2, userName: '李四', teacherName: '李老师', bookingTime: '2024-01-15 15:00', status: 'confirmed', phone: '13800138002', remark: '焦虑问题' },
  { id: 3, userName: '王五', teacherName: '王老师', bookingTime: '2024-01-16 10:00', status: 'completed', phone: '13800138003', remark: '人际关系' }
]);

// 过滤后的列表
const filteredAppointmentList = computed(() => {
  let list = appointmentList.value;
  
  // 状态过滤
  if (statusFilter.value) {
    list = list.filter(item => item.status === statusFilter.value);
  }
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    list = list.filter(item => 
      item.userName.toLowerCase().includes(keyword) || 
      item.teacherName.toLowerCase().includes(keyword)
    );
  }
  
  return list;
});

const pagination = ref({ current: 1, pageSize: 10, total: computed(() => filteredAppointmentList.value.length) });

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '用户', dataIndex: 'userName' },
  { title: '咨询师', dataIndex: 'teacherName' },
  { title: '预约时间', dataIndex: 'bookingTime' },
  { title: '联系电话', dataIndex: 'phone' },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '操作', slotName: 'actions', width: 250 }
];

const detailData = ref<any[]>([]);

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    pending: 'orange',
    confirmed: 'blue',
    completed: 'green',
    cancelled: 'red'
  };
  return colors[status] || 'gray';
};

const getStatusName = (status: string) => {
  const names: Record<string, string> = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  };
  return names[status] || status;
};

const handleFilterChange = () => {
  pagination.value.current = 1;
};

const handleSearch = () => {
  pagination.value.current = 1;
};

const handleView = (record: any) => {
  detailData.value = [
    { label: '预约ID', value: record.id },
    { label: '用户', value: record.userName },
    { label: '咨询师', value: record.teacherName },
    { label: '预约时间', value: record.bookingTime },
    { label: '联系电话', value: record.phone },
    { label: '状态', value: getStatusName(record.status) },
    { label: '备注', value: record.remark || '无' }
  ];
  detailVisible.value = true;
};

const handleConfirm = (record: any) => {
  currentRecord.value = record;
  confirmVisible.value = true;
};

const doConfirm = () => {
  if (currentRecord.value) {
    currentRecord.value.status = 'confirmed';
    Message.success(`已确认预约：${currentRecord.value.userName}`);
    confirmVisible.value = false;
    currentRecord.value = null;
  }
};

const handleCancel = (record: any) => {
  currentRecord.value = record;
  cancelReason.value = '';
  cancelVisible.value = true;
};

const doCancel = () => {
  if (currentRecord.value) {
    currentRecord.value.status = 'cancelled';
    Message.warning(`已取消预约：${currentRecord.value.userName}，原因：${cancelReason.value || '无'}`);
    cancelVisible.value = false;
    currentRecord.value = null;
    cancelReason.value = '';
  }
};

const handleComplete = (record: any) => {
  Modal.confirm({
    title: '完成预约',
    content: `确定要将 ${record.userName} 的预约标记为已完成吗？`,
    onOk: () => {
      record.status = 'completed';
      Message.success(`已完成预约：${record.userName}`);
    }
  });
};

const onPageChange = (page: number) => {
  pagination.value.current = page;
};
</script>

<style scoped>
.admin-page { padding: 20px; background: #fff; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.page-header h2 { margin: 0; }
</style>