<template>
  <div class="my-answer-page">
    <!-- 页面头部装饰 -->
    <div class="page-header-bg"></div>
    
    <div class="page-container">
      <!-- 返回按钮和标题区域 -->
      <div class="page-header">
        <div class="header-left">
          <a-button class="back-btn" @click="goBack">
            <icon-left /> 返回
          </a-button>
          <div class="header-title">
            <h1>我的测评记录</h1>
            <p>🌿 记录你的每一次心灵成长</p>
          </div>
        </div>
      </div>

      <!-- 最新测评结果卡片 -->
      <div class="latest-result-card" v-if="latestData && latestData.hasRecord && latestData.latest">
        <div class="latest-leaf">🍃</div>
        <div class="latest-content-wrapper">
          <div class="latest-header">
            <span class="latest-icon">🌟</span>
            <span class="latest-title">最新测评结果</span>
            <a-tag :color="getLatestLevelColor(latestData.latest.level)" size="small" class="latest-tag">
              {{ latestData.latest.level }}
            </a-tag>
          </div>
          <div class="latest-content">
            <div class="latest-info">
              <div class="latest-scale">{{ latestData.latest.scaleName }}</div>
              <div class="latest-score">
                <span class="score-value">{{ latestData.latest.totalScore }}</span>
                <span class="score-unit">分</span>
              </div>
            </div>
            <div class="latest-risk">{{ latestData.latest.risk }}</div>
            <div class="latest-time">📅 {{ formatDate(latestData.latest.createTime) }}</div>
          </div>
          <div class="latest-action">
            <a-button class="view-detail-btn" @click="goToResultDetail(latestData.latest!.id)">
              查看详情 <span class="arrow">→</span>
            </a-button>
          </div>
        </div>
      </div>

      <!-- 统计卡片区域 - 使用后端返回的统计数据 -->
      <div class="stats-cards">
        <div class="stat-card" @click="showAllRecords">
          <div class="stat-icon">📊</div>
          <div class="stat-info">
            <div class="stat-value">{{ latestData?.totalCount || total || 0 }}</div>
            <div class="stat-label">总测评次数</div>
          </div>
          <div class="stat-trend">📈 心灵成长轨迹</div>
        </div>
        <div class="stat-card" @click="showLatestRecord">
          <div class="stat-icon">🕐</div>
          <div class="stat-info">
            <div class="stat-value">{{ latestData?.latestDate || latestDateStr || '--' }}</div>
            <div class="stat-label">最近测评</div>
          </div>
          <div class="stat-trend">✨ 最新记录</div>
        </div>
        <div class="stat-card" @click="showAverageScore">
          <div class="stat-icon">⭐</div>
          <div class="stat-info">
            <div class="stat-value">{{ latestData?.avgScore || averageScoreStr || '--' }}</div>
            <div class="stat-label">平均得分</div>
          </div>
          <div class="stat-trend">🎯 健康指数</div>
        </div>
      </div>

      <!-- 搜索筛选区域 -->
      <div class="search-section">
        <div class="search-wrapper">
          <div class="search-input-group">
            <span class="search-icon">🔍</span>
            <a-input
              v-model="searchKeyword"
              placeholder="搜索测评名称..."
              allow-clear
              class="search-input"
              @press-enter="doSearch"
              @clear="doSearch"
            />
          </div>
          <a-button class="search-btn" @click="doSearch">
            <icon-search /> 搜索
          </a-button>
          <a-button class="reset-btn" @click="resetSearch" v-if="searchKeyword">
            <icon-close /> 重置
          </a-button>
        </div>
      </div>

      <!-- 记录列表 - 卡片式布局 -->
      <div class="records-list" v-if="displayList.length > 0">
        <div 
          v-for="record in displayList" 
          :key="record.id" 
          class="record-card"
          :class="getCardClass(record)"
        >
          <!-- 树叶装饰 -->
          <div class="card-leaf"></div>
          
          <div class="card-header">
            <div class="card-title-section">
              <div class="card-icon">
                <span class="icon-emoji">{{ getResultIcon(record) }}</span>
              </div>
              <div class="card-title-info">
                <h3>{{ record.scaleName }}</h3>
                <div class="card-tags">
                  <span class="tag tag-level">{{ record.level }}</span>
                  <span class="tag tag-score">得分 {{ record.totalScore }}</span>
                </div>
              </div>
            </div>
            <div class="card-score">
              <div class="score-value">{{ record.totalScore }}</div>
              <div class="score-label">综合得分</div>
            </div>
          </div>

          <div class="card-body">
            <div class="result-desc">
              <span class="desc-icon">💬</span>
              <span class="desc-text">{{ record.risk || '暂无描述' }}</span>
            </div>
            <div class="result-meta">
              <div class="meta-item">
                <span class="meta-icon">🆔</span>
                <span class="meta-label">记录ID:</span>
                <span class="meta-value">{{ record.id }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-icon">📅</span>
                <span class="meta-label">测评时间:</span>
                <span class="meta-value">{{ formatDate(record.createTime) }}</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <div class="footer-actions">
              <a-button class="action-btn view-btn" @click.stop="goToResultDetail(record.id)">
                <icon-eye /> 查看详情
              </a-button>
              <a-popconfirm content="确定要删除这条记录吗？" @ok="doDelete(record)">
                <a-button class="action-btn delete-btn" status="danger" @click.stop>
                  <icon-delete /> 删除
                </a-button>
              </a-popconfirm>
            </div>
            <div class="status-badge" :class="getStatusClass(record)">
              <span class="status-dot"></span>
              <span class="status-text">{{ getStatusText(record) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-else-if="!loading">
        <div class="empty-tree">🌳</div>
        <h3>暂无测评记录</h3>
        <p>还没有进行过心理测评？<br>快来大树下聊聊，了解自己的心理状态吧~</p>
        <a-button class="go-test-btn" @click="goToTestList">
          <icon-plus /> 前往测评
        </a-button>
      </div>

      <!-- 加载状态 -->
      <div class="loading-state" v-if="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <a-pagination
          :total="total"
          :current="currentPage"
          :page-size="pageSize"
          :show-total="true"
          :show-jumper="true"
          @change="onPageChange"
          class="custom-pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import message from "@arco-design/web-vue/es/message";
import { dayjs } from "@arco-design/web-vue/es/_utils/date";
import { IconLeft, IconSearch, IconClose, IconEye, IconDelete, IconPlus } from '@arco-design/web-vue/es/icon';
import { getLatestRecord, getMyRecords, deleteRecord, type ExamRecord, type LatestRecordData } from "@/api/scaleController";

const router = useRouter();

// 数据状态
const loading = ref(false);
const recordsList = ref<ExamRecord[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(6);
const searchKeyword = ref('');
const latestData = ref<LatestRecordData | null>(null);

// 过滤后的列表
const displayList = computed(() => {
  if (!searchKeyword.value) return recordsList.value;
  const keyword = searchKeyword.value.toLowerCase();
  return recordsList.value.filter(record => 
    record.scaleName.toLowerCase().includes(keyword)
  );
});

// 最近测评日期（备用，优先使用后端返回的 latestDate）
const latestDateStr = computed(() => {
  if (recordsList.value.length > 0 && recordsList.value[0].createTime) {
    return dayjs(recordsList.value[0].createTime).format("MM-DD");
  }
  return null;
});

// 平均得分（备用，优先使用后端返回的 avgScore）
const averageScoreStr = computed(() => {
  if (recordsList.value.length === 0) return null;
  const sum = recordsList.value.reduce((acc, r) => acc + (r.totalScore || 0), 0);
  return Math.round(sum / recordsList.value.length);
});

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '--';
  return dayjs(date).format("YYYY-MM-DD HH:mm");
};

// 获取结果图标
const getResultIcon = (record: ExamRecord) => {
  const level = record.level;
  if (level === '正常' || level === '心理健康') return '🌿';
  if (level === '轻度' || level.includes('轻度')) return '🍃';
  if (level === '中度' || level.includes('中度')) return '🍂';
  return '🌾';
};

// 获取卡片样式类
const getCardClass = (record: ExamRecord) => {
  const level = record.level;
  if (level.includes('重度') || level.includes('严重')) return 'card-severe';
  if (level.includes('中度')) return 'card-moderate';
  if (level.includes('轻度')) return 'card-mild';
  return 'card-normal';
};

// 获取状态样式类
const getStatusClass = (record: ExamRecord) => {
  const level = record.level;
  if (level.includes('重度')) return 'status-severe';
  if (level.includes('中度')) return 'status-moderate';
  if (level.includes('轻度')) return 'status-mild';
  return 'status-normal';
};

// 获取状态文本
const getStatusText = (record: ExamRecord) => {
  const level = record.level;
  if (level.includes('重度')) return '需要关注';
  if (level.includes('中度')) return '建议咨询';
  if (level.includes('轻度')) return '轻微困扰';
  return '状态良好';
};

// 获取等级颜色
const getLatestLevelColor = (level: string) => {
  const colorMap: Record<string, string> = {
    '正常': 'green',
    '心理健康': 'green',
    '轻度': 'arcoblue',
    '中度': 'orangered',
    '重度': 'red'
  };
  return colorMap[level] || 'green';
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 显示所有记录
const showAllRecords = () => {
  searchKeyword.value = '';
  loadRecords();
};

// 显示最新记录详情
const showLatestRecord = () => {
  if (latestData.value?.latest) {
    goToResultDetail(latestData.value.latest.id);
  } else if (recordsList.value.length > 0) {
    goToResultDetail(recordsList.value[0].id);
  }
};

// 显示平均得分
const showAverageScore = () => {
  const avgScore = latestData.value?.avgScore || averageScoreStr.value;
  message.info(`您的平均测评得分为 ${avgScore || '--'} 分`);
};

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = '';
  loadRecords();
};

// 执行搜索
const doSearch = () => {
  currentPage.value = 1;
  // 搜索已在 computed 中处理，只需重置页码
};

// 加载最新测评记录和统计信息
const loadLatestRecord = async () => {
  try {
    const res = await getLatestRecord();
    if (res.data.code === 200 && res.data.data) {
      latestData.value = res.data.data;
    }
  } catch (error) {
    console.error('获取最新记录失败:', error);
  }
};

// 加载所有测评记录
const loadRecords = async () => {
  loading.value = true;
  try {
    const res = await getMyRecords(currentPage.value, pageSize.value);
    if (res.data.code === 200 && res.data.data) {
      recordsList.value = res.data.data.records || [];
      total.value = res.data.data.total || 0;
    } else {
      message.error(res.data.msg || '加载记录失败');
    }
  } catch (error) {
    console.error('加载记录失败:', error);
    message.error('加载记录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 分页变化
const onPageChange = (page: number) => {
  currentPage.value = page;
  loadRecords();
};

// 跳转到结果详情
const goToResultDetail = (recordId: number) => {
  router.push(`/answer/result/${recordId}`);
};

// 删除记录
const doDelete = async (record: ExamRecord) => {
  try {
    const res = await deleteRecord(record.id);
    if (res.data.code === 200) {
      message.success('删除成功');
      // 刷新列表
      await loadRecords();
      // 刷新最新记录和统计信息
      await loadLatestRecord();
    } else {
      message.error(res.data.msg || '删除失败');
    }
  } catch (error) {
    console.error('删除失败:', error);
    message.error('删除失败，请稍后重试');
  }
};

// 跳转到测评列表
const goToTestList = () => {
  router.push('/test/list');
};

// 页面加载
onMounted(() => {
  loadLatestRecord();
  loadRecords();
});
</script>

<style scoped lang="scss">
// ... 样式保持不变 ...
.my-answer-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F5E9 0%, #C8E6C9 100%);
  position: relative;
}

/* 页面头部背景 - 治愈绿色渐变 */
.page-header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 320px;
  background: linear-gradient(135deg, #5B8C6F 0%, #7CB87A 50%, #A8D5A3 100%);
  border-radius: 0 0 60px 60px;
  z-index: 0;
  box-shadow: 0 4px 20px rgba(92, 140, 111, 0.2);
}

.page-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 32px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  
  .back-btn {
    background: rgba(255, 255, 255, 0.25);
    border: 1px solid rgba(255, 255, 255, 0.4);
    color: white;
    border-radius: 30px;
    padding: 6px 20px;
    backdrop-filter: blur(10px);
    transition: all 0.3s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.35);
      transform: translateX(-2px);
    }
  }
  
  .header-title {
    h1 {
      margin: 0 0 4px;
      font-size: 28px;
      font-weight: 600;
      color: white;
      letter-spacing: 1px;
      text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
    }
    
    p {
      margin: 0;
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

/* 最新测评结果卡片 */
.latest-result-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 28px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 8px 25px rgba(92, 140, 111, 0.15);
  border: 1px solid rgba(124, 184, 122, 0.3);
  position: relative;
  overflow: hidden;
  display: flex;
  gap: 20px;
  
  .latest-leaf {
    font-size: 48px;
    opacity: 0.3;
    position: absolute;
    right: 20px;
    bottom: 20px;
    transform: rotate(15deg);
  }
  
  .latest-content-wrapper {
    flex: 1;
  }
  
  .latest-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;
    
    .latest-icon {
      font-size: 24px;
    }
    
    .latest-title {
      font-size: 16px;
      font-weight: 600;
      color: #5B8C6F;
    }
    
    .latest-tag {
      border-radius: 20px;
    }
  }
  
  .latest-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 16px;
    
    .latest-info {
      display: flex;
      align-items: baseline;
      gap: 12px;
      
      .latest-scale {
        font-size: 18px;
        font-weight: 600;
        color: #2c5f2d;
      }
      
      .latest-score {
        .score-value {
          font-size: 32px;
          font-weight: 700;
          color: #5B8C6F;
        }
        
        .score-unit {
          font-size: 14px;
          color: #95a5a6;
          margin-left: 4px;
        }
      }
    }
    
    .latest-risk {
      flex: 1;
      color: #7f8c8d;
      font-size: 14px;
    }
    
    .latest-time {
      color: #95a5a6;
      font-size: 13px;
    }
  }
  
  .latest-action {
    text-align: right;
    
    .view-detail-btn {
      background: transparent;
      border: none;
      color: #5B8C6F;
      font-weight: 500;
      
      &:hover {
        color: #7CB87A;
        .arrow {
          transform: translateX(4px);
        }
      }
      
      .arrow {
        display: inline-block;
        transition: transform 0.2s;
      }
    }
  }
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 32px;
  
  .stat-card {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(8px);
    border-radius: 24px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(92, 140, 111, 0.1);
    border: 1px solid rgba(124, 184, 122, 0.2);
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(92, 140, 111, 0.2);
      background: white;
    }
    
    .stat-icon {
      font-size: 42px;
    }
    
    .stat-info {
      flex: 1;
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #2c5f2d;
        line-height: 1.2;
      }
      
      .stat-label {
        font-size: 13px;
        color: #7f8c8d;
        margin-top: 4px;
      }
    }
    
    .stat-trend {
      font-size: 12px;
      color: #5B8C6F;
      background: rgba(92, 140, 111, 0.1);
      padding: 4px 10px;
      border-radius: 20px;
    }
  }
}

