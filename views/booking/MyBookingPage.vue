<template>
  <div class="my-booking-page">
    <div class="page-header">
      <h1>我的预约</h1>
      <a-button type="primary" @click="createBooking">
        <icon-plus /> 新建预约
      </a-button>
    </div>

    <!-- 预约统计 -->
    <div class="stats-cards">
      <div class="stat-card" @click="statusFilter = 'all'">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总预约</div>
      </div>
      <div class="stat-card pending" @click="statusFilter = 'pending'">
        <div class="stat-value">{{ stats.pending }}</div>
        <div class="stat-label">待确认</div>
      </div>
      <div class="stat-card confirmed" @click="statusFilter = 'confirmed'">
        <div class="stat-value">{{ stats.confirmed }}</div>
        <div class="stat-label">已确认</div>
      </div>
      <div class="stat-card completed" @click="statusFilter = 'completed'">
        <div class="stat-value">{{ stats.completed }}</div>
        <div class="stat-label">已完成</div>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <a-tabs v-model:active-key="statusFilter" @change="loadBookings">
        <a-tab-pane key="all" title="全部" />
        <a-tab-pane key="pending" title="待确认" />
        <a-tab-pane key="confirmed" title="已确认" />
        <a-tab-pane key="completed" title="已完成" />
        <a-tab-pane key="cancelled" title="已取消" />
      </a-tabs>
    </div>

    <!-- 预约列表 -->
    <div class="booking-list" v-loading="loading">
      <div v-for="booking in filteredBookings" :key="booking.id" class="booking-card">
        <div class="booking-date">
          <div class="month">{{ formatDate(booking.date).month }}</div>
          <div class="day">{{ formatDate(booking.date).day }}</div>
        </div>
        <div class="booking-detail">
          <div class="booking-title">
            <h3>{{ booking.counselorName }}</h3>
            <a-tag :color="getStatusColor(booking.status)" size="small">
              {{ getStatusText(booking.status) }}
            </a-tag>
          </div>
          <p><icon-clock-circle /> {{ booking.date }} {{ booking.timeSlot }}</p>
          <p><icon-tag /> {{ booking.type }}</p>
          <p v-if="booking.remark" class="remark"><icon-message /> {{ booking.remark }}</p>
        </div>
        <div class="booking-actions">
          <a-button 
            v-if="booking.status === 'pending'" 
            status="danger" 
            size="small" 
            @click="cancelBooking(booking.id)"
          >
            取消预约
          </a-button>
          <a-button 
            v-if="booking.status === 'confirmed'" 
            type="primary" 
            size="small" 
            @click="viewDetail(booking.id)"
          >
            查看详情
          </a-button>
          <a-button 
            v-if="booking.status === 'completed' && !booking.hasFeedback" 
            type="primary" 
            size="small" 
            @click="writeFeedback(booking.id)"
          >
            写反馈
          </a-button>
          <a-button 
            v-if="booking.status === 'completed' && booking.hasFeedback" 
            size="small" 
            @click="viewDetail(booking.id)"
          >
            查看反馈
          </a-button>
        </div>
      </div>
    </div>

    <a-empty v-if="!loading && filteredBookings.length === 0" description="暂无预约记录" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()

const loading = ref(false)
const statusFilter = ref('all')
const bookingList = ref<any[]>([])

// 统计数据
const stats = computed(() => {
  const list = bookingList.value
  return {
    total: list.length,
    pending: list.filter(b => b.status === 'pending').length,
    confirmed: list.filter(b => b.status === 'confirmed').length,
    completed: list.filter(b => b.status === 'completed').length
  }
})

// 筛选后的预约列表
const filteredBookings = computed(() => {
  if (statusFilter.value === 'all') {
    return bookingList.value
  }
  return bookingList.value.filter(b => b.status === statusFilter.value)
})

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return {
    month: `${date.getMonth() + 1}月`,
    day: date.getDate()
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'orange',
    confirmed: 'blue',
    completed: 'green',
    cancelled: 'red'
  }
  return map[status] || 'gray'
}

// 加载预约列表
const loadBookings = async () => {
  loading.value = true
  try {
    // TODO: 调用实际API
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据 - 包含完整信息
    bookingList.value = [
      {
        id: 1,
        counselorName: '张老师',
        date: '2025-03-28',
        timeSlot: '10:30-11:30',
        type: '情绪管理咨询',
        remark: '最近压力比较大，想寻求帮助',
        status: 'confirmed',
        hasFeedback: false
      },
      {
        id: 2,
        counselorName: '李老师',
        date: '2025-04-02',
        timeSlot: '14:00-15:00',
        type: '职业规划咨询',
        remark: '职业发展方向迷茫',
        status: 'pending',
        hasFeedback: false
      },
      {
        id: 3,
        counselorName: '王老师',
        date: '2025-03-20',
        timeSlot: '15:30-16:30',
        type: '人际关系咨询',
        remark: '',
        status: 'completed',
        hasFeedback: false
      }
    ]
  } catch (error) {
    Message.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 创建预约
const createBooking = () => {
  router.push('/booking/create')
}

// 取消预约
const cancelBooking = async (id: number) => {
  // TODO: 调用API
  Message.success('已取消预约')
  loadBookings()
}

// 查看详情
const viewDetail = (id: number) => {
  router.push(`/booking/detail/${id}`)
}

// 写反馈
const writeFeedback = (id: number) => {
  router.push(`/booking/feedback/${id}`)
}

onMounted(() => {
  loadBookings()
})
</script>

<style scoped>
.my-booking-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  cursor: pointer;
}

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  border: 1px solid #e5e6eb;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-card .stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1d2129;
}

.stat-card .stat-label {
  color: #86909c;
  margin-top: 8px;
}

.stat-card.pending .stat-value { color: #ff7d00; }
.stat-card.confirmed .stat-value { color: #165dff; }
.stat-card.completed .stat-value { color: #00b42a; }

.filter-tabs {
  margin-bottom: 24px;
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.booking-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e6eb;
  overflow: hidden;
  transition: all 0.3s;
}

.booking-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.booking-date {
  width: 80px;
  background: #f7f8fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.booking-date .month {
  font-size: 14px;
  color: #86909c;
}

.booking-date .day {
  font-size: 28px;
  font-weight: 600;
  color: #1d2129;
}

.booking-detail {
  flex: 1;
  padding: 16px;
}

.booking-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.booking-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.booking-detail p {
  margin: 8px 0;
  color: #4e5969;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.booking-detail .remark {
  color: #86909c;
  font-size: 13px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #e5e6eb;
}

.booking-actions {
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 8px;
}
</style>