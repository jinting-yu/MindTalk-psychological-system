<!-- src/views/user/archive/Archive.vue -->
<template>
  <div class="student-archive">
    <!-- 面包屑导航（采用第二个文件的设计） -->
    <div class="breadcrumb-bar">
      <a-button class="back-btn" @click="goBack" type="text">
        <span class="back-icon">←</span> 返回
      </a-button>
      <span class="page-title">学生档案</span>
    </div>

    <a-card :bordered="false" class="archive-card" :body-style="{ padding: 0 }">
      <div v-if="loading" class="loading">
        <a-spin />
      </div>

      <div v-else-if="studentInfo" class="student-info">
        <!-- 头部区域：头像 + 姓名 + 学号 + 性别（采用第二个文件的布局） -->
        <div class="info-header">
          <div class="avatar-wrapper">
            <a-avatar :size="72" :style="{ backgroundColor: '#3b82f6', fontSize: '28px' }">
              {{ studentInfo.name?.charAt(0) || '?' }}
            </a-avatar>
          </div>
          <div class="header-details">
            <h2>{{ studentInfo.name || '--' }}</h2>
            <p>学号：{{ studentInfo.studentId || '--' }}</p>
          </div>
          <div class="gender-tag">
            {{ studentInfo.gender || '--' }}
          </div>
        </div>

        <!-- 信息网格（采用第二个文件的两列布局） -->
        <div class="info-grid">
          <div class="info-item">
            <label>姓名</label>
            <span>{{ studentInfo.name || '-' }}</span>
          </div>
          <div class="info-item">
            <label>学号</label>
            <span>{{ studentInfo.studentId || '-' }}</span>
          </div>
          <div class="info-item">
            <label>班级</label>
            <span>{{ studentInfo.className || '-' }}</span>
          </div>
          <div class="info-item">
            <label>学院</label>
            <span>{{ studentInfo.college || '-' }}</span>
          </div>
          <div class="info-item">
            <label>联系电话</label>
            <span>{{ studentInfo.phone || '-' }}</span>
          </div>
          <div class="info-item">
            <label>紧急联系人</label>
            <span>{{ studentInfo.emergencyContact || '-' }}</span>
          </div>
          <div class="info-item">
            <label>紧急联系电话</label>
            <span>{{ studentInfo.emergencyPhone || studentInfo.emergencyContactPhone || '-' }}</span>
          </div>
        </div>

        <!-- 底部按钮（结合两个文件的按钮功能） -->
        <div class="card-footer">
          <a-space :size="16">
            <a-button type="primary" class="contact-btn" @click="contactStudent">
              📞 联系学生
            </a-button>
            <a-button class="archive-btn" @click="viewArchive">
              📋 查看档案
            </a-button>
          </a-space>
        </div>
      </div>

      <div v-else class="empty">
        <a-empty description="暂无学生信息" />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const studentInfo = ref<any>(null)

const getToken = () => localStorage.getItem('token')

const fetchStudentInfo = async () => {
  // 获取学生ID（支持多种参数方式）
  const studentId = route.params.id || route.query.id
  
  if (!studentId) {
    Message.error('学生ID不存在')
    return
  }
  
  loading.value = true
  try {
    // 修复API路径（使用正确的单斜杠）
    const res = await axios.get(`/api/user/archive/${studentId}`, {
      headers: { Authorization: `Bearer ${getToken()}` }
    })
    
    // 兼容多种返回格式
    if (res.data.code === 200 || res.data.code === 20000) {
      studentInfo.value = res.data.data
    } else {
      Message.error(res.data.msg || res.data.message || '获取学生信息失败')
    }
  } catch (error) {
    console.error('获取学生信息失败', error)
    Message.error('获取学生信息失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const contactStudent = () => {
  const phone = studentInfo.value?.phone
  if (phone) {
    // 清理电话号码中的特殊字符（采用第二个文件的清理逻辑）
    const cleanPhone = phone.replace(/\*/g, '').replace(/\s/g, '')
    if (cleanPhone && /^1[3-9]\d{9}$/.test(cleanPhone)) {
      window.location.href = `tel:${cleanPhone}`
    } else if (cleanPhone) {
      // 如果不是手机号格式，仍然尝试拨号
      window.location.href = `tel:${cleanPhone}`
    } else {
      Message.warning('暂无有效联系电话')
    }
  } else {
    Message.warning('暂无联系电话')
  }
}

const viewArchive = () => {
  // 查看完整档案（可以跳转到更详细的档案页面）
  Modal.info({
    title: '学生档案',
    content: `
      姓名：${studentInfo.value?.name || '-'}
      学号：${studentInfo.value?.studentId || '-'}
      班级：${studentInfo.value?.className || '-'}
      学院：${studentInfo.value?.college || '-'}
      联系电话：${studentInfo.value?.phone || '-'}
      紧急联系人：${studentInfo.value?.emergencyContact || '-'}
      紧急联系电话：${studentInfo.value?.emergencyPhone || '-'}
    `,
    okText: '关闭',
    simple: false
  })
}

onMounted(() => {
  fetchStudentInfo()
})
</script>

<style scoped>
.student-archive {
  background: #f0f2f6;
  min-height: 100vh;
  padding: 24px;
}

/* 面包屑导航（第二个文件） */
.breadcrumb-bar {
  max-width: 900px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: white;
  border-radius: 40px;
  font-weight: 500;
  color: #3b82f6;
  border: 1px solid #e2e8f0;
  padding: 6px 18px;
}

.back-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.back-icon {
  font-size: 18px;
  line-height: 1;
}

.page-title {
  background: white;
  padding: 6px 20px;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  border: 1px solid #e2e8f0;
}

/* 主卡片（第二个文件） */
.archive-card {
  max-width: 900px;
  margin: 0 auto;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #eef2f6;
}

/* 头部区域（第二个文件） */
.info-header {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  padding: 28px 32px;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.header-details h2 {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px 0;
}

.header-details p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.gender-tag {
  background: #eef2ff;
  padding: 4px 14px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
  margin-left: auto;
}

/* 信息网格 - 两列布局（第二个文件） */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  background: white;
}

.info-item {
  display: flex;
  flex-direction: column;
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
  border-right: 1px solid #f1f5f9;
}

.info-item:nth-child(even) {
  border-right: none;
}

.info-item label {
  font-size: 12px;
  color: #94a3b8;
  letter-spacing: 0.3px;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.info-item span {
  font-size: 15px;
  font-weight: 500;
  color: #1e293b;
}

/* 底部按钮（结合两者） */
.card-footer {
  padding: 24px 32px 32px;
  border-top: 1px solid #edf2f7;
  display: flex;
  justify-content: center;
  background: white;
}

.contact-btn {
  background: #3b82f6;
  border-radius: 40px;
  padding: 8px 28px;
  font-size: 15px;
  font-weight: 500;
  border: none;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.2);
}

.contact-btn:hover {
  background: #2563eb;
}

.archive-btn {
  border-radius: 40px;
  padding: 8px 28px;
  font-size: 15px;
  font-weight: 500;
  border: 1px solid #cbd5e1;
  color: #475569;
}

.archive-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #f8fafc;
}

.loading,
.empty {
  text-align: center;
  padding: 80px;
}
</style>