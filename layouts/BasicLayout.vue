<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh; background: transparent">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="" target="_blank"> © 2025 大学生心理测试系统 | 关爱心理健康，从了解自己开始 </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
</script>

<style scoped>
#basicLayout {
  background: transparent;
}

#basicLayout .header {
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.95); /* 半透明白，更柔和 */
  backdrop-filter: blur(2px); /* 轻微模糊，增加质感 */
}

#basicLayout .content {
  max-width: 1200px;
  width: 100%;
  box-sizing: border-box;
  margin: 0 auto 28px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.85); /* 半透明白，透出治愈背景 */
  border-radius: 24px; /* 圆角更柔和 */
  backdrop-filter: blur(4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.04); /* 柔和阴影 */
}

.footer {
  padding: 16px;
  text-align: center;
  background: rgba(255, 255, 255, 0.6); /* 半透明 */
  backdrop-filter: blur(2px);
  color: #6b8c7c;
}

.footer a {
  color: #7eb09e;
  text-decoration: none;
}

.footer a:hover {
  color: #5e8e7a;
}
</style>
