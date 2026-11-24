<template>
  <div class="teacher-layout">
    <div class="teacher-sidebar">
      <div class="sidebar-header">
        <img class="logo" src="@/assets/logo.png" alt="logo" />
        <h3>心理老师工作台</h3>
      </div>
      <a-menu :selected-keys="selectedKeys" @menu-item-click="handleMenuClick">
        <a-menu-item key="/teacher/dashboard">
          <template #icon><icon-dashboard /></template>
          工作台首页
        </a-menu-item>
        <a-menu-item key="/teacher/appointments">
          <template #icon><icon-calendar /></template>
          我的预约
          <a-badge :count="pendingCount" :offset="[10, 0]" v-if="pendingCount > 0" />
        </a-menu-item>
        <a-menu-item key="/user/archive/s">
          <template #icon><icon-user-group /></template>
          学生管理
        </a-menu-item>
        <a-menu-item key="/teacher/test-records">
          <template #icon><icon-file /></template>
          测评记录
        </a-menu-item>
        <a-menu-item key="/teacher/statistics">
          <template #icon><icon-bar-chart /></template>
          数据统计
        </a-menu-item>
      </a-menu>
    </div>
    
    <div class="teacher-content">
      <div class="content-header">
        <div class="header-title">
          <h2>{{ currentTitle }}</h2>
        </div>
        <div class="header-user">
          <a-dropdown>
            <a-button>
              {{ teacherInfo.name }}
              <icon-down />
            </a-button>
            <template #content>
              <a-doption @click="goToProfile">个人资料</a-doption>
              <a-doption @click="logout">退出登录</a-doption>
            </template>
          </a-dropdown>
        </div>
      </div>
      <div class="content-body">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useLoginUserStore } from '@/store/userStore'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const selectedKeys = ref(['/teacher/dashboard'])
const currentTitle = ref('工作台首页')
const pendingCount = ref(0)

const teacherInfo = computed(() => loginUserStore.loginUser)

const handleMenuClick = (key: string) => {
  selectedKeys.value = [key]
  router.push(key)
  
  // 更新标题
  const titleMap: Record<string, string> = {
    '/teacher/dashboard': '工作台首页',
    '/teacher/appointments': '我的预约',
    '/user/archive/s': '学生管理',
    '/teacher/test-records': '测评记录',
    '/teacher/statistics': '数据统计'
  }
  currentTitle.value = titleMap[key] || '工作台'
}

// 获取待处理预约数量
const fetchPendingCount = async () => {
  // TODO: 调用API获取待确认预约数量
  pendingCount.value = 3
}

const goToProfile = () => {
  router.push('/teacher/profile')
}

const logout = () => {
  loginUserStore.clearLoginUser()
  router.push('/user/login')
  Message.success('已退出登录')
}

onMounted(() => {
  fetchPendingCount()
  // 根据当前路由设置选中的菜单
  const currentPath = route.path
  if (currentPath.startsWith('/teacher')) {
    selectedKeys.value = [currentPath]
  }
})
</script>

<style scoped>
.teacher-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.teacher-sidebar {
  width: 260px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 24px 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 20px;
}

.sidebar-header .logo {
  height: 48px;
  margin-bottom: 12px;
}

.sidebar-header h3 {
  color: #fff;
  font-size: 16px;
  margin: 0;
}

:deep(.arco-menu) {
  background: transparent;
  border-right: none;
}

:deep(.arco-menu-item) {
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 12px;
  border-radius: 8px;
}

:deep(.arco-menu-item:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

:deep(.arco-menu-selected) {
  color: #fff;
  background: #3b82f6;
}

:deep(.arco-menu-selected:hover) {
  background: #3b82f6;
}

.teacher-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f7fa;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.content-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>