<template>
  <div class="test-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>心理测评</h1>
      <p>专业心理量表，科学了解自己</p>
    </div>

    <!-- 搜索框 -->
    <div class="search-section">
      <a-input-search
        v-model="keyword"
        placeholder="搜索测评名称..."
        style="max-width: 400px"
        allow-clear
        @search="handleSearch"
      />
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <a-tabs v-model:active-key="activeCategory" @change="handleCategoryChange">
        <a-tab-pane key="all" title="全部" />
        <a-tab-pane key="personality" title="性格测试" />
        <a-tab-pane key="emotion" title="情绪评估" />
        <a-tab-pane key="ability" title="能力测试" />
        <a-tab-pane key="relationship" title="人际关系" />
      </a-tabs>
    </div>

    <!-- 测评列表 -->
    <div class="test-grid" v-loading="loading">
      <div v-for="test in testList" :key="test.id" class="test-card">
        <div class="test-icon">{{ test.icon }}</div>
        <h3>{{ test.name }}</h3>
        <p>{{ test.description }}</p>
        <div class="test-meta">
          <span><icon-clock-circle /> {{ test.duration }}分钟</span>
          <span><icon-edit /> {{ test.questionCount }}题</span>
          <span><icon-user /> {{ test.testCount }}人测过</span>
        </div>
        <div class="test-tags">
          <a-tag v-for="tag in test.tags" :key="tag" size="small">{{ tag }}</a-tag>
        </div>
        <a-button type="primary" long @click="startTest(test.id)">开始测评</a-button>
      </div>
    </div>

    <!-- 空状态 -->
    <a-empty v-if="!loading && testList.length === 0" description="暂无测评" />

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <a-pagination
        v-model:current="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        show-total
        @change="loadTests"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()

// 数据
const loading = ref(false)
const keyword = ref('')
const activeCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const testList = ref<any[]>([])

/**
 * 加载测评列表
 */
const loadTests = async () => {
  loading.value = true
  try {
    // TODO: 调用实际API
    // const res = await getTestList({
    //   keyword: keyword.value,
    //   category: activeCategory.value,
    //   current: currentPage.value,
    //   pageSize: pageSize.value
    // })
    // testList.value = res.data.records
    // total.value = res.data.total
    
    // 模拟数据（使用真实 emoji 图标）
    await new Promise(resolve => setTimeout(resolve, 500))
    testList.value = [
      {
        id: 1,
        name: 'MBTI职业性格测试',
        icon: '🔮',
        description: '经典93题，解读你的性格偏好与职业方向，帮助找到适合的工作领域',
        duration: 15,
        questionCount: 93,
        testCount: 12580,
        tags: ['经典', '专业', '职业发展'],
        category: 'personality'
      },
      {
        id: 2,
        name: 'SAS焦虑自评量表',
        icon: '😟',
        description: '专业焦虑水平评估，提供缓解建议和放松技巧',
        duration: 5,
        questionCount: 20,
        testCount: 8920,
        tags: ['SAS', '情绪', '快速'],
        category: 'emotion'
      },
      {
        id: 3,
        name: '抑郁自评量表(SDS)',
        icon: '🌧️',
        description: '了解近期情绪状态，评估抑郁倾向，获取专业支持资源',
        duration: 5,
        questionCount: 20,
        testCount: 10500,
        tags: ['SDS', '情绪', '专业'],
        category: 'emotion'
      },
      {
        id: 4,
        name: '人际关系综合诊断',
        icon: '👥',
        description: '评估人际交往能力与困扰程度，提升社交技巧',
        duration: 8,
        questionCount: 28,
        testCount: 5680,
        tags: ['人际关系', '社交'],
        category: 'relationship'
      },
      {
        id: 5,
        name: '艾森克人格问卷(EPQ)',
        icon: '🧠',
        description: '测量内外向、神经质、精神质三个人格维度',
        duration: 10,
        questionCount: 88,
        testCount: 4320,
        tags: ['经典', '人格'],
        category: 'personality'
      },
      {
        id: 6,
        name: '压力应对方式问卷',
        icon: '💪',
        description: '了解你面对压力时的应对策略，学习更有效的应对方式',
        duration: 6,
        questionCount: 36,
        testCount: 3780,
        tags: ['压力', '应对'],
        category: 'ability'
      }
    ]
    total.value = 36
  } catch (error) {
    console.error('加载测评列表失败:', error)
    Message.error('加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  loadTests()
}

/**
 * 分类切换
 */
const handleCategoryChange = () => {
  currentPage.value = 1
  loadTests()
}

/**
 * 开始测评
 * @param testId 测评ID
 */
const startTest = (testId: number) => {
  console.log('开始测评，testId:', testId)
  // 跳转到答题页面
  router.push(`/answer/do/${testId}`)
}

onMounted(() => {
  loadTests()
})
</script>

<style scoped lang="scss">
.test-list-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;

  .page-header {
    text-align: center;
    margin-bottom: 32px;

    h1 {
      font-size: 32px;
      font-weight: 600;
      color: #1d2129;
      margin-bottom: 12px;
    }

    p {
      font-size: 16px;
      color: #86909c;
    }
  }

  .search-section {
    display: flex;
    justify-content: center;
    margin-bottom: 24px;
  }

  .category-tabs {
    margin-bottom: 32px;
    display: flex;
    justify-content: center;
  }

  .test-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 24px;
    margin-bottom: 32px;
  }

  .test-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    border: 1px solid #e5e6eb;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
    }

    .test-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }

    h3 {
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 12px;
      color: #1d2129;
    }

    p {
      color: #4e5969;
      line-height: 1.5;
      margin-bottom: 16px;
      min-height: 48px;
    }

    .test-meta {
      display: flex;
      gap: 16px;
      color: #86909c;
      font-size: 13px;
      margin-bottom: 12px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .test-tags {
      margin-bottom: 20px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    :deep(.arco-btn) {
      border-radius: 24px;
      height: 40px;
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 32px;
  }
}
</style>