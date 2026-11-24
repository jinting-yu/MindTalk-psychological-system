<template>
  <div class="booking-detail-page">
    <div class="detail-container">
      <!-- 返回按钮 -->
      <div class="back-button">
        <a-button type="text" @click="goBack">
          <icon-left /> 返回列表
        </a-button>
      </div>

      <!-- 预约详情卡片 -->
      <div class="detail-card" v-loading="loading">
        <!-- 状态头部 -->
        <div class="detail-header" :class="getStatusClass(bookingDetail.status)">
          <div class="status-badge">
            <a-tag :color="getStatusColor(bookingDetail.status)" size="large">
              {{ getStatusText(bookingDetail.status) }}
            </a-tag>
          </div>
          <div class="booking-id">预约编号：{{ bookingDetail.id }}</div>
        </div>

        <!-- 咨询师信息 -->
        <div class="info-section">
          <h3>👨‍⚕️ 咨询师信息</h3>
          <div class="counselor-info">
            <a-avatar :size="64" :style="{ backgroundColor: '#3b82f6' }">
              {{ bookingDetail.counselorName?.charAt(0) }}
            </a-avatar>
            <div class="counselor-detail">
              <div class="counselor-name">{{ bookingDetail.counselorName }}</div>
              <div class="counselor-title">{{ bookingDetail.counselorTitle || '心理咨询师' }}</div>
              <div class="counselor-specialty">擅长领域：{{ bookingDetail.counselorSpecialty || '情绪管理、压力调适' }}</div>
            </div>
          </div>
        </div>

        <!-- 预约信息 -->
        <div class="info-section">
          <h3>📅 预约信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">预约日期</div>
              <div class="info-value">{{ bookingDetail.date }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">预约时段</div>
              <div class="info-value">{{ bookingDetail.timeSlot }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">咨询类型</div>
              <div class="info-value">{{ bookingDetail.type }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">咨询方式</div>
              <div class="info-value">{{ bookingDetail.mode === 'online' ? '线上咨询' : '线下咨询' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">创建时间</div>
              <div class="info-value">{{ bookingDetail.createTime }}</div>
            </div>
            <div class="info-item" v-if="bookingDetail.confirmTime">
              <div class="info-label">确认时间</div>
              <div class="info-value">{{ bookingDetail.confirmTime }}</div>
            </div>
          </div>
        </div>

        <!-- 备注信息 -->
        <div class="info-section" v-if="bookingDetail.remark">
          <h3>📝 备注信息</h3>
          <div class="remark-content">
            {{ bookingDetail.remark }}
          </div>
        </div>

        <!-- 咨询准备（已确认状态） -->
        <div class="info-section" v-if="bookingDetail.status === 'confirmed'">
          <h3>💡 咨询准备</h3>
          <div class="prepare-content">
            <p><strong>线上咨询：</strong></p>
            <ul>
              <li>请提前5分钟进入会议室</li>
              <li>确保网络稳定，设备正常</li>
              <li>选择安静、私密的环境</li>
              <li>会议链接：<a-link>{{ bookingDetail.meetingLink || 'https://meet.example.com/123456' }}</a-link></li>
            </ul>
            <p><strong>线下咨询：</strong></p>
            <ul>
              <li>地址：东莞城市学院</li>
              <li>请提前10分钟到达</li>
              <li>携带身份证件登记</li>
            </ul>
          </div>
        </div>

        <!-- 咨询反馈（已完成状态） -->
        <div class="info-section" v-if="bookingDetail.status === 'completed' && bookingDetail.feedback">
          <h3>📋 咨询反馈</h3>
          <div class="feedback-content">
            <div class="feedback-rating">
              <span>评分：</span>
              <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= bookingDetail.rating }">★</span>
            </div>
            <div class="feedback-text">{{ bookingDetail.feedback }}</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <a-button 
            v-if="bookingDetail.status === 'pending'" 
            type="primary" 
            @click="cancelBooking"
            status="danger"
          >
            取消预约
          </a-button>
          <a-button 
            v-if="bookingDetail.status === 'confirmed'" 
            type="primary" 
            @click="enterConsultation"
          >
            进入咨询
          </a-button>
          <a-button 
            v-if="bookingDetail.status === 'completed' && !bookingDetail.hasFeedback" 
            type="primary" 
            @click="writeFeedback"
          >
            写反馈
          </a-button>
          <a-button @click="goBack">返回</a-button>
        </div>
      </div>

      <!-- 右侧侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-card">
          <h3>温馨提示</h3>
          <ul>
            <li>请按时参加咨询，迟到时间会计入咨询时长</li>
            <li>如需改期，请提前24小时联系客服</li>
            <li>紧急情况请联系：020-12345678</li>
          </ul>
        </div>
        <div class="sidebar-card">
          <h3>常见问题</h3>
          <a-collapse :bordered="false">
            <a-collapse-item header="咨询前需要准备什么？">
              <p>保持放松的心态，准备好想讨论的问题即可。</p>
            </a-collapse-item>
            <a-collapse-item header="可以更改咨询时间吗？">
              <p>可以，请提前24小时联系客服或咨询师协商改期。</p>
            </a-collapse-item>
            <a-collapse-item header="咨询内容会保密吗？">
              <p>是的，所有咨询内容严格保密，请放心。</p>
            </a-collapse-item>
          </a-collapse>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const bookingDetail = ref({
  id: '',
  counselorName: '',
  counselorTitle: '',
  counselorSpecialty: '',
  date: '',
  timeSlot: '',
  type: '',
  mode: 'online',
  status: '',
  remark: '',
  createTime: '',
  confirmTime: '',
  meetingLink: '',
  feedback: '',
  rating: 0,
  hasFeedback: false
})

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

// 获取状态样式类
const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    pending: 'status-pending',
    confirmed: 'status-confirmed',
    completed: 'status-completed',
    cancelled: 'status-cancelled'
  }
  return map[status] || ''
}

// 加载预约详情
const loadBookingDetail = async () => {
  const bookingId = route.params.id
  if (!bookingId) {
    Message.error('预约ID不存在')
    router.push('/booking/list')
    return
  }
  
  loading.value = true
  try {
    // TODO: 调用实际API
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据 - 根据ID返回不同内容
    const mockData: Record<string, any> = {
      '1': {
        id: '1',
        counselorName: '张老师',
        counselorTitle: '情绪管理专家',
        counselorSpecialty: '焦虑抑郁、情绪调节',
        date: '2025-03-28',
        timeSlot: '10:30-11:30',
        type: '情绪管理咨询',
        mode: 'online',
        status: 'confirmed',
        remark: '最近压力比较大，想寻求帮助',
        createTime: '2025-03-25 14:30',
        confirmTime: '2025-03-26 09:00',
        meetingLink: 'https://meet.example.com/123456'
      },
      '2': {
        id: '2',
        counselorName: '李老师',
        counselorTitle: '职业心理咨询师',
        counselorSpecialty: '职业规划、职场发展',
        date: '2025-04-02',
        timeSlot: '14:00-15:00',
        type: '职业规划咨询',
        mode: 'offline',
        status: 'pending',
        remark: '职业发展方向迷茫',
        createTime: '2025-03-26 10:15'
      },
      '3': {
        id: '3',
        counselorName: '王老师',
        counselorTitle: '人际关系指导师',
        counselorSpecialty: '人际关系、社交焦虑',
        date: '2025-03-20',
        timeSlot: '15:30-16:30',
        type: '人际关系咨询',
        mode: 'online',
        status: 'completed',
        remark: '',
        createTime: '2025-03-18 09:00',
        confirmTime: '2025-03-18 14:00',
        feedback: '咨询师非常专业，给了我很多实用的建议，感觉收获很大！',
        rating: 5,
        hasFeedback: true
      }
    }
    
    const data = mockData[bookingId as string]
    if (data) {
      bookingDetail.value = data
    } else {
      Message.error('预约记录不存在')
      router.push('/booking/list')
    }
  } catch (error) {
    Message.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 取消预约
const cancelBooking = async () => {
  // TODO: 调用API取消预约
  Message.success('已取消预约')
  router.push('/booking/list')
}

// 进入咨询
const enterConsultation = () => {
  if (bookingDetail.value.mode === 'online') {
    // 打开视频会议链接
    window.open(bookingDetail.value.meetingLink, '_blank')
  } else {
    // 显示线下地址
    Message.info('线下咨询地址：东莞城市学院')
  }
}

// 写反馈
const writeFeedback = () => {
  router.push(`/booking/feedback/${bookingDetail.value.id}`)
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadBookingDetail()
})
</script>

<style scoped>
.booking-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.detail-container {
  display: flex;
  gap: 24px;
}

.detail-card {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.back-button {
  margin-bottom: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid #e5e6eb;
}

.detail-header.status-pending {
  border-bottom-color: #ff7d00;
}

.detail-header.status-confirmed {
  border-bottom-color: #165dff;
}

.detail-header.status-completed {
  border-bottom-color: #00b42a;
}

.booking-id {
  font-size: 13px;
  color: #86909c;
}

.info-section {
  margin-bottom: 32px;
}

.info-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1e293b;
}

.counselor-info {
  display: flex;
  gap: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.counselor-detail {
  flex: 1;
}

.counselor-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.counselor-title {
  font-size: 14px;
  color: #3b82f6;
  margin-bottom: 8px;
}

.counselor-specialty {
  font-size: 13px;
  color: #64748b;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 12px;
  color: #86909c;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.remark-content {
  background: #fef9e7;
  padding: 16px;
  border-radius: 12px;
  color: #1e293b;
  line-height: 1.6;
  border-left: 3px solid #ffb400;
}

.prepare-content {
  background: #eef2ff;
  padding: 16px;
  border-radius: 12px;
  color: #1e293b;
  line-height: 1.6;
}

.prepare-content ul {
  margin: 8px 0 16px;
  padding-left: 20px;
}

.prepare-content li {
  margin: 6px 0;
  font-size: 14px;
}

.feedback-content {
  background: #e8f3ed;
  padding: 16px;
  border-radius: 12px;
}

.feedback-rating {
  margin-bottom: 12px;
}

.star {
  color: #e2e8f0;
  font-size: 16px;
  margin: 0 2px;
}

.star.active {
  color: #ffb400;
}

.feedback-text {
  line-height: 1.6;
  color: #1e293b;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e6eb;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
}

.sidebar-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.sidebar-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1e293b;
}

.sidebar-card ul {
  padding-left: 20px;
  margin: 0;
}

.sidebar-card li {
  margin: 12px 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

:deep(.arco-collapse) {
  background: transparent;
}

:deep(.arco-collapse-item-header) {
  font-size: 14px;
  font-weight: 500;
  padding: 12px 0;
}

:deep(.arco-collapse-item-content) {
  font-size: 13px;
  color: #64748b;
  padding: 0 0 12px 0;
}

@media (max-width: 768px) {
  .detail-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>