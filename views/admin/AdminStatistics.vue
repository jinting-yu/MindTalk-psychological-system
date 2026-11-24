<template>
  <div class="admin-page">
    <h2>数据统计</h2>
    
    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 24px;">
      <a-col :span="6" v-for="stat in statsCards" :key="stat.title">
        <a-card :hoverable="true" @click="stat.onClick">
          <div style="text-align: center;">
            <div style="font-size: 32px; font-weight: 700; color: #165dff;">{{ stat.value }}</div>
            <div style="color: #666; margin-top: 8px;">{{ stat.title }}</div>
            <div style="font-size: 12px; color: #52c41a; margin-top: 4px;">{{ stat.trend }}</div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 时间筛选 -->
    <a-space style="margin-bottom: 20px;">
      <a-radio-group v-model="dateRange" type="button" @change="handleDateRangeChange">
        <a-radio value="week">近7天</a-radio>
        <a-radio value="month">近30天</a-radio>
        <a-radio value="year">近12个月</a-radio>
      </a-radio-group>
    </a-space>

    <!-- 图表区域 -->
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="访问趋势">
          <div ref="visitChartRef" style="height: 350px"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="预约趋势">
          <div ref="appointmentChartRef" style="height: 350px"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 20px;">
      <a-col :span="12">
        <a-card title="测评完成分布">
          <div ref="testChartRef" style="height: 350px"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="用户角色分布">
          <div ref="roleChartRef" style="height: 350px"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 最近活动 -->
    <a-card title="最近活动" style="margin-top: 20px;">
      <a-timeline>
        <a-timeline-item v-for="activity in recentActivities" :key="activity.id">
          <div>{{ activity.content }}</div>
          <div style="color: #999; font-size: 12px;">{{ activity.time }}</div>
        </a-timeline-item>
      </a-timeline>
    </a-card>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';

const router = useRouter();
const dateRange = ref('week');
const visitChartRef = ref(null);
const appointmentChartRef = ref(null);
const testChartRef = ref(null);
const roleChartRef = ref(null);

const statsCards = ref([
  { title: '总用户数', value: 1250, trend: '较上月 +12%', onClick: () => router.push('/admin/user') },
  { title: '总预约数', value: 456, trend: '较上月 +8%', onClick: () => router.push('/admin/appointments') },
  { title: '总测评数', value: 2340, trend: '较上月 +15%', onClick: () => router.push('/admin/tests') },
  { title: '咨询师数', value: 12, trend: '较上月 +2', onClick: () => router.push('/admin/teachers') }
]);

const recentActivities = ref([
  { id: 1, content: '新用户注册：张三', time: '2024-01-15 10:30' },
  { id: 2, content: '新增预约：李四预约张老师', time: '2024-01-15 09:15' },
  { id: 3, content: '完成测评：王五完成焦虑测评', time: '2024-01-14 16:20' },
  { id: 4, content: '发布新资讯：心理健康小贴士', time: '2024-01-14 14:00' }
]);

// 周数据
const weekData = {
  visit: [120, 200, 150, 180, 220, 280, 250],
  appointment: [15, 20, 18, 25, 30, 35, 28],
  categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
};

// 月数据
const monthData = {
  visit: [1200, 1350, 1400, 1600, 1550, 1700, 1650, 1800, 1900, 2100, 2200, 2340],
  appointment: [120, 135, 140, 155, 160, 170, 165, 180, 190, 210, 220, 230],
  categories: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
};

// 年数据
const yearData = {
  visit: [1250, 2340, 3450, 4560, 5670, 6780],
  appointment: [125, 234, 345, 456, 567, 678],
  categories: ['2020', '2021', '2022', '2023', '2024', '2025']
};

const initCharts = () => {
  const currentData = dateRange.value === 'week' ? weekData : dateRange.value === 'month' ? monthData : yearData;
  
  if (visitChartRef.value) {
    const chart = echarts.init(visitChartRef.value);
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: currentData.categories },
      yAxis: { type: 'value' },
      series: [{ data: currentData.visit, type: 'line', smooth: true, areaStyle: { opacity: 0.3 } }]
    });
  }

  if (appointmentChartRef.value) {
    const chart = echarts.init(appointmentChartRef.value);
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: currentData.categories },
      yAxis: { type: 'value' },
      series: [{ data: currentData.appointment, type: 'bar', itemStyle: { color: '#165dff' } }]
    });
  }

  if (testChartRef.value) {
    const chart = echarts.init(testChartRef.value);
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: [
          { name: '焦虑测评', value: 850 },
          { name: '抑郁测评', value: 720 },
          { name: '压力测评', value: 450 },
          { name: '性格测评', value: 320 }
        ]
      }]
    });
  }

  if (roleChartRef.value) {
    const chart = echarts.init(roleChartRef.value);
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '55%',
        data: [
          { name: '普通用户', value: 1080 },
          { name: '咨询师', value: 12 },
          { name: '管理员', value: 3 }
        ]
      }]
    });
  }
};

const handleDateRangeChange = () => {
  setTimeout(() => initCharts(), 100);
};

onMounted(() => {
  initCharts();
  window.addEventListener('resize', () => {
    [visitChartRef, appointmentChartRef, testChartRef, roleChartRef].forEach(ref => {
      if (ref.value) echarts.init(ref.value)?.resize();
    });
  });
});
</script>

<style scoped>
.admin-page { padding: 20px; background: #f5f7fa; min-height: 100vh; }
.admin-page h2 { margin-top: 0; margin-bottom: 20px; }
</style>