<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h3>管理员工作台</h3>
      </div>
      <a-menu
        :selected-keys="selectedKeys"
        @menu-item-click="handleMenuClick"
        :style="{ width: '100%' }"
      >
        <a-menu-item key="/admin/dashboard">数据看板</a-menu-item>
        <a-menu-item key="/admin/user">用户管理</a-menu-item>
        <a-menu-item key="/admin/appointments">预约管理</a-menu-item>
        <a-menu-item key="/admin/tests">测评管理</a-menu-item>
        <a-menu-item key="/admin/teachers">咨询师管理</a-menu-item>
        <a-menu-item key="/admin/news">资讯管理</a-menu-item>
        <a-menu-item key="/admin/statistics">数据统计</a-menu-item>
      </a-menu>
    </div>
    <div class="admin-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const selectedKeys = ref([route.path]);

const handleMenuClick = (key: string) => {
  router.push(key);
};

watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath];
}, { immediate: true });
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: calc(100vh - 120px);
  background: #f5f7fa;
}

.admin-sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

:deep(.arco-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 4px 8px;
  border-radius: 8px;
}

:deep(.arco-menu-item:hover) {
  background-color: rgba(59, 130, 246, 0.08);
}

:deep(.arco-menu-selected) {
  background-color: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}
</style>