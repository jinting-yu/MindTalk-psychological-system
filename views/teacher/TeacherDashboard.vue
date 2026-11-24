<template>
  <div class="teacher-dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: #eef2ff;">
          <icon-calendar />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalAppointments }}</div>
          <div class="stat-label">总预约数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fef3e8;">
          <icon-clock-circle />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingAppointments }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #e8f3ed;">
          <icon-user />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalStudents }}</div>
          <div class="stat-label">服务学生</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fde7e7;">
          <icon-file />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalTests }}</div>
          <div class="stat-label">测评总数</div>
        </div>
      </div>
    </div>

    <!-- 今日预约 -->
    <div class="section">
      <div class="section-header">
        <h3>今日预约</h3>
        <a-link @click="goToAppointments">查看全部 →</a-link>
      </div>
      <div class="appointment-list">
        <div v-for="apt in todayAppointments" :key="apt.id" class="appointment-item">
          <div class="apt-time">
            <div class="time">{{ apt.time }}</div>
          </div>
          <div class="apt-info">
            <h4>{{ apt.studentName }}</h4>
            <p>{{ apt.type }}</p>
          </div>
          <div class="apt-status">
            <a-tag :color="apt.status === 'pending' ? 'orange' : 'green'">
              {{ apt.status === 'pending' ? '待确认' : '已确认' }}
            </a-tag>
          </div>
          <div class="apt-actions">
            <a-button v-if="apt.status === 'pending'" type="primary" size="small" @click="confirmAppointment(apt.id)">
              确认
            </a-button>
            <a-button type="outline" size="small" @click="startConsultation(apt.id)">
              开始咨询
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 近期测评记录 -->
    <div class="section">
      <div class="section-header">
        <h3>近期测评记录</h3>
        <a-link @click="goToTestRecords">查看更多 →</a-link>
      </div>
      <a-table 
        :columns="testColumns" 
        :data="recentTests" 
        :pagination="false"
        size="small"
      >
        <template #result="{ record }">
          <a-tag :color="getResultColor(record.result)">
            {{ record.result }}
          </a-tag>
        </template>
        <template #action="{ record }">
          <a-button type="text" size="small" @click="viewTestDetail(record.id)">
            查看详情
          </a-button>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()

// 统计数据
const stats = ref({
  totalAppointments: 0,
  pendingAppointments: 0,
  totalStudents: 0,
  totalTests: 0
})

// 今日预约
const todayAppointments = ref([
  {
    id: 1,
    studentName: '张三',
    time: '09:00-10:00',
    type: '情绪管理咨询',
    status: 'pending'
  },
  {
    id: 2,
    studentName: '李四',
    time: '10:30-11:30',
    type: '职业规划咨询',
    status: 'confirmed'
  },
  {
    id: 3,
    studentName: '王五',
    time: '14:00-15:00',
    type: '人际关系咨询',
    status: 'pending'
  }
])

// 近期测评记录
const recentTests = ref([
  {
    id: 1,
    studentName: '张三',
    testName: 'MBTI职业性格测试',
    result: 'INTJ型',
    date: '2025-03-25',
    score: 85
  },
  {
    id: 2,
    studentName: '李四',
    testName: 'SAS焦虑自评量表',
    result: '轻度焦虑',
    date: '2025-03-24',
    score: 52
  },
  {
    id: 3,
    studentName: '王五',
    testName: '抑郁自评量表',
    result: '正常范围',
    date: '2025-03-23',
    score: 32
  }
])

// 表格列定义
const testColumns = [
  { title: '学生', dataIndex: 'studentName', width: 100 },
  { title: '测评名称', dataIndex: 'testName', width: 180 },
  { title: '结果', slotName: 'result', width: 100 },
  { title: '测评日期', dataIndex: 'date', width: 120 },
  { title: '操作', slotName: 'action', width: 100 }
]

// 获取结果颜色
const getResultColor = (result: string) => {
  if (result.includes('焦虑') || result.includes('抑郁')) return 'orange'
  if (result === '正常范围') return 'green'
  return 'blue'
}

// 加载统计数据
const loadStats = async () => {
  // TODO: 调用API
  stats.value = {
    totalAppointments: 28,
    pendingAppointments: 5,
    totalStudents: 45,
    totalTests: 156
  }
}

// 确认预约
const confirmAppointment = (id: number) => {
  Message.success('已确认预约')
  // TODO: 调用API
}

// 开始咨询
const startConsultation = (id: number) => {
  router.push(`/teacher/consultation/${id}`)
}

// 跳转到预约列表
const goToAppointments = () => {
  router.push('/teacher/appointments')
}

// 跳转到测评记录
const goToTestRecords = () => {
  router.push('/teacher/test-records')
}

// 查看测评详情
const viewTestDetail = (id: number) => {
  router.push(`/teacher/test-detail/${id}`)
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.teacher-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.stat-info .stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2ff;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.appointment-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.3s;
}

.appointment-item:hover {
  background: #f1f5f9;
}

.apt-time .time {
  font-size: 16px;
  font-weight: 600;
  color: #3b82f6;
}

.apt-info {
  flex: 1;
}

.apt-info h4 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
}

.apt-info p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.apt-status {
  min-width: 80px;
}

.apt-actions {
  display: flex;
  gap: 8px;
}
</style>