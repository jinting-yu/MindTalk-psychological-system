<template>
  <div class="booking-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>心理预约</h1>
        <p>专业心理咨询师，一对一保密服务</p>
      </div>
      <a-button type="primary" size="large" @click="createBooking">
        <icon-plus /> 新建预约
      </a-button>
    </div>

    <!-- 咨询师推荐 -->
    <div class="counselors-section">
      <h2>推荐咨询师</h2>
      <div class="counselors-grid">
        <div v-for="counselor in counselors" :key="counselor.id" class="counselor-card">
          <a-avatar :size="80" :style="{ backgroundColor: counselor.color }">
            {{ counselor.name.charAt(0) }}
          </a-avatar>
          <h3>{{ counselor.name }}</h3>
          <p>{{ counselor.title }}</p>
          <div class="rating">
            <icon-star-fill v-for="i in 5" :key="i" :style="{ color: i <= counselor.rating ? '#ffb400' : '#e5e6eb' }" />
          </div>
          <a-button type="outline" size="small" @click="bookCounselor(counselor.id)">预约</a-button>
        </div>
      </div>
    </div>

    <!-- 预约筛选 -->
    <div class="filter-section">
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
      <div v-for="booking in bookingList" :key="booking.id" class="booking-item">
        <div class="booking-info">
          <div class="booking-header">
            <h3>{{ booking.counselorName }}</h3>
            <a-tag :color="getStatusColor(booking.status)">
              {{ getStatusText(booking.status) }}
            </a-tag>
          </div>
          <p><icon-calendar /> {{ booking.date }} {{ booking.timeSlot }}</p>
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
            v-if="booking.status === 'completed'" 
            size="small" 
            @click="writeFeedback(booking.id)"
          >
            写反馈
          </a-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <a-empty v-if="!loading && bookingList.length === 0" description="暂无预约记录" />

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <a-pagination
        v-model:current="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        show-total
        @change="loadBookings"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()

// 数据
const loading = ref(false)
const statusFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const bookingList = ref<any[]>([])

// 咨询师列表
const counselors = ref([
  { id: 1, name: '张老师', title: '情绪管理专家', rating: 5, color: '#3b82f6' },
  { id: 2, name: '李老师', title: '职业心理咨询师', rating: 5, color: '#a855f7' },
  { id: 3, name: '王老师', title: '人际关系指导师', rating: 4, color: '#10b981' },
  { id: 4, name: '陈老师', title: '家庭治疗师', rating: 5, color: '#f59e0b' }
])

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
    bookingList.value = [
      {
        id: 1,
        counselorName: '张老师',
        date: '2025-03-28',
        timeSlot: '10:30-11:30',
        type: '情绪管理咨询',
        remark: '最近压力比较大，想寻求帮助',
        status: 'confirmed'
      },
      {
        id: 2,
        counselorName: '李老师',
        date: '2025-04-02',
        timeSlot: '14:00-15:00',
        type: '职业规划咨询',
        remark: '职业发展方向迷茫',
        status: 'pending'
      },
      {
        id: 3,
        counselorName: '王老师',
        date: '2025-03-20',
        timeSlot: '15:30-16:30',
        type: '人际关系咨询',
        status: 'completed'
      }
    ]
    total.value = 3
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

// 预约咨询师
const bookCounselor = (counselorId: number) => {
  router.push(`/booking/create?counselorId=${counselorId}`)
}

// 取消预约
const cancelBooking = async (id: number) => {
  // TODO: 调用API取消预约
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

<style scoped lang="scss">
.booking-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;

    h1 {
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 8px;
    }

    p {
      color: #86909c;
    }
  }

  .counselors-section {
    margin-bottom: 40px;
    padding: 24px;
    background: #f7f8fa;
    border-radius: 16px;

    h2 {
      font-size: 20px;
      margin-bottom: 20px;
    }

    .counselors-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
    }

    .counselor-card {
      text-align: center;
      padding: 20px;
      background: #fff;
      border-radius: 12px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      h3 {
        margin: 12px 0 4px;
        font-size: 16px;
      }

      p {
        color: #86909c;
        font-size: 13px;
        margin-bottom: 8px;
      }

      .rating {
        margin: 12px 0;
        font-size: 12px;
      }
    }
  }

  .filter-section {
    margin-bottom: 24px;
  }

  .booking-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .booking-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    border: 1px solid #e5e6eb;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    }

    .booking-info {
      flex: 1;

      .booking-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        h3 {
          font-size: 16px;
          font-weight: 600;
          margin: 0;
        }
      }

      p {
        margin: 8px 0;
        color: #4e5969;
        font-size: 14px;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .remark {
        color: #86909c;
        font-size: 13px;
        margin-top: 8px;
        padding-top: 8px;
        border-top: 1px dashed #e5e6eb;
      }
    }

    .booking-actions {
      display: flex;
      gap: 12px;
      margin-left: 20px;
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 32px;
  }
}
</style>