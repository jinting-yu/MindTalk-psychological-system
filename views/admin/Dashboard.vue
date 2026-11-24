<template>
  <div class="admin-dashboard">
    <h2>数据看板</h2>
    <p>欢迎回来，{{ loginUserStore.loginUser?.userName }}！</p>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" @click="goToUsers">
        <div class="stat-value">{{ stats.totalUsers }}</div>
        <div class="stat-title">总用户数</div>
        <div class="stat-trend">较上月 +12%</div>
      </div>
      <div class="stat-card" @click="goToAppointments">
        <div class="stat-value">{{ stats.totalAppointments }}</div>
        <div class="stat-title">总预约数</div>
        <div class="stat-trend">较上月 +8%</div>
      </div>
      <div class="stat-card" @click="goToTests">
        <div class="stat-value">{{ stats.totalTests }}</div>
        <div class="stat-title">总测评数</div>
        <div class="stat-trend">较上月 +15%</div>
      </div>
      <div class="stat-card" @click="goToTeachers">
        <div class="stat-value">{{ stats.totalTeachers }}</div>
        <div class="stat-title">咨询师数</div>
        <div class="stat-trend">较上月 +2</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3>近7天访问量</h3>
        <div ref="visitChartRef" style="height: 300px"></div>
      </div>
      <div class="chart-card">
        <h3>预约趋势</h3>
        <div ref="appointmentChartRef" style="height: 300px"></div>
      </div>
    </div>

    <!-- 最近动态 -->
    <div class="recent-section">
      <div class="recent-card">
        <h3>最新注册用户</h3>
        <a-table :columns="userColumns" :data="recentUsers" :pagination="false" size="small" />
      </div>
      <div class="recent-card">
        <h3>最新预约</h3>
        <a-table :columns="appointmentColumns" :data="recentAppointments" :pagination="false" size="small" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useLoginUserStore } from '@/store/userStore';
import * as echarts from 'echarts';

const router = useRouter();
const loginUserStore = useLoginUserStore();

// 统计数据
const stats = ref({
  totalUsers: 1250,
  totalAppointments: 456,
  totalTests: 2340,
  totalTeachers: 12
});

// 最近用户数据
const recentUsers = ref([
  { id: 1, userName: '张三', userAccount: 'zhangsan', createTime: '2024-01-15' },
  { id: 2, userName: '李四', userAccount: 'lisi', createTime: '2024-01-14' }
]);

// 最近预约数据
const recentAppointments = ref([
  { id: 1, userName: '王五', teacherName: '张老师', bookingTime: '2024-01-15 14:00', status: '待确认' },
  { id: 2, userName: '赵六', teacherName: '李老师', bookingTime: '2024-01-15 15:00', status: '已确认' }
]);

// 表格列定义
const userColumns = [
  { title: '用户名', dataIndex: 'userName' },
  { title: '账号', dataIndex: 'userAccount' },
  { title: '注册时间', dataIndex: 'createTime' }
];

const appointmentColumns = [
  { title: '用户', dataIndex: 'userName' },
  { title: '咨询师', dataIndex: 'teacherName' },
  { title: '预约时间', dataIndex: 'bookingTime' },
  { title: '状态', dataIndex: 'status' }
];

// 图表引用
const visitChartRef = ref<HTMLElement>();
const appointmentChartRef = ref<HTMLElement>();

// 初始化图表
const initCharts = () => {
  if (visitChartRef.value) {
    const visitChart = echarts.init(visitChartRef.value);
    visitChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{ data: [120, 200, 150, 180, 220, 280, 250], type: 'line', smooth: true }]
    });
  }

  if (appointmentChartRef.value) {
    const appointmentChart = echarts.init(appointmentChartRef.value);
    appointmentChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{ data: [15, 20, 18, 25, 30, 35, 28], type: 'bar' }]
    });
  }
};

// 跳转函数
const goToUsers = () => router.push('/admin/user');
const goToAppointments = () => router.push('/admin/appointments');
const goToTests = () => router.push('/admin/tests');
const goToTeachers = () => router.push('/admin/teachers');

onMounted(() => {
  initCharts();
});
</script>

<style scoped>
.admin-dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #3b82f6;
}

.stat-title {
  font-size: 14px;
  color: #64748b;
  margin-top: 8px;
}

.stat-trend {
  font-size: 12px;
  color: #10b981;
  margin-top: 8px;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.chart-card h3 {
  margin-bottom: 16px;
  font-size: 16px;
  color: #1f2937;
}

.recent-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.recent-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.recent-card h3 {
  margin-bottom: 16px;
  font-size: 16px;
  color: #1f2937;
}
</style>
