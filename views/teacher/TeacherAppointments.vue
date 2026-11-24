<template>
  <div class="teacher-appointments">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <a-tabs v-model:active-key="statusFilter" @change="loadAppointments">
        <a-tab-pane key="all" title="全部" />
        <a-tab-pane key="pending" title="待确认" />
        <a-tab-pane key="confirmed" title="已确认" />
        <a-tab-pane key="completed" title="已完成" />
      </a-tabs>
      
      <div class="search-box">
        <a-input-search 
          v-model="keyword" 
          placeholder="搜索学生姓名" 
          @search="handleSearch"
        />
      </div>
    </div>

    <!-- 预约列表 -->
    <a-table 
      :columns="columns" 
      :data="appointmentList" 
      :loading="loading"
      :pagination="pagination"
      @page-change="handlePageChange"
    >
      <template #status="{ record }">
        <a-tag :color="getStatusColor(record.status)">
          {{ getStatusText(record.status) }}
        </a-tag>
      </template>
      
      <template #action="{ record }">
        <a-space wrap>
          <!-- 查看档案按钮（始终显示） -->
          <a-button type="text" size="small" @click="viewArchive(record.studentId)">
            📋 档案
          </a-button>
          
          <a-button 
            v-if="record.status === 'pending'" 
            type="primary" 
            size="small" 
            @click="confirmAppointment(record.id)"
          >
            确认
          </a-button>
          
          <a-button 
            v-if="record.status === 'confirmed'" 
            type="outline" 
            size="small" 
            @click="viewStudentDetail(record.studentId)"
          >
            查看学生
          </a-button>
          
          <a-button 
            v-if="record.status === 'confirmed'" 
            type="outline" 
            size="small" 
            @click="viewTestRecords(record.studentId)"
          >
            测评记录
          </a-button>
          
          <a-button 
            v-if="record.status === 'completed'" 
            type="text" 
            size="small" 
            @click="viewFeedback(record.id)"
          >
            查看反馈
          </a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()

const loading = ref(false)
const statusFilter = ref('all')
const keyword = ref('')
const appointmentList = ref<any[]>([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '学生姓名', dataIndex: 'studentName', width: 120 },
  { title: '预约日期', dataIndex: 'date', width: 120 },
  { title: '预约时段', dataIndex: 'timeSlot', width: 120 },
  { title: '咨询类型', dataIndex: 'type', width: 120 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '预约时间', dataIndex: 'createTime', width: 160 },
  { title: '操作', slotName: 'action', width: 320, fixed: 'right' }
]

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

/**
 * 获取状态颜色
 */
const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'orange',
    confirmed: 'blue',
    completed: 'green',
    cancelled: 'red'
  }
  return map[status] || 'gray'
}

/**
 * 安全处理学生ID（转换为字符串并去除空格）
 */
const normalizeStudentId = (studentId: number | string | undefined): string => {
  if (!studentId) return ''
  return String(studentId).trim()
}

/**
 * 查看学生档案
 */
const viewArchive = (studentId: number | string) => {
  const id = normalizeStudentId(studentId)
  if (!id) {
    Message.warning('学生信息不存在')
    return
  }
  console.log('查看学生档案，学生ID:', id)
  router.push(`/user/archive/${id}`)
}

/**
 * 查看测评记录
 */
const viewTestRecords = (studentId: number | string) => {
  const id = normalizeStudentId(studentId)
  if (!id) {
    Message.warning('学生信息不存在')
    return
  }
  console.log('查看测评记录，学生ID:', id)
  router.push({
    path: '/teacher/test-records',
    query: { studentId: id }
  })
}

/**
 * 查看学生详情（跳转到档案）
 */
const viewStudentDetail = (studentId: number | string) => {
  const id = normalizeStudentId(studentId)
  if (!id) {
    Message.warning('学生信息不存在')
    return
  }
  console.log('查看学生详情，学生ID:', id)
  router.push(`/user/archive/${id}`)
}

/**
 * 确认预约
 */
const confirmAppointment = async (id: number) => {
  // TODO: 调用API确认预约
  // const res = await confirmAppointmentApi({ id })
  // if (res.code === 200) {
  //   Message.success('已确认预约')
  //   loadAppointments()
  // }
  
  Message.success('已确认预约')
  loadAppointments()
}

/**
 * 查看反馈
 */
const viewFeedback = (id: number) => {
  router.push(`/teacher/feedback/${id}`)
}

/**
 * 搜索处理
 */
const handleSearch = () => {
  pagination.value.current = 1
  loadAppointments()
}

/**
 * 分页变化
 */
const handlePageChange = (page: number) => {
  pagination.value.current = page
  loadAppointments()
}

/**
 * 加载预约列表
 */
const loadAppointments = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取预约列表
    // const res = await getAppointmentsApi({
    //   status: statusFilter.value === 'all' ? undefined : statusFilter.value,
    //   keyword: keyword.value,
    //   pageNum: pagination.value.current,
    //   pageSize: pagination.value.pageSize
    // })
    // appointmentList.value = res.data.records
    // pagination.value.total = res.data.total
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    appointmentList.value = [
      { id: 1, studentId: '2024001', studentName: '张三', date: '2025-03-28', timeSlot: '10:30-11:30', type: '情绪管理咨询', status: 'pending', createTime: '2025-03-25 14:30' },
      { id: 2, studentId: '2024002', studentName: '李四', date: '2025-03-28', timeSlot: '14:00-15:00', type: '职业规划咨询', status: 'confirmed', createTime: '2025-03-25 10:15' },
      { id: 3, studentId: '2024003', studentName: '王五', date: '2025-03-27', timeSlot: '09:00-10:00', type: '人际关系咨询', status: 'completed', createTime: '2025-03-24 09:00' }
    ]
    pagination.value.total = 25
  } catch (error) {
    console.error('加载预约列表失败:', error)
    Message.error('加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAppointments()
})
</script>

<style scoped>
.teacher-appointments {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-box {
  width: 260px;
}
</style>