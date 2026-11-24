<template>
  <div class="test-detail">
    <div class="page-header">
      <a-button type="outline" @click="goBack">
        <template #icon><icon-left /></template>
        返回
      </a-button>
      <h2>测评详情</h2>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <!-- 学生信息卡片 -->
      <div class="info-card student-card">
        <h3>👤 学生信息</h3>
        <div class="student-header">
          <div class="student-avatar">
            <div class="avatar-circle">{{ detail.studentName?.charAt(0) || '学' }}</div>
            <a-tag :color="studentGender === '女' ? 'pink' : 'blue'">{{ studentGender || '男' }}</a-tag>
          </div>
          <div class="student-basic">
            <div class="student-name">{{ detail.studentName }}</div>
            <div class="student-id">学号：{{ detail.studentId }}</div>
          </div>
        </div>
        <div class="info-list">
          <div class="info-item">
            <span class="label">姓名：</span>
            <span class="value">{{ detail.studentName }}</span>
          </div>
          <div class="info-item">
            <span class="label">学号：</span>
            <span class="value">{{ detail.studentId }}</span>
          </div>
          <div class="info-item">
            <span class="label">班级：</span>
            <span class="value">{{ detail.className || '心理学院1班' }}</span>
          </div>
          <div class="info-item">
            <span class="label">学院：</span>
            <span class="value">{{ studentCollege || '心理学院' }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系电话：</span>
            <span class="value">{{ studentPhone || '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="label">紧急联系人：</span>
            <span class="value">{{ emergencyContact || '未设置' }}</span>
          </div>
        </div>
        <div class="contact-actions">
          <a-button type="primary" size="small" @click="contactStudent">
            <template #icon><icon-phone /></template>
            联系学生
          </a-button>
          <a-button size="small" @click="viewStudentProfile">
            <template #icon><icon-user /></template>
            查看档案
          </a-button>
        </div>
      </div>

      <!-- 测评信息 -->
      <div class="info-card">
        <h3>📊 测评信息</h3>
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="测评名称">{{ detail.testName }}</a-descriptions-item>
          <a-descriptions-item label="测评结果">
            <a-tag :color="resultColor">{{ detail.result }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="得分">
            <div class="score-display">
              <span class="score-value">{{ detail.score }}</span>
              <span class="score-total">/ {{ detail.totalScore || 100 }}</span>
              <div class="score-progress">
                <div class="progress-bar" :style="{ width: (detail.score / (detail.totalScore || 100) * 100) + '%', backgroundColor: scoreColor }"></div>
              </div>
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="风险等级">
            <a-tag :color="riskColor">{{ riskText }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="测评建议">{{ detail.suggestion || '建议关注学生心理健康状况，必要时进行心理辅导' }}</a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 答题详情 -->
      <div class="info-card">
        <h3>📝 答题详情</h3>
        <div class="answer-list">
          <div v-for="(answer, index) in detail.answers" :key="index" class="answer-item">
            <div class="question-title">{{ index + 1 }}. {{ answer.question }}</div>
            <div class="answer-content">
              <span class="answer-label">学生答案：</span>
              <span :class="{ 'answer-warning': answer.isAbnormal }">{{ answer.answer }}</span>
            </div>
            <div class="answer-score" v-if="answer.score !== undefined">
              <span class="answer-label">得分：</span>
              <span>{{ answer.score }}分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <a-button type="primary" @click="exportReport">
          <template #icon><icon-download /></template>
          导出报告
        </a-button>
        <a-button @click="contactStudent">
          <template #icon><icon-phone /></template>
          联系学生
        </a-button>
        <a-button @click="scheduleFollowUp">
          <template #icon><icon-calendar /></template>
          安排跟进
        </a-button>
      </div>
    </a-spin>

    <!-- 安排跟进弹窗 -->
    <a-modal
      v-model:visible="showFollowUpModal"
      title="安排跟进"
      @ok="confirmFollowUp"
      @cancel="showFollowUpModal = false"
    >
      <a-form :model="followUpForm" layout="vertical">
        <a-form-item label="跟进日期" required>
          <a-date-picker v-model="followUpForm.date" style="width: 100%" placeholder="请选择日期" />
        </a-form-item>
        <a-form-item label="跟进时间" required>
          <a-time-picker v-model="followUpForm.time" style="width: 100%" placeholder="请选择时间" />
        </a-form-item>
        <a-form-item label="跟进方式">
          <a-radio-group v-model="followUpForm.type">
            <a-radio value="online">线上咨询</a-radio>
            <a-radio value="offline">线下见面</a-radio>
            <a-radio value="phone">电话沟通</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="跟进内容">
          <a-textarea v-model="followUpForm.content" placeholder="请输入跟进内容" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'

// 定义类型
interface AnswerItem {
  question: string
  answer: string
  score?: number
  isAbnormal: boolean
}

interface TestDetail {
  id: string
  studentName: string
  studentId: string
  className: string
  testName: string
  result: string
  score: number
  totalScore: number
  testTime: string
  suggestion: string
  answers: AnswerItem[]
}

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const showFollowUpModal = ref(false)

// 学生扩展信息
const studentGender = ref('男')
const studentCollege = ref('心理学院')
const studentPhone = ref('13800138000')
const emergencyContact = ref('李四(父亲) 13900139000')

const followUpForm = ref({
  date: '',
  time: '',
  type: 'online',
  content: ''
})

const detail = ref<TestDetail>({
  id: '',
  studentName: '',
  studentId: '',
  className: '',
  testName: '',
  result: '',
  score: 0,
  totalScore: 100,
  testTime: '',
  suggestion: '',
  answers: []
})

// 结果颜色
const resultColor = computed(() => {
  const result = detail.value.result
  if (result.includes('焦虑') || result.includes('抑郁')) return 'orange'
  if (result === '正常范围') return 'green'
  if (result === 'INTJ型' || result === 'INFJ型') return 'blue'
  return 'gray'
})

// 得分颜色
const scoreColor = computed(() => {
  const score = detail.value.score
  const total = detail.value.totalScore || 100
  const percent = (score / total) * 100
  if (percent >= 80) return '#00b42a'
  if (percent >= 60) return '#ff7d00'
  return '#f53f3f'
})

// 风险等级文本
const riskText = computed(() => {
  const score = detail.value.score
  const total = detail.value.totalScore || 100
  const percent = (score / total) * 100
  if (percent >= 80) return '低风险'
  if (percent >= 60) return '中风险'
  return '高风险'
})

// 风险等级颜色
const riskColor = computed(() => {
  const score = detail.value.score
  const total = detail.value.totalScore || 100
  const percent = (score / total) * 100
  if (percent >= 80) return 'green'
  if (percent >= 60) return 'orange'
  return 'red'
})

/**
 * 查看学生档案
 * 修复：使用单斜杠，不是双斜杠
 */
const viewStudentProfile = () => {
  const studentId = detail.value.studentId
  if (!studentId) {
    Message.warning('学生信息不存在')
    return
  }
  console.log('查看学生档案，学生ID:', studentId)
  // 关键修复：使用单斜杠，不是双斜杠
  router.push(`/user/archive/${studentId}`)
}

/**
 * 导出报告功能
 */
const exportReport = () => {
  const reportContent = generateReportContent()
  const blob = new Blob([reportContent], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${detail.value.studentName}_${detail.value.testName}_测评报告.html`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  Message.success('报告导出成功')
}

/**
 * 生成报告内容
 */
const generateReportContent = () => {
  const now = new Date().toLocaleString('zh-CN')
  const answersHtml = detail.value.answers.map((item, idx) => `
    <div style="margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px;">
      <h4 style="margin: 0 0 10px 0; color: #333;">${idx + 1}. ${item.question}</h4>
      <p style="margin: 5px 0; color: #666;"><strong>学生答案：</strong> ${item.answer}</p>
      ${item.score !== undefined ? `<p style="margin: 5px 0; color: #666;"><strong>得分：</strong> ${item.score}分</p>` : ''}
    </div>
  `).join('')
  
  return `<!DOCTYPE html>
  <html>
  <head>
    <meta charset="UTF-8">
    <title>心理测评报告</title>
    <style>
      body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 0; padding: 40px; background: #f0f2f5; }
      .report-container { max-width: 800px; margin: 0 auto; background: white; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); overflow: hidden; }
      .report-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
      .report-header h1 { margin: 0; font-size: 24px; }
      .report-header p { margin: 10px 0 0; opacity: 0.9; }
      .report-body { padding: 30px; }
      .info-section { margin-bottom: 30px; }
      .info-section h3 { color: #333; border-left: 4px solid #667eea; padding-left: 12px; margin: 0 0 15px 0; }
      .info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; background: #f8f9fa; padding: 20px; border-radius: 12px; }
      .info-item { display: flex; }
      .info-label { font-weight: bold; width: 80px; color: #666; }
      .info-value { color: #333; }
      .result-card { background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%); padding: 20px; border-radius: 12px; margin-bottom: 30px; text-align: center; }
      .result-score { font-size: 48px; font-weight: bold; color: #667eea; }
      .result-label { color: #666; margin-top: 10px; }
      .suggestion-box { background: #e8f4e8; padding: 20px; border-radius: 12px; margin: 20px 0; }
      .footer { text-align: center; padding: 20px; background: #f8f9fa; color: #999; font-size: 12px; }
    </style>
  </head>
  <body>
    <div class="report-container">
      <div class="report-header">
        <h1>心理测评报告</h1>
        <p>生成时间：${now}</p>
      </div>
      <div class="report-body">
        <div class="info-section">
          <h3>基本信息</h3>
          <div class="info-grid">
            <div class="info-item"><span class="info-label">学生姓名：</span><span class="info-value">${detail.value.studentName}</span></div>
            <div class="info-item"><span class="info-label">学号：</span><span class="info-value">${detail.value.studentId}</span></div>
            <div class="info-item"><span class="info-label">班级：</span><span class="info-value">${detail.value.className}</span></div>
            <div class="info-item"><span class="info-label">测评时间：</span><span class="info-value">${detail.value.testTime}</span></div>
          </div>
        </div>
        
        <div class="info-section">
          <h3>测评结果</h3>
          <div class="result-card">
            <div class="result-score">${detail.value.score} / ${detail.value.totalScore}</div>
            <div class="result-label">${detail.value.testName}</div>
            <div style="margin-top: 10px;"><strong>${detail.value.result}</strong></div>
            <div style="margin-top: 10px; color: ${riskColor.value === 'green' ? '#52c41a' : (riskColor.value === 'orange' ? '#ff7d00' : '#f53f3f')}">${riskText.value}</div>
          </div>
        </div>
        
        <div class="info-section">
          <h3>答题详情</h3>
          ${answersHtml}
        </div>
        
        <div class="info-section">
          <h3>测评建议</h3>
          <div class="suggestion-box">
            <p style="margin: 0; line-height: 1.6;">${detail.value.suggestion}</p>
          </div>
        </div>
      </div>
      <div class="footer">
        <p>本报告由大学生心理测试系统生成，仅供参考</p>
        <p>如有疑问，请联系心理老师</p>
      </div>
    </div>
  </body>
  </html>`
}

/**
 * 联系学生
 */
const contactStudent = () => {
  Modal.confirm({
    title: '联系学生',
    content: `
      <div style="text-align: left; padding: 10px 0;">
        <p><strong>学生姓名：</strong> ${detail.value.studentName}</p>
        <p><strong>学号：</strong> ${detail.value.studentId}</p>
        <p><strong>班级：</strong> ${detail.value.className}</p>
        <p><strong>联系电话：</strong> ${studentPhone.value}</p>
        <p style="margin-top: 15px;">请选择联系方式：</p>
        <div style="display: flex; gap: 15px; margin-top: 10px;">
          <a href="tel:${studentPhone.value}" style="text-decoration: none;">
            <button style="padding: 8px 16px; background: #165dff; color: white; border: none; border-radius: 8px; cursor: pointer;">📞 电话联系</button>
          </a>
          <button id="sendMessageBtn" style="padding: 8px 16px; background: #52c41a; color: white; border: none; border-radius: 8px; cursor: pointer;">💬 发送消息</button>
        </div>
      </div>
    `,
    hideCancel: true,
    onOk: () => {},
    onOpen: () => {
      setTimeout(() => {
        const sendBtn = document.getElementById('sendMessageBtn')
        if (sendBtn) {
          sendBtn.onclick = () => {
            sendMessage()
          }
        }
      }, 100)
    }
  })
}

/**
 * 发送消息
 */
const sendMessage = () => {
  Modal.confirm({
    title: '发送消息',
    content: `
      <div style="padding: 10px 0;">
        <p><strong>收件人：</strong> ${detail.value.studentName}</p>
        <p><strong>消息内容：</strong></p>
        <textarea id="messageContent" rows="4" style="width: 100%; padding: 8px; border: 1px solid #e5e6eb; border-radius: 8px; margin-top: 8px;">${generateMessageContent()}</textarea>
      </div>
    `,
    onOk: async () => {
      const textarea = document.getElementById('messageContent') as HTMLTextAreaElement
      Message.success(`消息已发送给 ${detail.value.studentName}`)
    }
  })
}

/**
 * 生成消息内容
 */
const generateMessageContent = () => {
  return `【心理测评反馈】\n同学你好，你的${detail.value.testName}测评结果显示${detail.value.result}。\n${detail.value.suggestion}\n如需帮助，请随时联系心理老师。`
}

/**
 * 安排跟进
 */
const scheduleFollowUp = () => {
  showFollowUpModal.value = true
}

/**
 * 确认安排跟进
 */
const confirmFollowUp = async () => {
  if (!followUpForm.value.date || !followUpForm.value.time) {
    Message.warning('请选择跟进时间和方式')
    return
  }
  
  Message.success(`已安排${followUpForm.value.date} ${followUpForm.value.time}进行跟进`)
  showFollowUpModal.value = false
  
  followUpForm.value = {
    date: '',
    time: '',
    type: 'online',
    content: ''
  }
}

/**
 * 获取测评类型对应的题目
 */
const getQuestionsByTestType = (testName: string) => {
  if (testName.includes('MBTI')) {
    return [
      { question: '你更喜欢独自工作还是团队合作？', options: ['独自工作', '团队合作', '两者都可'] },
      { question: '你做决定时更依赖逻辑还是情感？', options: ['逻辑', '情感', '两者兼顾'] },
      { question: '你更喜欢按计划行事还是随性而为？', options: ['按计划', '随性而为', '灵活调整'] },
      { question: '你更关注未来可能性还是眼前现实？', options: ['未来可能性', '眼前现实', '平衡'] },
      { question: '你更容易被事实说服还是被理念打动？', options: ['事实', '理念', '都需要'] }
    ]
  } else if (testName.includes('SAS') || testName.includes('焦虑')) {
    return [
      { question: '我感到比平时更容易紧张和焦虑', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] },
      { question: '我无缘无故感到害怕', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] },
      { question: '我容易心烦意乱或感到恐慌', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] },
      { question: '我觉得自己可能要发疯', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] },
      { question: '我感到事事都很顺利，不会有倒霉的事情发生', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] }
    ]
  } else if (testName.includes('抑郁') || testName.includes('SDS')) {
    return [
      { question: '我感到情绪沮丧、郁闷', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] },
      { question: '我感到早晨心情最好', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] },
      { question: '我要哭或想哭', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] },
      { question: '我晚上睡眠不好', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] },
      { question: '我吃饭像平时一样多', options: ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间'] }
    ]
  } else {
    return [
      { question: '题目1：请根据实际情况选择', options: ['非常不符合', '不符合', '一般', '符合', '非常符合'] },
      { question: '题目2：请根据实际情况选择', options: ['非常不符合', '不符合', '一般', '符合', '非常符合'] },
      { question: '题目3：请根据实际情况选择', options: ['非常不符合', '不符合', '一般', '符合', '非常符合'] },
      { question: '题目4：请根据实际情况选择', options: ['非常不符合', '不符合', '一般', '符合', '非常符合'] },
      { question: '题目5：请根据实际情况选择', options: ['非常不符合', '不符合', '一般', '符合', '非常符合'] }
    ]
  }
}

/**
 * 模拟获取学生的答题答案
 */
const getStudentAnswers = (testName: string, score: number, result: string) => {
  const questions = getQuestionsByTestType(testName)
  
  if (testName.includes('MBTI')) {
    const mbtiAnswers: Record<string, string[]> = {
      'INTJ型': ['独自工作', '逻辑', '按计划', '未来可能性', '事实'],
      'INFJ型': ['两者都可', '情感', '灵活调整', '未来可能性', '理念'],
      'ESTJ型': ['团队合作', '逻辑', '按计划', '眼前现实', '事实'],
      'ENFP型': ['团队合作', '情感', '随性而为', '未来可能性', '理念']
    }
    const answers = mbtiAnswers[result] || mbtiAnswers['INTJ型']
    return questions.map((q, idx) => ({
      question: q.question,
      answer: answers[idx] || q.options[0],
      isAbnormal: false
    }))
  } else if (testName.includes('焦虑') || testName.includes('抑郁')) {
    return questions.map((q, idx) => {
      let answerIndex = 0
      if (score >= 80) answerIndex = 0
      else if (score >= 60) answerIndex = 1
      else if (score >= 40) answerIndex = 2
      else answerIndex = 3
      
      const reverseQuestions = ['我感到事事都很顺利', '我感到早晨心情最好', '我吃饭像平时一样多']
      const isReverse = reverseQuestions.some(r => q.question.includes(r))
      if (isReverse) {
        answerIndex = 3 - answerIndex
      }
      
      return {
        question: q.question,
        answer: q.options[answerIndex],
        score: (4 - answerIndex) * 5,
        isAbnormal: answerIndex >= 2
      }
    })
  } else {
    return questions.map((q, idx) => ({
      question: q.question,
      answer: q.options[Math.floor(Math.random() * q.options.length)],
      isAbnormal: false
    }))
  }
}

/**
 * 获取建议
 */
const getSuggestion = (testName: string, result: string, score: number) => {
  if (testName.includes('MBTI')) {
    return `${result}型人格具有独立思考、善于规划的特点，适合从事科研、管理等职业。建议继续保持理性思考，同时多关注他人感受。`
  } else if (testName.includes('焦虑')) {
    if (score >= 60) return '您的焦虑水平在正常范围内，建议保持良好作息，适当运动。'
    return '您的焦虑水平偏高，建议寻求专业心理咨询帮助，学习放松技巧。'
  } else if (testName.includes('抑郁')) {
    if (score >= 70) return '您的情绪状态良好，继续保持积极心态。'
    return '您可能存在抑郁倾向，建议及时与心理老师沟通，获得专业支持。'
  }
  return '建议关注学生心理健康状况，必要时进行心理辅导。'
}

/**
 * 加载详情
 */
const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const mockData: Record<string, any> = {
      '1': { 
        name: '张三', 
        testName: 'MBTI职业性格测试', 
        result: 'INTJ型', 
        score: 85, 
        totalScore: 100,
        gender: '男',
        college: '心理学院',
        phone: '13800138000',
        emergencyContact: '李四(父亲) 13900139000'
      },
      '2': { 
        name: '李四', 
        testName: 'SAS焦虑自评量表', 
        result: '轻度焦虑', 
        score: 52, 
        totalScore: 80,
        gender: '女',
        college: '教育学院',
        phone: '13900139000',
        emergencyContact: '王五(母亲) 13700137000'
      },
      '3': { 
        name: '王五', 
        testName: '抑郁自评量表', 
        result: '中度抑郁倾向', 
        score: 68, 
        totalScore: 100,
        gender: '男',
        college: '管理学院',
        phone: '15900159000',
        emergencyContact: '赵六(姐姐) 15800158000'
      },
      '4': { 
        name: '赵六', 
        testName: '人际关系综合诊断', 
        result: '需要改善', 
        score: 45, 
        totalScore: 100,
        gender: '女',
        college: '文学院',
        phone: '18700187000',
        emergencyContact: '钱七(朋友) 18600186000'
      }
    }
    
    const data = mockData[id as string] || mockData['1']
    const answers = getStudentAnswers(data.testName, data.score, data.result)
    
    detail.value = {
      id: id as string,
      studentName: data.name,
      studentId: `2024${String(id).padStart(3, '0')}`,
      className: '心理学院1班',
      testName: data.testName,
      result: data.result,
      score: data.score,
      totalScore: data.totalScore,
      testTime: '2025-03-20 14:30',
      suggestion: getSuggestion(data.testName, data.result, data.score),
      answers: answers
    }
    
    // 设置学生扩展信息
    studentGender.value = data.gender || '男'
    studentCollege.value = data.college || '心理学院'
    studentPhone.value = data.phone || '13800138000'
    emergencyContact.value = data.emergencyContact || '李四(父亲) 13900139000'
    
  } catch (error) {
    console.error('加载详情失败:', error)
    Message.error('加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.test-detail {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.info-card {
  margin-bottom: 24px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.info-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

/* 学生卡片样式 */
.student-card .student-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e6eb;
}

.student-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-circle {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.student-basic {
  flex: 1;
}

.student-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.student-id {
  font-size: 13px;
  color: #86909c;
}

.info-list {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item .label {
  width: 85px;
  color: #86909c;
  font-size: 13px;
}

.info-item .value {
  flex: 1;
  color: #1e293b;
  font-size: 13px;
  font-weight: 500;
}

.contact-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.score-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.score-total {
  color: #94a3b8;
}

.score-progress {
  flex: 1;
  min-width: 200px;
  height: 8px;
  background: #e5e6eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.answer-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.answer-item {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.question-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: #1e293b;
  font-size: 14px;
}

.answer-content {
  color: #64748b;
  margin-bottom: 8px;
}

.answer-label {
  font-weight: 500;
  margin-right: 8px;
}

.answer-warning {
  color: #f53f3f;
  font-weight: 500;
}

.answer-score {
  color: #667eea;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e6eb;
}
</style>