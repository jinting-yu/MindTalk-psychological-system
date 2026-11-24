<template>
  <div class="user-profile-page">
    <!-- 页面头部返回按钮 -->
    <div class="page-header">
      <a-button class="back-btn" @click="goBack">
        <icon-left /> 返回
      </a-button>
    </div>

    <div class="profile-container">
      <!-- 用户信息卡片 -->
      <div class="profile-card">
        <div class="profile-avatar">
          <a-avatar :size="100">
            {{ userInfo.userName?.charAt(0) || userInfo.userAccount?.charAt(0) || 'U' }}
          </a-avatar>
          <a-button type="text" size="small" @click="changeAvatar">更换头像</a-button>
        </div>
        <div class="profile-info">
          <h2>{{ userInfo.userName || userInfo.userAccount || '用户' }}</h2>
          <p><icon-user /> 用户名：{{ userInfo.userAccount || userInfo.userName }}</p>
          <p><icon-email /> 邮箱：{{ userInfo.email || '未绑定' }}</p>
          <p><icon-phone /> 手机号：{{ userInfo.phone || '未绑定' }}</p>
          <p><icon-calendar /> 注册时间：{{ formatDate(userInfo.createTime) || '2025-01-01' }}</p>
        </div>
        <div class="profile-stats">
          <div class="stat-item">
            <div class="stat-value">{{ stats.testCount }}</div>
            <div class="stat-label">完成测评</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.bookingCount }}</div>
            <div class="stat-label">预约次数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.favoriteCount }}</div>
            <div class="stat-label">收藏内容</div>
          </div>
        </div>
      </div>

      <!-- 最近测评 -->
      <div class="section">
        <div class="section-header">
          <h3>最近测评</h3>
          <a-link @click="goToMyAnswers">查看全部 →</a-link>
        </div>
        
        <div v-if="loading" class="loading-state">
          <a-spin /> 加载中...
        </div>
        
        <div v-else-if="recentAnswers.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <p>暂无测评记录</p>
          <a-button type="primary" size="small" @click="goToTestList">去完成一个测评</a-button>
        </div>
        
        <div v-else class="recent-list">
          <div v-for="answer in recentAnswers" :key="answer.id" class="recent-item">
            <div class="recent-icon">{{ getScaleIcon(answer.scaleName) }}</div>
            <div class="recent-info">
              <h4>{{ answer.scaleName || '心理测评' }}</h4>
              <p>完成时间：{{ formatDate(answer.createTime) }}</p>
              <div class="score-info">
                <span class="score">{{ answer.totalScore }}分</span>
                <a-tag :color="getLevelColor(answer.level)" size="small">{{ answer.level || '正常' }}</a-tag>
              </div>
            </div>
            <a-button type="text" @click="viewResult(answer.id)">查看结果</a-button>
          </div>
        </div>
      </div>

      <!-- 最近预约 -->
      <div class="section">
        <div class="section-header">
          <h3>最近预约</h3>
          <a-link @click="goToMyBookings">查看全部 →</a-link>
        </div>
        <div class="recent-list">
          <div v-for="booking in recentBookings" :key="booking.id" class="recent-item">
            <div class="recent-icon">📅</div>
            <div class="recent-info">
              <h4>{{ booking.counselorName }}</h4>
              <p>{{ booking.date }} {{ booking.timeSlot }}</p>
            </div>
            <a-tag :color="getStatusColor(booking.status)">{{ getStatusText(booking.status) }}</a-tag>
          </div>
        </div>
      </div>

      <!-- 紧急联系人模块 -->
      <div class="section">
        <div class="section-header">
          <h3>📞 紧急联系人</h3>
          <a-link @click="goToEmergencyContact">去设置 →</a-link>
        </div>
        <div class="emergency-contact-entry" @click="goToEmergencyContact">
          <div class="emergency-icon">🆘</div>
          <div class="emergency-info">
            <h4>紧急联系人设置</h4>
            <p>添加紧急联系人，在需要时能快速获得帮助</p>
          </div>
          <div class="emergency-arrow">→</div>
        </div>
      </div>

      <!-- 编辑按钮 -->
      <div class="actions">
        <a-button type="primary" long @click="editProfile">编辑资料</a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/store/userStore'
import { Message } from '@arco-design/web-vue'
import { ref, onMounted, onActivated } from 'vue'
import { 
  IconUser, 
  IconEmail, 
  IconPhone, 
  IconCalendar,
  IconLeft 
} from '@arco-design/web-vue/es/icon'
import request from '@/request'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const userInfo = ref<any>({})
const stats = ref({
  testCount: 0,
  bookingCount: 3,
  favoriteCount: 5
})
const recentAnswers = ref<any[]>([])
const recentBookings = ref([
  { id: 1, counselorName: '张老师', date: '2025-03-28', timeSlot: '10:30-11:30', status: 'confirmed' }
])

