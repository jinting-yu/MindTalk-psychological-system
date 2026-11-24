<template>
  <div class="create-booking-page">
    <div class="page-header">
      <a-button type="text" @click="goBack">
        <icon-left /> 返回
      </a-button>
      <h1>新建预约</h1>
    </div>

    <div class="booking-form-container">
      <!-- 左侧表单区域 -->
      <div class="form-area">
        <!-- 学生信息卡片（管理员端显示） -->
        <div v-if="isAdmin" class="student-info-card">
          <div class="card-header">
            <h3>📋 学生信息</h3>
            <div class="header-actions">
              <a-button type="text" size="small" @click="handleContactStudent">
                <icon-phone /> 联系学生
              </a-button>
              <a-button type="text" size="small" @click="handleViewArchive">
                <icon-file /> 查看档案
              </a-button>
            </div>
          </div>
          <div class="student-info-content">
            <div class="info-row">
              <div class="info-item">
                <span class="label">姓名：</span>
                <span class="value">
                  {{ studentInfo.name }}
                  <a-tag :color="studentInfo.gender === '男' ? 'blue' : 'magenta'" size="small">
                    {{ studentInfo.gender }}
                  </a-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="label">学号：</span>
                <span class="value">{{ studentInfo.studentId }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <span class="label">班级：</span>
                <span class="value">{{ studentInfo.className }}</span>
              </div>
              <div class="info-item">
                <span class="label">学院：</span>
                <span class="value">{{ studentInfo.college }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <span class="label">联系电话：</span>
                <span class="value">{{ studentInfo.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">紧急联系人：</span>
                <span class="value">
                  {{ studentInfo.emergencyContact.name }}({{ studentInfo.emergencyContact.relation }}) 
                  {{ studentInfo.emergencyContact.phone }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 预约表单 -->
        <a-form :model="form" :rules="rules" ref="formRef" layout="vertical" @submit="handleSubmit">
          <!-- 选择咨询师 -->
          <a-form-item label="选择咨询师" field="counselorId" required>
            <a-select v-model="form.counselorId" placeholder="请选择咨询师" allow-clear>
              <a-option v-for="counselor in counselors" :key="counselor.id" :value="counselor.id">
                <div class="counselor-option">
                  <span class="name">{{ counselor.name }}</span>
                  <span class="title"> - {{ counselor.title }}</span>
                </div>
              </a-option>
            </a-select>
          </a-form-item>

          <!-- 预约日期 -->
          <a-form-item label="预约日期" field="date" required>
            <a-date-picker 
              v-model="form.date" 
              :disabled-date="disabledDate"
              placeholder="请选择日期"
              style="width: 100%"
            />
          </a-form-item>

          <!-- 预约时段 - 使用网格布局（第二个文件的样式更现代） -->
          <a-form-item label="预约时段" field="timeSlot" required>
            <div class="time-slots-grid">
              <a-button
                v-for="slot in timeSlots"
                :key="slot.value"
                :type="form.timeSlot === slot.value ? 'primary' : 'outline'"
                :class="{ 'slot-active': form.timeSlot === slot.value }"
                @click="form.timeSlot = slot.value"
                class="time-slot-btn"
              >
                {{ slot.label }}
              </a-button>
            </div>
          </a-form-item>

          <!-- 咨询类型 -->
          <a-form-item label="咨询类型" field="type" required>
            <a-select v-model="form.type" placeholder="请选择咨询类型" allow-clear>
              <a-option value="情绪管理">😊 情绪管理咨询</a-option>
              <a-option value="职业规划">💼 职业规划咨询</a-option>
              <a-option value="人际关系">👥 人际关系咨询</a-option>
              <a-option value="家庭关系">🏠 家庭关系咨询</a-option>
              <a-option value="压力调适">🧘 压力调适咨询</a-option>
              <a-option value="自我成长">🌱 自我成长咨询</a-option>
            </a-select>
          </a-form-item>

          <!-- 咨询方式 -->
          <a-form-item label="咨询方式" field="mode" required>
            <a-radio-group v-model="form.mode">
              <a-radio value="online">💻 线上咨询</a-radio>
              <a-radio value="offline">🏢 线下咨询</a-radio>
            </a-radio-group>
            <div v-if="form.mode === 'offline'" class="location-tip">
              📍 线下咨询地址：东莞城市学院
            </div>
          </a-form-item>

          <!-- 备注 -->
          <a-form-item label="备注" field="remark">
            <a-textarea 
              v-model="form.remark" 
              placeholder="请简要描述您想咨询的问题或期望..."
              :max-length="200"
              show-word-limit
              :rows="4"
            />
          </a-form-item>

          <!-- 提交按钮 -->
          <a-form-item>
            <a-button type="primary" html-type="submit" :loading="submitting" long size="large">
              提交预约
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 右侧预约须知（结合两者优点） -->
      <div class="booking-info">
        <h3>📋 预约须知</h3>
        <ul>
          <li>⏰ 请提前24小时预约，以便咨询师做好准备</li>
          <li>⏱️ 每次咨询时长约50分钟</li>
          <li>🔄 如需取消或改期，请提前联系客服</li>
          <li>🎁 首次咨询可享受免费体验（30分钟）</li>
          <li>🔒 所有咨询内容严格保密</li>
          <li>📍 线下咨询地址：东莞城市学院</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()

// 判断是否为管理员端（可根据路由或权限判断）
const isAdmin = computed(() => {
  return route.path.includes('/admin') || route.query.fromAdmin === 'true'
})

// 表单引用
const formRef = ref()

// 提交状态
const submitting = ref(false)

// 学生信息（管理员端显示）
const studentInfo = reactive({
  name: '张三',
  gender: '男',
  studentId: '2024001001',
  className: '心理学院1班',
  college: '心理学院',
  phone: '13800138000',
  emergencyContact: {
    name: '李四',
    relation: '父亲',
    phone: '13900139000'
  }
})

// 咨询师列表（合并两个文件）
const counselors = ref([
  { id: 1, name: '张老师', title: '情绪管理专家' },
  { id: 2, name: '李老师', title: '职业心理咨询师' },
  { id: 3, name: '王老师', title: '人际关系指导师' },
  { id: 4, name: '陈老师', title: '家庭治疗师' }
])

// 时段选项
const timeSlots = [
  { label: '09:00 - 10:00', value: '09:00-10:00' },
  { label: '10:30 - 11:30', value: '10:30-11:30' },
  { label: '14:00 - 15:00', value: '14:00-15:00' },
  { label: '15:30 - 16:30', value: '15:30-16:30' },
  { label: '17:00 - 18:00', value: '17:00-18:00' }
]

// 表单数据
const form = reactive({
  counselorId: undefined as number | undefined,
  date: '',
  timeSlot: '',
  type: '',
  mode: 'online',
  remark: ''
})

// 表单校验规则
const rules = {
  counselorId: [{ required: true, message: '请选择咨询师' }],
  date: [{ required: true, message: '请选择预约日期' }],
  timeSlot: [{ required: true, message: '请选择预约时段' }],
  type: [{ required: true, message: '请选择咨询类型' }]
}

// 禁用过去的日期
const disabledDate = (current: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return current < today
}

// 联系学生（管理员功能）
const handleContactStudent = () => {
  Modal.info({
    title: '联系学生',
    content: `电话：${studentInfo.phone}\n紧急联系人：${studentInfo.emergencyContact.name} ${studentInfo.emergencyContact.phone}`,
    okText: '知道了'
  })
}

// 查看档案（管理员功能）
const handleViewArchive = () => {
  router.push(`/user/archive/${studentInfo.studentId}`)
}

// 提交表单
const handleSubmit = async () => {
  const errors = await formRef.value?.validate()
  if (errors) return

  submitting.value = true
  try {
    const selectedCounselor = counselors.value.find(c => c.id === form.counselorId)
    
    // 构建提交数据
    let submitData: any = {
      counselorId: form.counselorId,
      counselorName: selectedCounselor?.name,
      date: form.date,
      timeSlot: form.timeSlot,
      type: form.type,
      mode: form.mode,
      remark: form.remark
    }
    
    // 如果是管理员端，添加学生信息
    if (isAdmin.value) {
      submitData = {
        ...submitData,
        studentId: studentInfo.studentId,
        studentName: studentInfo.name,
      }
    } else {
      // 学生端，从登录信息获取
      submitData.studentId = 'current_user_id' // 从store获取
      submitData.studentName = 'current_user_name'
    }
    
    // TODO: 调用API创建预约
    console.log('提交数据:', submitData)
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    Message.success('预约提交成功，请等待咨询师确认')
    // 根据权限跳转不同页面
    router.push(isAdmin.value ? '/booking/list' : '/booking/my')
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

// 初始化：如果有counselorId参数，自动选中
if (route.query.counselorId) {
  form.counselorId = Number(route.query.counselorId)
}

// 如果有studentId参数，获取学生信息（管理员端）
if (route.query.studentId && isAdmin.value) {
  // TODO: 根据studentId获取学生信息
  console.log('学生ID:', route.query.studentId)
}
</script>

<style scoped lang="scss">
.create-booking-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 120px);
  background: #f5f6f8;

  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    background: white;
    padding: 16px 24px;
    border-radius: 12px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    h1 {
      font-size: 24px;
      font-weight: 600;
      margin: 0;
      color: #1d2129;
    }
  }

  .booking-form-container {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 24px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  // 学生信息卡片
  .student-info-card {
    background: white;
    border-radius: 12px;
    margin-bottom: 24px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    overflow: hidden;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      background: #f7f8fa;
      border-bottom: 1px solid #e5e6eb;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #1d2129;
      }

      .header-actions {
        display: flex;
        gap: 8px;
      }
    }

    .student-info-content {
      padding: 20px;

      .info-row {
        display: flex;
        gap: 32px;
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }

        @media (max-width: 768px) {
          flex-direction: column;
          gap: 12px;
        }
      }

      .info-item {
        flex: 1;
        display: flex;
        align-items: baseline;
        
        .label {
          width: 90px;
          color: #86909c;
          font-size: 14px;
          flex-shrink: 0;
        }
        
        .value {
          color: #1d2129;
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
  }

  // 时段网格样式（采用第二个文件的设计）
  .time-slots-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    
    .time-slot-btn {
      height: 44px;
      font-size: 14px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
    
    .slot-active {
      background: #165dff;
      color: white;
      border-color: #165dff;
    }
  }

  .counselor-option {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .name {
      font-weight: 500;
    }
    
    .title {
      color: #86909c;
      font-size: 13px;
    }
  }

  .location-tip {
    margin-top: 8px;
    padding: 8px 12px;
    background: #e8f3ff;
    border-radius: 6px;
    color: #165dff;
    font-size: 13px;
  }

  .booking-info {
    background: white;
    padding: 24px;
    border-radius: 12px;
    position: sticky;
    top: 100px;
    height: fit-content;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    h3 {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 20px;
      color: #1d2129;
      padding-bottom: 12px;
      border-bottom: 2px solid #165dff;
      display: inline-block;
    }

    ul {
      padding-left: 20px;
      margin: 0;
      
      li {
        margin: 16px 0;
        color: #4e5969;
        font-size: 14px;
        line-height: 1.6;
      }
    }
  }

  :deep(.arco-form-item) {
    margin-bottom: 24px;
  }

  :deep(.arco-btn-primary) {
    background: #165dff;
    border-radius: 8px;
    font-weight: 500;
  }
}
</style>