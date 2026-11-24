<!-- @ts-nocheck -->
<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="titleBar">
            <div class="logo-wrapper">
              <img class="logo" src="../assets/logo.png" />
              <div class="logo-ring"></div>
            </div>
            <div class="title">
              <span class="title-main">心</span>
              <span class="title-text">大学生心理测试系统</span>
            </div>
          </div>
        </a-menu-item>
        
        <!-- 普通用户菜单 - 精美艺术字 -->
        <a-menu-item v-for="item in userMenus" :key="item.path" class="art-menu-item">
          <div class="menu-item-inner">
            <span class="menu-icon">{{ getMenuIcon(item.name) }}</span>
            <span class="menu-label">{{ getMenuLabel(item.name) }}</span>
            <span class="menu-glow"></span>
          </div>
        </a-menu-item>
        
        <!-- 老师工作台菜单 -->
        <a-menu-item 
          v-if="loginUserStore.loginUser?.userRole === 'COUNSELOR' || loginUserStore.loginUser?.userRole === 'TEACHER'" 
          key="/teacher/dashboard"
          class="teacher-menu-item art-menu-item"
        >
          <div class="menu-item-inner">
            <span class="menu-icon">👨‍🏫</span>
            <span class="menu-label">老师工作台</span>
            <span class="menu-glow"></span>
          </div>
        </a-menu-item>
        
        <!-- 管理员专属菜单 -->
        <a-menu-item 
          v-if="loginUserStore.loginUser?.userRole === 'ADMIN' || loginUserStore.loginUser?.userRole === 'admin'" 
          key="/add/app"
          class="admin-menu-item art-menu-item"
        >
          <div class="menu-item-inner">
            <span class="menu-icon">✨</span>
            <span class="menu-label">创建应用</span>
            <span class="menu-glow"></span>
          </div>
        </a-menu-item>

        <a-menu-item 
          v-if="loginUserStore.loginUser?.userRole === 'ADMIN' || loginUserStore.loginUser?.userRole === 'admin'" 
          key="/admin/dashboard"
          class="admin-menu-item art-menu-item"
        >
          <div class="menu-item-inner">
            <span class="menu-icon">⚙️</span>
            <span class="menu-label">管理员工作台</span>
            <span class="menu-glow"></span>
          </div>
        </a-menu-item>
      </a-menu>
    </a-col>
    
    <a-col flex="100px">
      <div v-if="loginUserStore.isLogin()">
        <a-dropdown trigger="click">
          <div class="user-profile">
            <div class="user-avatar">
              <span class="avatar-initial">{{ getUserInitial() }}</span>
              <div class="avatar-status"></div>
            </div>
            <span class="user-name">{{ loginUserStore.loginUser.userName ?? loginUserStore.loginUser.userAccount ?? "用户" }}</span>
            <icon-down class="user-dropdown-icon" />
          </div>
          <template #content>
            <div class="user-dropdown-menu">
              <a-doption @click="goToProfile">
                <template #icon>👤</template>
                个人资料
              </a-doption>
              <a-doption @click="logout">
                <template #icon>🚪</template>
                注销登录
              </a-doption>
            </div>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <button class="login-btn" @click="goToLogin">
          <span class="login-btn-text">登录</span>
          <span class="login-btn-icon">→</span>
        </button>
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
/* eslint-disable */
// @ts-nocheck
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useLoginUserStore } from "@/store/userStore";
import checkAccess from "@/access/checkAccess";
import { userLogoutUsingPost } from "@/api/userController";
import { Message } from "@arco-design/web-vue";
import { IconDown } from "@arco-design/web-vue/es/icon";

const loginUserStore = useLoginUserStore();
const router = useRouter();

const selectedKeys = ref(["/"]);

router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});

// 菜单图标映射
const menuIconMap: Record<string, string> = {
  '主页': '🏠',
  '个人中心': '👤',
  '心理测评': '📋',
  '心理资讯': '📰',
  '我的测评记录': '📊',
  '心理预约': '📅',
  '紧急联系人': '🆘'
};

