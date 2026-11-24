<template>
  <div class="teacher-test-records">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>学生测评记录</h2>
      <p>查看所有学生的心理测评历史数据</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <a-input-search 
        v-model="keyword" 
        placeholder="搜索学生姓名" 
        style="width: 260px"
        @search="handleSearch"
      />
      <a-select v-model="testType" placeholder="选择测评类型" style="width: 180px" allow-clear>
        <a-option value="MBTI">MBTI性格测试</a-option>
        <a-option value="SAS">SAS焦虑自评量表</a-option>
        <a-option value="SDS">抑郁自评量表</a-option>
        <a-option value="人际关系">人际关系综合诊断</a-option>
      </a-select>
      <a-range-picker 
        v-model="dateRange" 
        placeholder="选择日期范围"
        style="width: 260px"
      />
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="resetFilters">重置</a-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalTests }}</div>
        <div class="stat-label">总测评次数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalStudents }}</div>
        <div class="stat-label">参与学生</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.avgScore }}</div>
        <div class="stat-label">平均得分</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.highRiskCount }}</div>
        <div class="stat-label">高风险学生</div>
      </div>
    </div>

    <!-- 测评记录表格 -->
    <a-table 
      :columns="columns" 
      :data="testRecords" 
      :loading="loading"
      :pagination="pagination"
      @page-change="handlePageChange"
      @page-size-change="handlePageSizeChange"
    >
      <template #result="{ record }">
        <a-tag :color="getResultColor(record.result)">
          {{ record.result }}
        </a-tag>
      </template>
      
      <template #score="{ record }">
        <div class="score-bar">
          <div class="score-value">{{ record.score }}</div>
          <div class="score-progress">
            <div class="progress-bar" :style="{ width: (record.score / 100 * 100) + '%', backgroundColor: getScoreColor(record.score) }"></div>
          </div>
        </div>
      </template>
      
      <template #riskLevel="{ record }">
        <a-tag :color="getRiskColor(record.riskLevel)">
          {{ getRiskText(record.riskLevel) }}
        </a-tag>
      </template>
      
      <template #action="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="viewDetail(record.id)">
            查看详情
          </a-button>
          <a-button type="text" size="small" @click="viewStudentArchive(record.studentId)">
            📋 学生档案
          </a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const keyword = ref('')