// 量表名称映射（根据 scaleId）
const getScaleNameById = (scaleId: number): string => {
  const scaleMap: Record<number, string> = {
    1: '心理健康检测',
    2: 'SAS焦虑自评量表',
    3: '抑郁自评量表(SDS)',
    4: '人际关系综合诊断',
    5: '艾森克人格问卷(EPQ)',
    6: '压力应对方式问卷'
  }
  return scaleMap[scaleId] || '心理测评'
}

// 格式化日期
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 获取量表图标
const getScaleIcon = (scaleName: string) => {
  if (!scaleName) return '📋'
  if (scaleName.includes('人格') || scaleName.includes('MBTI')) return '🔮'
  if (scaleName.includes('焦虑')) return '😟'
  if (scaleName.includes('抑郁')) return '😔'
  if (scaleName.includes('压力')) return '😫'
  if (scaleName.includes('睡眠')) return '😴'
  if (scaleName.includes('人际')) return '👥'
  if (scaleName.includes('学习')) return '📚'
  return '📋'
}

// 获取等级颜色
const getLevelColor = (level: string) => {
  if (!level) return 'gray'
  const colorMap: Record<string, string> = {
    '正常': 'green',
    '轻度': 'orange',
    '中度': 'orangered',
    '重度': 'red',
    '重度焦虑': 'red',
    '重度抑郁': 'red'
  }
  return colorMap[level] || 'gray'
}

// 获取最新测评记录 - 直接调用后端 /api/exam/record 接口
const fetchLatestExamRecords = async () => {
  console.log('========== 开始获取测评记录 ==========')
  loading.value = true
  try {
    // 直接调用后端接口 /api/exam/record
    const res = await request({
      url: '/api/exam/record',
      method: 'GET'
    })
    
    console.log('API返回:', res)
    
    // 处理后端返回格式: { code: 20000, data: [...] } 或 { code: 200, data: [...] }
    let records = []
    if (res.code === 20000 || res.code === 200) {
      records = res.data || []
    } else if (res.data && (res.data.code === 20000 || res.data.code === 200)) {
      records = res.data.data || []
    } else if (Array.isArray(res)) {
      records = res
    } else if (res.data && Array.isArray(res.data)) {
      records = res.data
    }
    
    console.log('提取的记录:', records)
    console.log('记录数量:', records.length)
    
    // 打印第一条记录的结构
    if (records.length > 0) {
      console.log('第一条记录字段:', Object.keys(records[0]))
      console.log('第一条记录内容:', records[0])
    }
    
    // 字段映射 - 关键修复：正确处理字段名
    recentAnswers.value = records.map((record: any) => ({
      id: record.id,
      // 优先使用 scaleName，如果没有则根据 scaleId 映射
      scaleName: record.scaleName || getScaleNameById(record.scaleId),
      // 分数：后端可能返回 totalScore 或 score
      totalScore: record.totalScore ?? record.score ?? 0,
      level: record.level || '正常',
      createTime: record.createTime,
      risk: record.risk
    }))
    
    // 更新统计数字
    stats.value.testCount = recentAnswers.value.length
    
    console.log('映射后的数据:', recentAnswers.value)
    
  } catch (error) {
    console.error('获取测评记录失败:', error)
    Message.error('获取测评记录失败')
  } finally {
    loading.value = false
    console.log('========== 获取测评记录结束 ==========')
  }
}

// 加载用户数据
const loadUserData = async () => {
  const loginUser = loginUserStore.loginUser
  console.log('登录用户:', loginUser)
  if (loginUser) {
    userInfo.value = loginUser
  }
}

const goBack = () => router.back()
const getStatusText = (status: string) => ({ pending: '待确认', confirmed: '已确认', completed: '已完成' }[status] || status)
const getStatusColor = (status: string) => ({ pending: 'orange', confirmed: 'blue', completed: 'green' }[status] || 'gray')
const editProfile = () => router.push('/user/profile/edit')
const changeAvatar = () => Message.info('头像更换功能开发中')
const viewResult = (id: number) => {
  if (id) {
    router.push(`/answer/result/${id}`)
  } else {
    Message.warning('无法查看结果，缺少记录ID')
  }
}
const goToMyAnswers = () => router.push('/answer/my')
const goToTestList = () => router.push('/test/list')
const goToMyBookings = () => router.push('/booking/my')
const goToEmergencyContact = () => router.push('/emergency-contact')