// 菜单标签（去掉图标，纯文字）
const menuLabelMap: Record<string, string> = {
  '主页': '主页',
  '个人中心': '个人中心',
  '心理测评': '心理测评',
  '心理资讯': '心理资讯',
  '我的测评记录': '测评记录',
  '心理预约': '心理预约',
  '紧急联系人': '紧急联系'
};

const getMenuIcon = (name: string): string => {
  return menuIconMap[name] || '📌';
};

const getMenuLabel = (name: string): string => {
  return menuLabelMap[name] || name;
};

const getUserInitial = (): string => {
  const name = loginUserStore.loginUser?.userName || loginUserStore.loginUser?.userAccount || "U";
  return name.charAt(0).toUpperCase();
};

const goToProfile = () => {
  router.push("/user/profile");
};

const userMenus = computed(() => {
  const adminOnlyPaths = [
    '/add/app', 
    '/admin/user', 
    '/admin/app', 
    '/admin/question', 
    '/admin/scoring_result', 
    '/admin/user_answer', 
    '/app_statistic',
    '/admin/dashboard'
  ];
  
  const teacherOnlyPaths = [
    '/teacher/workbench',
    '/teacher/dashboard'
  ];
  
  const specialMenuNames = ['主页', '个人中心', '心理测评', '心理资讯', '我的测评记录', '心理预约', '紧急联系人'];
  
  const allVisibleRoutes = routes.filter((item: any) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    if (!checkAccess(loginUserStore.loginUser, item.meta?.access as string)) {
      return false;
    }
    if (!specialMenuNames.includes(item.name)) {
      return false;
    }
    return true;
  });
  
  return allVisibleRoutes.filter((route: any) => {
    if (adminOnlyPaths.includes(route.path)) {
      return false;
    }
    if (teacherOnlyPaths.includes(route.path)) {
      const userRole = loginUserStore.loginUser?.userRole;
      if (userRole !== 'COUNSELOR' && userRole !== 'TEACHER') {
        return false;
      }
    }
    return true;
  });
});

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

const goToLogin = () => {
  router.push("/user/login");
};

const logout = async () => {
  try {
    await userLogoutUsingPost();
    Message.success("已退出登录");
    loginUserStore.clearLoginUser();
    router.push("/");
  } catch (error) {
    Message.error("退出失败");
  }
};

</script>

<style scoped>
#globalHeader {
  background: linear-gradient(135deg, #ffffff 0%, #f8faf8 100%);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0 32px;
  border-bottom: 1px solid rgba(139, 187, 154, 0.25);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.03);
}

/* ========== Logo区域 ========== */
.titleBar {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 12px;
}

.logo-wrapper {
  position: relative;
}

.logo {
  height: 44px;
  width: auto;
  position: relative;
  z-index: 1;
}

.logo-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 56px;
  height: 56px;
  border: 1px solid rgba(139, 187, 154, 0.4);
  border-radius: 50%;
  animation: ringPulse 2s ease infinite;
}

@keyframes ringPulse {
  0%, 100% { opacity: 0.6; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 0.2; transform: translate(-50%, -50%) scale(1.15); }
}