/* 搜索区域 */
.search-section {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 24px;
  padding: 20px 24px;
  margin-bottom: 32px;
  box-shadow: 0 2px 10px rgba(92, 140, 111, 0.08);
  border: 1px solid rgba(124, 184, 122, 0.2);
  
  .search-wrapper {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 12px;
    
    .search-input-group {
      flex: 1;
      display: flex;
      align-items: center;
      background: #F5F9F5;
      border-radius: 40px;
      padding: 0 16px;
      transition: all 0.3s;
      
      &:focus-within {
        background: white;
        box-shadow: 0 0 0 2px rgba(92, 140, 111, 0.2);
      }
      
      .search-icon {
        font-size: 18px;
        margin-right: 8px;
        color: #5B8C6F;
      }
      
      .search-input {
        flex: 1;
        background: transparent;
        border: none;
        
        :deep(.arco-input) {
          background: transparent;
          padding: 12px 0;
        }
      }
    }
    
    .search-btn {
      background: linear-gradient(135deg, #5B8C6F 0%, #7CB87A 100%);
      border: none;
      color: white;
      border-radius: 40px;
      padding: 8px 28px;
      font-weight: 500;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(92, 140, 111, 0.4);
      }
    }
    
    .reset-btn {
      border-radius: 40px;
      padding: 8px 20px;
      color: #7f8c8d;
      
      &:hover {
        color: #e74c3c;
        border-color: #e74c3c;
      }
    }
  }
}