onMounted(() => {
  loadUserData()
  fetchLatestExamRecords()
})

onActivated(() => {
  console.log('页面激活，重新获取测评记录')
  fetchLatestExamRecords()
})
</script>

<style scoped lang="scss">
/* 样式保持不变 */
.user-profile-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;

  .page-header {
    margin-bottom: 20px;
    .back-btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      border-radius: 28px;
      border-color: #9bcd9c;
      color: #7cb87a;
      background: transparent;
      transition: all 0.3s;
      &:hover {
        background: #f6fbf4;
        border-color: #7cb87a;
        color: #5a9e5b;
        transform: translateX(-2px);
      }
    }
  }

  .profile-container {
    background: #fff;
    border-radius: 32px;
    padding: 32px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  }

  .profile-card {
    display: flex;
    gap: 32px;
    padding-bottom: 32px;
    border-bottom: 1px solid #e8ecd9;
    margin-bottom: 32px;
    @media (max-width: 600px) {
      flex-direction: column;
      text-align: center;
    }

    .profile-avatar {
      text-align: center;
      :deep(.arco-avatar) {
        background: linear-gradient(135deg, #9bcd9c, #7cb87a);
        font-size: 40px;
        font-weight: 500;
      }
      button {
        margin-top: 12px;
        display: block;
        width: 100%;
        color: #7cb87a;
        &:hover {
          color: #5a9e5b;
        }
      }
    }

    .profile-info {
      flex: 1;
      h2 {
        font-size: 24px;
        margin-bottom: 16px;
        color: #2c5f2d;
      }
      p {
        margin: 12px 0;
        color: #5a7c5b;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .profile-stats {
      display: flex;
      gap: 32px;
      align-items: center;
      .stat-item {
        text-align: center;
        .stat-value {
          font-size: 28px;
          font-weight: 600;
          color: #7cb87a;
        }
        .stat-label {
          font-size: 13px;
          color: #8ba888;
        }
      }
    }
  }

  .section {
    margin-bottom: 32px;
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      h3 {
        font-size: 18px;
        font-weight: 600;
        margin: 0;
        color: #2c5f2d;
      }
      :deep(.arco-link) {
        color: #7cb87a;
        &:hover {
          color: #5a9e5b;
        }
      }
    }
  }

  .recent-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .recent-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px;
    background: #fefef8;
    border-radius: 16px;
    border: 1px solid #e8ecd9;
    
    .recent-icon {
      font-size: 24px;
    }
    
    .recent-info {
      flex: 1;
      h4 {
        margin: 0 0 4px;
        font-size: 14px;
        color: #5a7c5b;
      }
      p {
        margin: 0;
        font-size: 12px;
        color: #8ba888;
      }
      .score-info {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: 6px;
        .score {
          font-size: 14px;
          font-weight: 600;
          color: #7cb87a;
        }
      }
    }
    
    :deep(.arco-btn-text) {
      color: #7cb87a;
      &:hover {
        color: #5a9e5b;
      }
    }
  }

  .actions {
    margin-top: 32px;
    :deep(.arco-btn-primary) {
      background: linear-gradient(135deg, #9bcd9c 0%, #7cb87a 100%);
      border: none;
      border-radius: 32px;
      height: 44px;
      font-size: 16px;
      font-weight: 500;
      &:hover {
        background: linear-gradient(135deg, #7cb87a 0%, #5a9e5b 100%);
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(124, 184, 122, 0.3);
      }
    }
  }
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 48px;
  color: #8ba888;
}

.empty-state {
  text-align: center;
  padding: 48px;
  background: #fefef8;
  border-radius: 16px;
  border: 1px solid #e8ecd9;
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.5;
  }
  p {
    color: #8ba888;
    margin-bottom: 16px;
  }
}

.emergency-contact-entry {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ffccc7;
  &:hover {
    transform: translateX(4px);
    background: linear-gradient(135deg, #ffe8e8 0%, #ffd6d6 100%);
    box-shadow: 0 4px 12px rgba(255, 77, 79, 0.2);
  }
  .emergency-icon {
    font-size: 32px;
  }
  .emergency-info {
    flex: 1;
    h4 {
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 600;
      color: #ff4d4f;
    }
    p {
      margin: 0;
      font-size: 12px;
      color: #ff7875;
    }
  }
  .emergency-arrow {
    font-size: 20px;
    color: #ff4d4f;
  }
}
</style>