.title {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.title-main {
  font-size: 32px;
  font-weight: 400;
  font-family: 'Georgia', 'Times New Roman', serif;
  background: linear-gradient(120deg, #8BBD9A, #6B9A7A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 2px 2px 4px rgba(107, 154, 122, 0.1);
}

.title-text {
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  color: #5A7C6A;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ========== 菜单容器 ========== */
:deep(.arco-menu) {
  background: transparent;
  border-bottom: none;
}

:deep(.arco-menu-item) {
  padding: 0;
  margin: 0 4px;
  background: transparent !important;
}

/* ========== 菜单项样式 - 精致卡片式 ========== */
.art-menu-item {
  position: relative;
}

.menu-item-inner {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 40px;
  transition: all 0.3s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  position: relative;
  cursor: pointer;
  background: transparent;
}

.menu-icon {
  font-size: 18px;
  transition: transform 0.2s ease;
}

.menu-label {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
  color: #5A6E62;
  transition: all 0.2s ease;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.menu-glow {
  position: absolute;
  inset: 0;
  border-radius: 40px;
  opacity: 0;
  transition: opacity 0.3s ease;
  background: linear-gradient(120deg, rgba(139, 187, 154, 0.08), rgba(107, 154, 122, 0.05));
  pointer-events: none;
}

/* 悬停效果 */
.art-menu-item:hover .menu-item-inner {
  background: linear-gradient(120deg, rgba(139, 187, 154, 0.12), rgba(107, 154, 122, 0.06));
  transform: translateY(-2px);
}

.art-menu-item:hover .menu-icon {
  transform: scale(1.08);
}

.art-menu-item:hover .menu-label {
  color: #4A7C62;
}

.art-menu-item:hover .menu-glow {
  opacity: 1;
}

/* 选中状态 */
:deep(.arco-menu-selected) .menu-item-inner {
  background: linear-gradient(135deg, rgba(139, 187, 154, 0.2), rgba(107, 154, 122, 0.12));
  box-shadow: 0 2px 8px rgba(107, 154, 122, 0.15);
}

:deep(.arco-menu-selected) .menu-label {
  color: #4A7C62;
  font-weight: 600;
}

:deep(.arco-menu-selected) .menu-icon {
  transform: scale(1.05);
}

:deep(.arco-menu-selected) .menu-glow {
  opacity: 0.5;
}

/* 移除默认选中指示器 */
:deep(.arco-menu-selected::after) {
  display: none;
}

/* ========== 老师菜单 ========== */
.teacher-menu-item .menu-label {
  color: #5B8C6F;
}

.teacher-menu-item:hover .menu-label {
  color: #3A6B4F;
}

/* ========== 管理员菜单 ========== */
.admin-menu-item .menu-label {
  color: #8B6B9A;
}

.admin-menu-item:hover .menu-label {
  color: #6B4A7A;
}

:deep(.admin-menu-item.arco-menu-selected) .menu-label {
  color: #6B4A7A;
}

/* ========== 用户资料区域 ========== */
.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 8px;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: transparent;
  border: 1px solid transparent;
}

.user-profile:hover {
  background: rgba(139, 187, 154, 0.08);
  border-color: rgba(139, 187, 154, 0.2);
}

.user-avatar {
  position: relative;
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #8BBD9A, #6B9A7A);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.avatar-initial {
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.avatar-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background: #4CAF50;
  border-radius: 50%;
  border: 2px solid white;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #5A6E62;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dropdown-icon {
  font-size: 14px;
  color: #8BBD9A;
  transition: transform 0.2s ease;
}

.user-profile:hover .user-dropdown-icon {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.user-dropdown-menu {
  min-width: 140px;
  padding: 8px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

:deep(.arco-dropdown-option) {
  padding: 10px 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #5A6E62;
  transition: all 0.2s ease;
}

:deep(.arco-dropdown-option:hover) {
  background: rgba(139, 187, 154, 0.1);
  color: #4A7C62;
}

/* ========== 登录按钮 ========== */
.login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #8BBD9A, #6B9A7A);
  border: none;
  border-radius: 40px;
  padding: 8px 22px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(107, 154, 122, 0.25);
}

.login-btn-text {
  color: white;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 1px;
}

.login-btn-icon {
  color: white;
  font-size: 16px;
  transition: transform 0.2s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(107, 154, 122, 0.35);
  background: linear-gradient(135deg, #9BCDA8, #7BAA8A);
}

.login-btn:hover .login-btn-icon {
  transform: translateX(3px);
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  #globalHeader {
    padding: 0 20px;
  }
  
  .menu-item-inner {
    padding: 6px 14px;
  }
  
  .menu-label {
    font-size: 13px;
  }
}

@media (max-width: 1000px) {
  .menu-label {
    display: none;
  }
  
  .menu-item-inner {
    padding: 8px 12px;
  }
  
  .menu-icon {
    font-size: 20px;
  }
  
  .user-name {
    display: none;
  }
  
  .title-text {
    display: none;
  }
  
  .title-main {
    font-size: 28px;
  }
}
</style>