/* 记录卡片列表 */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
}

.record-card {
  background: white;
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  position: relative;
  
  .card-leaf {
    position: absolute;
    top: 0;
    right: 0;
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, transparent 50%, rgba(92, 140, 111, 0.05) 50%);
    border-radius: 0 24px 0 40px;
    pointer-events: none;
  }
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 30px rgba(92, 140, 111, 0.15);
  }
  
  &.card-normal {
    border-left: 4px solid #7CB87A;
  }
  
  &.card-mild {
    border-left: 4px solid #A8D5A3;
  }
  
  &.card-moderate {
    border-left: 4px solid #FFB74D;
  }
  
  &.card-severe {
    border-left: 4px solid #EF9A9A;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 20px 24px 12px;
    
    .card-title-section {
      display: flex;
      gap: 16px;
      
      .card-icon {
        .icon-emoji {
          font-size: 44px;
        }
      }
      
      .card-title-info {
        h3 {
          margin: 0 0 8px;
          font-size: 18px;
          font-weight: 600;
          color: #2c5f2d;
        }
        
        .card-tags {
          display: flex;
          gap: 8px;
          
          .tag {
            font-size: 11px;
            padding: 2px 12px;
            border-radius: 20px;
            
            &.tag-level {
              background: rgba(92, 140, 111, 0.1);
              color: #5B8C6F;
            }
            
            &.tag-score {
              background: #F5F9F5;
              color: #7CB87A;
            }
          }
        }
      }
    }
    
    .card-score {
      text-align: center;
      background: linear-gradient(135deg, #5B8C6F 0%, #7CB87A 100%);
      padding: 12px 20px;
      border-radius: 20px;
      min-width: 90px;
      
      .score-value {
        font-size: 28px;
        font-weight: 700;
        color: white;
        line-height: 1;
      }
      
      .score-label {
        font-size: 11px;
        color: rgba(255, 255, 255, 0.85);
        margin-top: 4px;
      }
    }
  }
  
  .card-body {
    padding: 16px 24px;
    
    .result-desc {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;
      padding: 12px 16px;
      background: #F5F9F5;
      border-radius: 16px;
      
      .desc-icon {
        font-size: 20px;
      }
      
      .desc-text {
        flex: 1;
        color: #555;
        line-height: 1.5;
        font-size: 14px;
      }
    }
    
    .result-meta {
      display: flex;
      gap: 24px;
      flex-wrap: wrap;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        
        .meta-icon {
          font-size: 14px;
        }
        
        .meta-label {
          color: #95a5a6;
        }
        
        .meta-value {
          color: #2c5f2d;
          font-weight: 500;
        }
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 24px 20px;
    border-top: 1px solid #E8F5E9;
    
    .footer-actions {
      display: flex;
      gap: 12px;
      
      .action-btn {
        border-radius: 30px;
        padding: 6px 20px;
        font-size: 13px;
        
        &.view-btn {
          background: rgba(92, 140, 111, 0.08);
          border-color: transparent;
          color: #5B8C6F;
          
          &:hover {
            background: rgba(92, 140, 111, 0.15);
          }
        }
        
        &.delete-btn {
          background: rgba(231, 76, 60, 0.08);
          border-color: transparent;
          color: #e74c3c;
          
          &:hover {
            background: rgba(231, 76, 60, 0.15);
          }
        }
      }
    }
    
    .status-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px;
      border-radius: 30px;
      font-size: 12px;
      
      .status-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
      }
      
      &.status-normal {
        background: rgba(124, 184, 122, 0.1);
        .status-dot { background: #7CB87A; }
        .status-text { color: #7CB87A; }
      }
      
      &.status-mild {
        background: rgba(168, 213, 163, 0.15);
        .status-dot { background: #A8D5A3; }
        .status-text { color: #A8D5A3; }
      }
      
      &.status-moderate {
        background: rgba(255, 183, 77, 0.1);
        .status-dot { background: #FFB74D; }
        .status-text { color: #FFB74D; }
      }
      
      &.status-severe {
        background: rgba(239, 154, 154, 0.1);
        .status-dot { background: #EF9A9A; }
        .status-text { color: #EF9A9A; }
      }
    }
  }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 28px;
  
  .empty-tree {
    font-size: 80px;
    margin-bottom: 20px;
  }
  
  h3 {
    font-size: 20px;
    color: #2c5f2d;
    margin-bottom: 12px;
  }
  
  p {
    color: #95a5a6;
    margin-bottom: 24px;
    line-height: 1.6;
  }
  
  .go-test-btn {
    background: linear-gradient(135deg, #5B8C6F 0%, #7CB87A 100%);
    border: none;
    border-radius: 40px;
    padding: 10px 32px;
    color: white;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(92, 140, 111, 0.4);
    }
  }
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 28px;
  
  .loading-spinner {
    width: 40px;
    height: 40px;
    margin: 0 auto 16px;
    border: 3px solid #E8F5E9;
    border-top-color: #5B8C6F;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
  
  @keyframes spin {
    to { transform: rotate(360deg); }
  }
  
  p {
    color: #95a5a6;
  }
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  
  .custom-pagination {
    :deep(.arco-pagination-item) {
      border-radius: 12px;
      
      &.arco-pagination-item-active {
        background: linear-gradient(135deg, #5B8C6F 0%, #7CB87A 100%);
        border-color: transparent;
        
        a {
          color: white;
        }
      }
    }
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .search-section .search-wrapper {
    flex-direction: column;
    
    .search-input-group {
      width: 100%;
    }
    
    .search-btn, .reset-btn {
      width: 100%;
    }
  }
  
  .latest-result-card {
    flex-direction: column;
  }
  
  .record-card {
    .card-header {
      flex-direction: column;
      gap: 16px;
      
      .card-score {
        align-self: flex-start;
      }
    }
    
    .card-footer {
      flex-direction: column;
      gap: 12px;
      
      .footer-actions {
        width: 100%;
        
        .action-btn {
          flex: 1;
          text-align: center;
        }
      }
    }
  }
}
</style>