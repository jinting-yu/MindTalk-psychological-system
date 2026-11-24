<template>
  <div class="new-appointment-container">
    <a-card title="新建预约" :bordered="false" class="appointment-card">
      <template #extra>
        <a-button @click="handleContactStudent">联系学生</a-button>
      </template>

      <!-- 学生信息 -->
      <div class="student-info-section">
        <h3 class="section-title">学生信息</h3>
        <a-descriptions :column="2" bordered size="medium">
          <a-descriptions-item label="姓名">
            {{ studentInfo.name }}
            <a-tag :color="studentInfo.gender === '男' ? 'blue' : 'magenta'" size="small">
              {{ studentInfo.gender }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="学号">{{ studentInfo.studentId }}</a-descriptions-item>
          <a-descriptions-item label="班级">{{ studentInfo.className }}</a-descriptions-item>
          <a-descriptions-item label="学院">{{ studentInfo.college }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ studentInfo.phone }}</a-descriptions-item>
          <a-descriptions-item label="紧急联系人">
            {{ studentInfo.emergencyContact.name }}({{ studentInfo.emergencyContact.relation }}) 
            {{ studentInfo.emergencyContact.phone }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 预约信息 -->
      <div class="appointment-info-section">
        <h3 class="section-title">预约信息</h3>
        <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item field="counselorId" label="选择咨询师" required>
                <a-select v-model="formData.counselorId" placeholder="请选择咨询师" allow-clear>
                  <a-option v-for="c in counselorList" :key="c.id" :value="c.id">
                    {{ c.name }} - {{ c.title }}
                  </a-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :span="12">
              <a-form-item field="appointmentDate" label="预约日期" required>
                <a-date-picker v-model="formData.appointmentDate" placeholder="请选择日期" style="width: 100%" />
              </a-form-item>
            </a-col>

            <a-col :span="12">
              <a-form-item field="timeSlot" label="预约时段" required>
                <a-select v-model="formData.timeSlot" placeholder="请选择时段">
                  <a-option value="09:00-10:00">09:00-10:00</a-option>
                  <a-option value="10:00-11:00">10:00-11:00</a-option>
                  <a-option value="11:00-12:00">11:00-12:00</a-option>
                  <a-option value="14:00-15:00">14:00-15:00</a-option>
                  <a-option value="15:00-16:00">15:00-16:00</a-option>
                  <a-option value="16:00-17:00">16:00-17:00</a-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :span="12">
              <a-form-item field="consultType" label="咨询类型" required>
                <a-select v-model="formData.consultType" placeholder="请选择咨询类型">
                  <a-option value="individual">个体咨询</a-option>
                  <a-option value="career">职业规划</a-option>
                  <a-option value="study">学业指导</a-option>
                  <a-option value="relationship">人际关系</a-option>
                  <a-option value="stress">压力管理</a-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :span="24">
              <a-form-item field="consultMethod" label="咨询方式" required>
                <a-radio-group v-model="formData.consultMethod">
                  <a-radio value="online">线上咨询</a-radio>
                  <a-radio value="offline">线下咨询</a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>

            <a-col :span="24">
              <a-form-item field="remark" label="备注">
                <a-textarea v-model="formData.remark" placeholder="请简要描述咨询问题或特殊需求" :rows="4" show-word-limit :max-length="200" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="action-buttons">
        <a-space>
          <a-button @click="handleReset">重置</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">提交预约</a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'

// 学生信息
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

// 咨询师列表
const counselorList = ref([
  { id: 1, name: '王老师', title: '高级心理咨询师' },
  { id: 2, name: '李老师', title: '资深咨询师' },
  { id: 3, name: '张老师', title: '心理治疗师' },
])

// 表单数据
const formData = reactive({
  counselorId: undefined,
  appointmentDate: undefined,
  timeSlot: '09:00-10:00',
  consultType: undefined,
  consultMethod: 'offline',
  remark: ''
})

const formRules = {
  counselorId: [{ required: true, message: '请选择咨询师' }],
  appointmentDate: [{ required: true, message: '请选择预约日期' }],
  timeSlot: [{ required: true, message: '请选择预约时段' }],
  consultType: [{ required: true, message: '请选择咨询类型' }],
}

const formRef = ref()
const submitting = ref(false)

const handleContactStudent = () => {
  Modal.info({
    title: '联系学生',
    content: `电话：${studentInfo.phone}\n紧急联系人：${studentInfo.emergencyContact.name} ${studentInfo.emergencyContact.phone}`,
  })
}

const handleReset = () => {
  formRef.value?.resetFields()
  formData.timeSlot = '09:00-10:00'
  formData.consultMethod = 'offline'
  formData.remark = ''
}

const handleSubmit = async () => {
  const errors = await formRef.value?.validate()
  if (errors) return
  
  submitting.value = true
  // 调用API...
  await new Promise(resolve => setTimeout(resolve, 1000))
  Message.success('预约成功')
  handleReset()
  submitting.value = false
}
</script>

<style scoped lang="scss">
.new-appointment-container {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.appointment-card {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid #165dff;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e6eb;
}
</style>
