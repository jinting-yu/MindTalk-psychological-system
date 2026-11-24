<template>
  <div class="feedback-page">
    <div class="feedback-container">
      <!-- 返回按钮 -->
      <div class="back-button">
        <a-button type="text" @click="goBack">
          <icon-left /> 返回
        </a-button>
      </div>

      <div class="feedback-card">
        <div class="feedback-header">
          <h2>咨询反馈</h2>
          <p>感谢您的反馈，这能帮助我们提供更好的服务</p>
        </div>

        <!-- 咨询信息 -->
        <div class="booking-info">
          <div class="info-row">
            <span class="label">咨询师：</span>
            <span class="value">{{ bookingInfo.counselorName }}</span>
          </div>
          <div class="info-row">
            <span class="label">咨询时间：</span>
            <span class="value">{{ bookingInfo.date }} {{ bookingInfo.timeSlot }}</span>
          </div>
          <div class="info-row">
            <span class="label">咨询类型：</span>
            <span class="value">{{ bookingInfo.type }}</span>
          </div>
        </div>

        <!-- 评分 - 使用星星评分 -->
        <div class="form-section">
          <div class="section-title">
            <span class="required">*</span> 评分
          </div>
          <div class="rating-section">
            <div 
              v-for="i in 5" 
              :key="i" 
              class="rating-star"
              @click="rating = i"
              @mouseenter="hoverRating = i"
              @mouseleave="hoverRating = 0"
            >
              <icon-star-fill v-if="i <= (hoverRating || rating)" class="star active" />
              <icon-star v-else class="star" />
            </div>
            <span class="rating-text">{{ ratingText }}</span>
          </div>
        </div>

        <!-- 反馈内容 -->
        <div class="form-section">
          <div class="section-title">
            <span class="required">*</span> 反馈内容
          </div>
          <a-textarea
            v-model="feedbackContent"
            placeholder="请分享您的咨询体验，这能帮助咨询师更好地为您服务..."
            :rows="6"
            :max-length="500"
            show-word-limit
          />
        </div>

        <!-- 是否愿意推荐 -->
        <div class="form-section">
          <div class="section-title">是否愿意推荐给他人？</div>
          <a-radio-group v-model="recommend">
            <a-radio value="yes">愿意推荐</a-radio>
            <a-radio value="no">暂不推荐</a-radio>
          </a-radio-group>
        </div>

        <!-- 匿名选项 -->
        <div class="form-section">
          <a-checkbox v-model="isAnonymous">
            匿名提交（您的信息将被隐藏）
          </a-checkbox>
        </div>

        <!-- 提交按钮 -->
        <div class="action-buttons">
          <a-button @click="goBack">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="submitFeedback">
            提交反馈
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()

const submitting = ref(false)
const rating = ref(0)
const hoverRating = ref(0)
const feedbackContent = ref('')
const recommend = ref('yes')
const isAnonymous = ref(false)

const bookingInfo = ref({
  id: '',
  counselorName: '',
  date: '',
  timeSlot: '',
  type: ''
})

// 评分文字
const ratingText = computed(() => {
  const texts: Record<number, string> = {
    1: '非常不满意',
    2: '不满意',
    3: '一般',
    4: '满意',
    5: '非常满意'
  }
  return texts[rating.value] || ''
})

// 加载预约信息
const loadBookingInfo = async () => {
  const bookingId = route.params.id
  if (!bookingId) {
    Message.error('预约ID不存在')
    router.push('/booking/list')
    return
  }
  
  try {
    // TODO: 调用API获取预约信息
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 模拟数据
    bookingInfo.value = {
      id: bookingId as string,
      counselorName: '王老师',
      date: '2025-03-20',
      timeSlot: '15:30-16:30',
      type: '人际关系咨询'
    }
  } catch (error) {
    Message.error('加载失败')
  }
}

// 提交反馈
const submitFeedback = async () => {
  if (rating.value === 0) {
    Message.warning('请选择评分')
    return
  }
  if (!feedbackContent.value.trim()) {
    Message.warning('请填写反馈内容')
    return
  }
  
  submitting.value = true
  try {
    // TODO: 调用API提交反馈
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    Message.success('反馈提交成功，感谢您的评价！')
    router.push('/booking/list')
  } catch (error) {
    Message.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadBookingInfo()
})
</script>

<style scoped>
.feedback-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 120px);
  background: #f5f7fa;
}

.feedback-container {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.back-button {
  margin-bottom: 20px;
}

.feedback-header {
  text-align: center;
  margin-bottom: 32px;
}

.feedback-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.feedback-header p {
  color: #64748b;
  font-size: 14px;
}

.booking-info {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  width: 80px;
  color: #64748b;
  font-size: 14px;
}

.info-row .value {
  flex: 1;
  color: #1e293b;
  font-size: 14px;
  font-weight: 500;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 12px;
}

.section-title .required {
  color: #f53f3f;
  margin-right: 4px;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.rating-star {
  cursor: pointer;
  transition: transform 0.2s;
}

.rating-star:hover {
  transform: scale(1.1);
}

.star {
  font-size: 32px;
  color: #e5e6eb;
}

.star.active {
  color: #ffb400;
}

.rating-text {
  margin-left: 12px;
  font-size: 14px;
  color: #64748b;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e6eb;
}

:deep(.arco-textarea) {
  border-radius: 12px;
}

:deep(.arco-radio-group) {
  display: flex;
  gap: 24px;
}

:deep(.arco-checkbox) {
  margin: 0;
}
</style>