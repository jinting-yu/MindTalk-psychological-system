<template>
  <div class="teacher-students">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalStudents }}</div>
        <div class="stat-label">服务学生总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.activeStudents }}</div>
        <div class="stat-label">活跃学生</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.avgTestScore }}</div>
        <div class="stat-label">平均测评得分</div>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-bar">
      <a-input-search 
        v-model="keyword" 
        placeholder="搜索学生姓名" 
        style="width: 300px"
        @search="handleSearch"
      />
      <a-select v-model="riskLevel" placeholder="风险等级" style="width: 150px" allow-clear>
        <a-option value="high">高风险</a-option>
        <a-option value="medium">中风险</a-option>
        <a-option value="low">低风险</a-option>
      </a-select>
    </div>

    <!-- 学生列表 -->
    <a-table 
      :columns="columns" 
      :data="studentList" 
      :loading="loading"
      :pagination="pagination"
      @page-change="handlePageChange"
    >
      <template #riskLevel="{ record }">
        <a-tag :color="getRiskColor(record.riskLevel)">
          {{ getRiskText(record.riskLevel) }}
        </a-tag>
      </template>
      
      <template #action="{ record }">
        <a-space>
          <!-- 查看档案按钮 -->
          <a-button type="text" size="small" @click="viewArchive(record.id)">
            📋 查看档案
          </a-button>
          <!-- 测评记录按钮 -->
          <a-button type="text" size="small" @click="viewTestRecords(record.id)">
            📊 测评记录
          </a-button>
          <!-- 预约按钮 -->
          <a-button type="text" size="small" @click="createAppointment(record.id)">
            📅 预约
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
const keyword = ref('')
const riskLevel = ref('')
const studentList = ref<any[]>([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const stats = ref({
  totalStudents: 0,
  activeStudents: 0,
  avgTestScore: 0
})

const columns = [
  { title: '学生姓名', dataIndex: 'studentName', width: 100 },
  { title: '学号', dataIndex: 'studentId', width: 120 },
  { title: '最近测评', dataIndex: 'lastTest', width: 150 },
  { title: '最近测评时间', dataIndex: 'lastTestTime', width: 120 },
  { title: '风险等级', slotName: 'riskLevel', width: 100 },
  { title: '预约次数', dataIndex: 'appointmentCount', width: 100 },
  { title: '操作', slotName: 'action', width: 320, fixed: 'right' }
]

const getRiskText = (level: string) => {
  const map: Record<string, string> = {
    high: '高风险',
    medium: '中风险',
    low: '低风险'
  }
  return map[level] || level
}

const getRiskColor = (level: string) => {
  const map: Record<string, string> = {
    high: 'red',
    medium: 'orange',
    low: 'green'
  }
  return map[level] || 'gray'
}

// ✅ 查看档案
const viewArchive = (studentId: number) => {
  if (!studentId) {
    Message.warning('学生信息不存在')
    return
  }
  console.log('查看学生档案，学生ID:', studentId)
  router.push(`/user/archive/${studentId}`) 
}

// ✅ 查看测评记录
const viewTestRecords = (studentId: number) => {
  if (!studentId) {
    Message.warning('学生信息不存在')
    return
  }
  router.push({
    path: '/teacher/test-records',
    query: { studentId: String(studentId) }
  })
}

// ✅ 创建预约
const createAppointment = (studentId: number) => {
  if (!studentId) {
    Message.warning('学生信息不存在')
    return
  }
  router.push({
    path: '/booking/create',
    query: { studentId: String(studentId) }
  })
}

const handleSearch = () => {
  pagination.value.current = 1
  loadStudents()
}

const handlePageChange = (page: number) => {
  pagination.value.current = page
  loadStudents()
}

const loadStudents = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    studentList.value = [
      { id: 101, studentName: '张三', studentId: '2024001', lastTest: 'MBTI职业性格测试', lastTestTime: '2025-03-20', riskLevel: 'low', appointmentCount: 3 },
      { id: 102, studentName: '李四', studentId: '2024002', lastTest: 'SAS焦虑自评量表', lastTestTime: '2025-03-22', riskLevel: 'medium', appointmentCount: 5 },
      { id: 103, studentName: '王五', studentId: '2024003', lastTest: '抑郁自评量表', lastTestTime: '2025-03-25', riskLevel: 'high', appointmentCount: 2 }
    ]
    pagination.value.total = 45
    stats.value = { totalStudents: 45, activeStudents: 32, avgTestScore: 76 }
  } catch (error) {
    Message.error('加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStudents()
})
</script>

<style scoped>
.teacher-students {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-card .stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #3b82f6;
}

.stat-card .stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 8px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
</style>