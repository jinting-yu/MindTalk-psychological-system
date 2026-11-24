<template>
  <div class="teacher-statistics">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据统计</h2>
      <p>学生心理健康数据统计分析</p>
    </div>

    <!-- 时间筛选 -->
    <div class="time-filter">
      <a-radio-group v-model="timeRange" type="button" @change="handleTimeRangeChange">
        <a-radio value="week">本周</a-radio>
        <a-radio value="month">本月</a-radio>
        <a-radio value="quarter">本季度</a-radio>
        <a-radio value="year">本年</a-radio>
      </a-radio-group>
      <a-range-picker v-model="customDate" style="width: 260px" @change="handleCustomDateChange" />
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon" style="background: #eef2ff;">
          <icon-user />
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ metrics.totalStudents }}</div>
          <div class="metric-label">服务学生总数</div>
          <div class="metric-trend">较上月 +{{ metrics.studentGrowth }}%</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon" style="background: #e8f3ed;">
          <icon-file />
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ metrics.totalTests }}</div>
          <div class="metric-label">完成测评次数</div>
          <div class="metric-trend">较上月 +{{ metrics.testGrowth }}%</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon" style="background: #fef3e8;">
          <icon-calendar />
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ metrics.totalAppointments }}</div>
          <div class="metric-label">预约咨询次数</div>
          <div class="metric-trend">较上月 +{{ metrics.appointmentGrowth }}%</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon" style="background: #fde7e7;">
          <icon-exclamation-circle />
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ metrics.highRiskCount }}</div>
          <div class="metric-label">高风险学生</div>
          <div class="metric-trend trend-down">较上月 -{{ metrics.highRiskDrop }}%</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 测评趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>测评完成趋势</h3>
          <a-select v-model="trendType" size="small" style="width: 100px">
            <a-option value="daily">按日</a-option>
            <a-option value="weekly">按周</a-option>
            <a-option value="monthly">按月</a-option>
          </a-select>
        </div>
        <div class="chart-content">
          <div class="trend-chart">
            <div v-for="(item, index) in trendData" :key="index" class="trend-bar-wrapper">
              <div class="trend-label">{{ item.label }}</div>
              <div class="trend-bar">
                <div class="bar" :style="{ height: (item.value / maxTrendValue * 100) + '%', backgroundColor: '#3b82f6' }"></div>
              </div>
              <div class="trend-value">{{ item.value }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 测评类型分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>测评类型分布</h3>
        </div>
        <div class="chart-content">
          <div class="pie-chart">
            <div v-for="type in testDistribution" :key="type.name" class="pie-item">
              <div class="pie-color" :style="{ backgroundColor: type.color }"></div>
              <div class="pie-label">{{ type.name }}</div>
              <div class="pie-value">{{ type.value }}次</div>
              <div class="pie-percent">{{ type.percent }}%</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 风险等级分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>学生风险等级分布</h3>
        </div>
        <div class="chart-content">
          <div class="risk-distribution">
            <div class="risk-item high">
              <div class="risk-label">高风险</div>
              <div class="risk-bar">
                <div class="risk-bar-fill" :style="{ width: riskDistribution.high + '%' }"></div>
              </div>
              <div class="risk-value">{{ riskDistribution.high }}%</div>
            </div>
            <div class="risk-item medium">
              <div class="risk-label">中风险</div>
              <div class="risk-bar">
                <div class="risk-bar-fill" :style="{ width: riskDistribution.medium + '%' }"></div>
              </div>
              <div class="risk-value">{{ riskDistribution.medium }}%</div>
            </div>
            <div class="risk-item low">
              <div class="risk-label">低风险</div>
              <div class="risk-bar">
                <div class="risk-bar-fill" :style="{ width: riskDistribution.low + '%' }"></div>
              </div>
              <div class="risk-value">{{ riskDistribution.low }}%</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 咨询类型排行 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>热门咨询类型</h3>
        </div>
        <div class="chart-content">
          <div class="ranking-list">
            <div v-for="(type, index) in appointmentTypes" :key="type.name" class="ranking-item">
              <div class="ranking-number" :class="{ 'top': index < 3 }">{{ index + 1 }}</div>
              <div class="ranking-name">{{ type.name }}</div>
              <div class="ranking-count">{{ type.count }}次</div>
              <div class="ranking-bar">
                <div class="ranking-bar-fill" :style="{ width: (type.count / maxAppointmentCount * 100) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 导出按钮 -->
    <div class="export-section">
      <a-button type="primary" @click="exportData">
        <icon-download /> 导出统计报告
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'

const timeRange = ref('month')
const customDate = ref([])
const trendType = ref('daily')

// 核心指标
const metrics = ref({
  totalStudents: 0,
  totalTests: 0,
  totalAppointments: 0,
  highRiskCount: 0,
  studentGrowth: 12.5,
  testGrowth: 23.8,
  appointmentGrowth: 15.2,
  highRiskDrop: 8.3
})

// 趋势数据
const trendData = ref([
  { label: '03-21', value: 12 },
  { label: '03-22', value: 18 },
  { label: '03-23', value: 15 },
  { label: '03-24', value: 22 },
  { label: '03-25', value: 28 },
  { label: '03-26', value: 25 },
  { label: '03-27', value: 30 }
])

const maxTrendValue = ref(30)

// 测评类型分布
const testDistribution = ref([
  { name: 'MBTI性格测试', value: 68, percent: 35, color: '#3b82f6' },
  { name: 'SAS焦虑自评', value: 42, percent: 22, color: '#f59e0b' },
  { name: '抑郁自评量表', value: 38, percent: 20, color: '#10b981' },
  { name: '人际关系诊断', value: 28, percent: 15, color: '#a855f7' },
  { name: '其他测评', value: 16, percent: 8, color: '#6b7280' }
])

// 风险等级分布
const riskDistribution = ref({
  high: 18,
  medium: 35,
  low: 47
})

// 咨询类型排行
const appointmentTypes = ref([
  { name: '情绪管理咨询', count: 45 },
  { name: '职业规划咨询', count: 32 },
  { name: '人际关系咨询', count: 28 },
  { name: '压力调适咨询', count: 24 },
  { name: '自我成长咨询', count: 18 }
])

const maxAppointmentCount = ref(45)

// 加载统计数据
const loadStatistics = async () => {
  try {
    // TODO: 调用API
    await new Promise(resolve => setTimeout(resolve, 500))
    
    metrics.value = {
      totalStudents: 156,
      totalTests: 428,
      totalAppointments: 189,
      highRiskCount: 28,
      studentGrowth: 12.5,
      testGrowth: 23.8,
      appointmentGrowth: 15.2,
      highRiskDrop: 8.3
    }
  } catch (error) {
    Message.error('加载失败')
  }
}

// 时间范围变化
const handleTimeRangeChange = () => {
  loadStatistics()
}

// 自定义日期变化
const handleCustomDateChange = () => {
  loadStatistics()
}

// 导出数据
const exportData = () => {
  Message.info('正在生成统计报告...')
  // TODO: 调用导出API
  setTimeout(() => {
    Message.success('导出成功')
  }, 1500)
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.teacher-statistics {
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

.time-filter {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  align-items: center;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.metric-card {
  background: #fff;
  border: 1px solid #e5e6eb;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s;
}

.metric-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.metric-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.metric-trend {
  font-size: 12px;
  color: #00b42a;
  margin-top: 4px;
}

.metric-trend.trend-down {
  color: #f53f3f;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.chart-card {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #1e293b;
}

.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 200px;
}

.trend-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.trend-label {
  font-size: 12px;
  color: #64748b;
}

.trend-bar {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 32px;
  border-radius: 4px;
  transition: height 0.3s;
  min-height: 4px;
}

.trend-value {
  font-size: 12px;
  font-weight: 500;
  color: #1e293b;
}

.pie-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pie-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.pie-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.pie-label {
  flex: 1;
  font-size: 14px;
  color: #1e293b;
}

.pie-value {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.pie-percent {
  width: 50px;
  font-size: 14px;
  color: #64748b;
}

.risk-distribution {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.risk-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.risk-label {
  width: 60px;
  font-size: 14px;
  font-weight: 500;
}

.risk-bar {
  flex: 1;
  height: 8px;
  background: #e5e6eb;
  border-radius: 4px;
  overflow: hidden;
}

.risk-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.risk-item.high .risk-bar-fill {
  background: #f53f3f;
}

.risk-item.medium .risk-bar-fill {
  background: #ff7d00;
}

.risk-item.low .risk-bar-fill {
  background: #00b42a;
}

.risk-value {
  width: 50px;
  font-size: 14px;
  font-weight: 500;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.ranking-number {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5e6eb;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
}

.ranking-number.top {
  background: #f59e0b;
  color: #fff;
}

.ranking-name {
  flex: 1;
  font-size: 14px;
  color: #1e293b;
}

.ranking-count {
  width: 60px;
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.ranking-bar {
  width: 120px;
  height: 6px;
  background: #e5e6eb;
  border-radius: 3px;
  overflow: hidden;
}

.ranking-bar-fill {
  height: 100%;
  background: #3b82f6;
  border-radius: 3px;
  transition: width 0.3s;
}

.export-section {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #e5e6eb;
}
</style>