const testType = ref('')
const dateRange = ref<any[]>([])
const testRecords = ref<any[]>([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const stats = ref({
  totalTests: 0,
  totalStudents: 0,
  avgScore: 0,
  highRiskCount: 0
})

// 表格列定义（操作列宽度调整为200px以容纳两个按钮）
const columns = [
  { title: '学生姓名', dataIndex: 'studentName', width: 100 },
  { title: '学号', dataIndex: 'studentId', width: 120 },
  { title: '测评名称', dataIndex: 'testName', width: 150 },
  { title: '测评结果', slotName: 'result', width: 100 },
  { title: '得分', slotName: 'score', width: 150 },
  { title: '风险等级', slotName: 'riskLevel', width: 100 },
  { title: '测评时间', dataIndex: 'testTime', width: 160 },
  { title: '操作', slotName: 'action', width: 200, fixed: 'right' }
]

/**
 * 获取结果标签颜色
 */
const getResultColor = (result: string) => {
  if (result.includes('焦虑') || result.includes('抑郁')) return 'orange'
  if (result === '正常范围') return 'green'
  if (result === 'INTJ型' || result === 'INFJ型') return 'blue'
  return 'gray'
}

/**
 * 获取得分进度条颜色
 */
const getScoreColor = (score: number) => {
  if (score >= 80) return '#00b42a'
  if (score >= 60) return '#ff7d00'
  return '#f53f3f'
}

/**
 * 获取风险等级文本
 */
const getRiskText = (level: string) => {
  const map: Record<string, string> = {
    high: '高风险',
    medium: '中风险',
    low: '低风险'
  }
  return map[level] || level
}

/**
 * 获取风险等级标签颜色
 */
const getRiskColor = (level: string) => {
  const map: Record<string, string> = {
    high: 'red',
    medium: 'orange',
    low: 'green'
  }
  return map[level] || 'gray'
}

/**
 * 查看学生档案
 * 根据当前路由判断跳转到教师端还是用户端的学生档案页面
 */
const viewStudentArchive = (studentId: string | number) => {
  if (!studentId) {
    Message.warning('学生信息不存在')
    return
  }
  console.log('查看学生档案，学生ID:', studentId)
  
  // 根据当前路径判断跳转目标
  const currentPath = route.path
  if (currentPath.includes('/teacher')) {
    // 教师端跳转到教师工作台下的学生档案
    router.push(`/teacher/student-archive/${studentId}`)
  } else {
    // 其他情况跳转到用户端档案页面
    router.push(`/user/archive/${studentId}`)
  }
}

/**
 * 查看测评详情
 */
const viewDetail = (id: number) => {
  router.push(`/teacher/test-detail/${id}`)
}

/**
 * 搜索
 */
const handleSearch = () => {
  console.log('搜索关键词:', keyword.value)
  console.log('测评类型:', testType.value)
  console.log('日期范围:', dateRange.value)
  pagination.value.current = 1
  loadTestRecords()
}

/**
 * 重置筛选
 */
const resetFilters = () => {
  keyword.value = ''
  testType.value = ''
  dateRange.value = []
  pagination.value.current = 1
  loadTestRecords()
}

/**
 * 分页变化
 */
const handlePageChange = (page: number) => {
  pagination.value.current = page
  loadTestRecords()
}

/**
 * 每页条数变化
 */
const handlePageSizeChange = (pageSize: number) => {
  pagination.value.pageSize = pageSize
  pagination.value.current = 1
  loadTestRecords()
}

/**
 * 加载测评记录
 */
const loadTestRecords = async () => {
  loading.value = true
  try {
    // 获取URL中的学生ID参数（如果从学生管理页面跳转过来）
    const studentIdParam = route.query.studentId
    console.log('URL参数 studentId:', studentIdParam)
    
    // TODO: 调用API获取真实数据
    // const res = await getTestRecordsApi({
    //   keyword: keyword.value,
    //   testType: testType.value,
    //   dateRange: dateRange.value,
    //   pageNum: pagination.value.current,
    //   pageSize: pagination.value.pageSize,
    //   studentId: studentIdParam
    // })
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    testRecords.value = [
      {
        id: 1,
        studentId: 101,
        studentName: '张三',
        testName: 'MBTI职业性格测试',
        result: 'INTJ型',
        score: 85,
        riskLevel: 'low',
        testTime: '2025-03-20 14:30'
      },
      {
        id: 2,
        studentId: 102,
        studentName: '李四',
        testName: 'SAS焦虑自评量表',
        result: '轻度焦虑',
        score: 52,
        riskLevel: 'medium',
        testTime: '2025-03-22 10:15'
      },
      {
        id: 3,
        studentId: 103,
        studentName: '王五',
        testName: '抑郁自评量表',
        result: '中度抑郁倾向',
        score: 68,
        riskLevel: 'high',
        testTime: '2025-03-25 09:00'
      },
      {
        id: 4,
        studentId: 104,
        studentName: '赵六',
        testName: '人际关系综合诊断',
        result: '需要改善',
        score: 45,
        riskLevel: 'medium',
        testTime: '2025-03-24 16:20'
      }
    ]
    pagination.value.total = 36
    
    stats.value = {
      totalTests: 156,
      totalStudents: 45,
      avgScore: 72,
      highRiskCount: 8
    }
  } catch (error) {
    console.error('加载失败:', error)
    Message.error('加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTestRecords()
})
</script>

<style scoped>
.teacher-test-records {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1e293b;
}

.page-header p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  color: #fff;
}

.stat-card .stat-value {
  font-size: 32px;
  font-weight: 700;
}

.stat-card .stat-label {
  font-size: 14px;
  margin-top: 8px;
  opacity: 0.9;
}

.score-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-value {
  min-width: 40px;
  font-weight: 500;
}

.score-progress {
  flex: 1;
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
</style>