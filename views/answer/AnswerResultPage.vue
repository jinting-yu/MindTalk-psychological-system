<template>
  <div id="answerResultPage">
    <a-card :loading="loading">
      <!-- 错误状态 -->
      <div v-if="error" class="error-container">
        <a-result status="error" :title="error.title" :subtitle="error.message">
          <template #extra>
            <a-space>
              <a-button type="primary" @click="goBack">返回首页</a-button>
              <a-button @click="retryLoad">重试</a-button>
            </a-space>
          </template>
        </a-result>
      </div>

      <!-- 正常内容 -->
      <div v-else-if="data && data.resultId">
        <a-row style="margin-bottom: 16px">
          <a-col flex="auto" class="content-wrapper">
            <h2>{{ data.resultName || '测评结果' }}</h2>
            
            <!-- 风险等级标签 -->
            <a-tag v-if="riskLevel" :color="getLevelColor(riskLevel)" size="large">
              风险等级：{{ riskLevel }}
            </a-tag>
            
            <!-- 总分显示 -->
            <div v-if="data.totalScore !== undefined" class="score-container">
              <a-statistic 
                title="总分" 
                :value="data.totalScore" 
                :value-style="{ color: getScoreColor(data.totalScore), fontSize: '32px' }"
              />
            </div>
            
            <p v-if="data.resultDesc"><strong>结果描述：</strong>{{ data.resultDesc }}</p>
            <p v-if="data.risk"><strong>风险评估：</strong>{{ data.risk }}</p>
            <p v-if="data.resultScore !== undefined"><strong>结果得分：</strong>{{ data.resultScore }}</p>
            <p v-if="data.choices && data.choices.length">
              <strong>我的答案：</strong>
              <a-tag v-for="(choice, idx) in data.choices" :key="idx" style="margin: 2px">
                {{ choice }}
              </a-tag>
            </p>
            <p v-if="data.appName"><strong>测评应用：</strong>{{ data.appName }}</p>
            <p v-if="data.appType"><strong>应用类型：</strong>{{ APP_TYPE_MAP[data.appType] || data.appType }}</p>
            <p v-if="data.scoringStrategy">
              <strong>评分策略：</strong>{{ APP_SCORING_STRATEGY_MAP[data.scoringStrategy] || data.scoringStrategy }}
            </p>
            <p v-if="data.user">
              <a-space>
                <strong>答题人：</strong>
                <div :style="{ display: 'flex', alignItems: 'center' }">
                  <a-avatar
                    :size="24"
                    :image-url="data.user?.userAvatar"
                    :style="{ marginRight: '8px' }"
                  />
                  <a-typography-text>
                    {{ data.user?.userName ?? data.user?.username ?? "无名" }}
                  </a-typography-text>
                </div>
              </a-space>
            </p>
            <p v-if="data.createTime">
              <strong>答题时间：</strong>{{ dayjs(data.createTime).format("YYYY-MM-DD HH:mm:ss") }}
            </p>
            
            <!-- 报告内容 -->
            <div v-if="data.content" class="report-content">
              <h3>详细报告</h3>
              <div class="content-box">{{ data.content }}</div>
            </div>
            
            <!-- 根据风险等级动态显示按钮 -->
            <div class="action-buttons">
              <!-- 正常/轻度：显示建议和科普 -->
              <template v-if="isNormalOrMild()">
                <a-space size="medium" direction="vertical" fill>
                  <div class="suggestion-box suggestion-normal">
                    <span>📖 {{ getSuggestionText() }}</span>
                  </div>
                  <a-space size="medium" wrap>
                    <a-button type="primary" @click="goToNews">
                      了解更多心理知识
                    </a-button>
                    <a-button @click="goToDoAnswer">
                      重新测评
                    </a-button>
                    <a-button @click="goToTestList">
                      返回测评列表
                    </a-button>
                    <a-button @click="goToMyAnswers">
                      查看我的记录
                    </a-button>
                    <a-button type="outline" @click="goToBooking">
                      <template #icon><icon-calendar /></template>
                      心理预约
                    </a-button>
                  </a-space>
                </a-space>
              </template>

              <!-- 中度：显示建议和预约入口 -->
              <template v-else-if="isModerate()">
                <a-space size="medium" direction="vertical" fill>
                  <div class="suggestion-box suggestion-moderate">
                    <span>💡 {{ getSuggestionText() }}</span>
                  </div>
                  <a-space size="medium" wrap>
                    <a-button type="primary" @click="goToBooking">
                      📅 预约心理咨询
                    </a-button>
                    <a-button @click="goToNews">
                      📖 心理自助文章
                    </a-button>
                    <a-button @click="goToVideos">
                      🎬 放松减压视频
                    </a-button>
                    <a-button @click="goToDoAnswer">
                      重新测评
                    </a-button>
                    <a-button @click="goToMyBooking">
                      📋 我的预约
                    </a-button>
                  </a-space>
                </a-space>
              </template>

              <!-- 重度：突出显示预约按钮 -->
              <template v-else-if="isSevere()">
                <a-space size="medium" direction="vertical" fill>
                  <div class="suggestion-box suggestion-severe">
                    <div class="warning-header">
                      <span class="warning-icon">⚠️</span>
                      <span class="warning-title">需要关注</span>
                    </div>
                    <p class="warning-text">{{ getSuggestionText() }}</p>
                  </div>
                  <a-space size="medium" wrap>
                    <a-button type="primary" size="large" class="urgent-btn" @click="goToBooking">
                      🚨 立即预约心理咨询
                    </a-button>
                    <a-button status="danger" @click="goToEmergency">
                      📞 紧急求助
                    </a-button>
                    <a-button @click="goToNews">
                      📖 心理自助文章
                    </a-button>
                    <a-button @click="goToMyBooking">
                      📋 我的预约
                    </a-button>
                  </a-space>
                </a-space>
              </template>

              <!-- 默认按钮 -->
              <template v-else>
                <a-space size="medium" wrap>
                  <a-button type="primary" @click="goToDoAnswer">
                    重新测评
                  </a-button>
                  <a-button @click="goToTestList">
                    返回测评列表
                  </a-button>
                  <a-button @click="goToMyAnswers">
                    查看我的记录
                  </a-button>
                  <a-button type="outline" @click="goToNews">
                    <template #icon><icon-news /></template>
                    心理资讯
                  </a-button>
                  <a-button type="outline" @click="goToBooking">
                    <template #icon><icon-calendar /></template>
                    心理预约
                  </a-button>
                </a-space>
              </template>
            </div>
          </a-col>
          
          <a-col v-if="data.userAnswerIcon" flex="320px">
            <a-image width="100%" :src="data.userAnswerIcon" />
          </a-col>
        </a-row>
      </div>

      <!-- 空数据 -->
      <div v-else-if="!loading && !error" class="empty-container">
        <a-empty description="暂无测评结果数据" />
      </div>
    </a-card>

    <!-- 紧急求助弹窗 -->
    <a-modal v-model:visible="emergencyModalVisible" title="紧急求助" @ok="handleEmergencyCall">
      <div class="emergency-content">
        <p>如果您感到极度不适或有伤害自己/他人的想法，请立即：</p>
        <ul>
          <li>拨打全国心理危机干预热线：<strong>400-161-9995</strong></li>
          <li>拨打希望24热线：<strong>400-161-9995</strong></li>
          <li>联系学校心理咨询中心</li>
          <li>前往附近医院心理科就诊</li>
        </ul>
      </div>
      <template #footer>
        <a-button type="primary" @click="handleEmergencyCall">
          拨打求助热线
        </a-button>
        <a-button @click="emergencyModalVisible = false">
          关闭
        </a-button>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import message from "@arco-design/web-vue/es/message";
import { dayjs } from "@arco-design/web-vue/es/_utils/date";
import { APP_SCORING_STRATEGY_MAP, APP_TYPE_MAP } from "@/constant/app";
import { IconNews, IconCalendar } from '@arco-design/web-vue/es/icon';
import request from '@/request';

interface Props {
  id?: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const error = ref<{ title: string; message: string } | null>(null);
const data = ref<any>({});
const emergencyModalVisible = ref(false);
const loginUser = ref<any>(null);

/**
 * 获取风险等级（优先使用后端返回的level，否则根据分数计算）
 */
const riskLevel = computed(() => {
  if (data.value?.level) return data.value.level;
  
  const score = data.value?.totalScore;
  if (score !== undefined) {
    if (score >= 70) return '重度';
    if (score >= 60) return '中度';
    if (score >= 50) return '轻度';
    return '正常';
  }
  return '正常';
});

/**
 * 判断是否正常/轻度
 */
const isNormalOrMild = () => {
  return riskLevel.value === '正常' || riskLevel.value === '轻度';
};

/**
 * 判断是否中度
 */
const isModerate = () => {
  return riskLevel.value === '中度';
};

/**
 * 判断是否重度
 */
const isSevere = () => {
  return riskLevel.value === '重度' || riskLevel.value === '严重';
};

/**
 * 获取建议文本
 */
const getSuggestionText = () => {
  const level = riskLevel.value;
  const suggestions: Record<string, string> = {
    '正常': '您的心理状态良好，继续保持！建议定期进行心理健康自检。',
    '轻度': '您有轻度心理困扰，可以通过自我调节、运动、冥想等方式改善。',
    '中度': '您的状况需要关注，建议预约心理咨询，获得专业帮助。',
    '重度': '您的状况需要重视，强烈建议立即寻求专业心理帮助！'
  };
  return suggestions[level] || '如有需要，可寻求专业心理帮助。';
};

/**
 * 获取风险等级对应的颜色
 */
const getLevelColor = (level: string) => {
  const colorMap: Record<string, string> = {
    '正常': 'green',
    '轻度': 'blue',
    '中度': 'orange',
    '重度': 'red',
    '严重': 'red'
  };
  return colorMap[level] || 'gray';
};

/**
 * 获取分数对应的颜色
 */
const getScoreColor = (score: number) => {
  if (score < 50) return '#00b42a';
  if (score < 60) return '#ff7d00';
  return '#f53f3f';
};

/**
 * 根据等级获取描述
 */
const getDescriptionByLevel = (level: string): string => {
  if (level.includes('重度')) return '需要立即关注，建议寻求专业帮助';
  if (level.includes('中度')) return '建议预约心理咨询';
  if (level.includes('轻度')) return '可以通过自我调节改善';
  return '心理健康状况良好，请继续保持';
};

/**
 * 根据等级和分数获取详细报告
 */
const getReportContent = (level: string, score: number): string => {
  if (level.includes('重度')) {
    return `您的测评得分为 ${score} 分，属于${level}。建议您尽快联系心理咨询中心或专业心理医生，获得及时的心理支持和干预。同时，请保持与家人朋友的联系，不要独自承受。`;
  }
  if (level.includes('中度')) {
    return `您的测评得分为 ${score} 分，属于${level}。建议您预约心理咨询，与专业咨询师探讨当前困扰，学习有效的应对策略。`;
  }
  if (level.includes('轻度')) {
    return `您的测评得分为 ${score} 分，属于${level}。建议您通过运动、冥想、与朋友交流等方式进行自我调节，也可以阅读相关心理自助书籍。`;
  }
  return `您的测评得分为 ${score} 分，${level}。您的心理状态良好，请继续保持健康的生活方式和积极的心态。`;
};

/**
 * 获取用户信息
 */
const getUserInfo = async () => {
  try {
    const res = await request({
      url: '/api/user/get/login',
      method: 'GET'
    });
    if (res.code === 20000 || res.code === 200) {
      loginUser.value = res.data;
    }
  } catch (e) {
    console.warn('获取用户信息失败', e);
  }
};

/**
 * 加载数据 - 从 exam_record 表读取
 */
const loadData = async () => {
  let resultId = props.id;
  
  if (!resultId) {
    resultId = route.params.id as string;
  }
  
  if (!resultId) {
    console.error('缺少结果 ID');
    error.value = {
      title: '参数错误',
      message: '缺少测评结果ID，请从正确入口访问'
    };
    return;
  }

  console.log('开始加载测评结果，ID:', resultId);
  loading.value = true;
  error.value = null;

  try {
    const res = await request({
      url: '/api/exam/record',
      method: 'GET'
    });
    
    console.log('exam/record 返回:', res);
    
    const code = res.code ?? res.data?.code;
    let records = [];
    
    if (code === 20000 || code === 200) {
      records = res.data?.data ?? res.data ?? [];
    }
    
    console.log('所有测评记录:', records);
    
    const currentRecord = records.find((r: any) => r.id == resultId);
    
    if (currentRecord) {
      data.value = {
        resultId: currentRecord.id,
        totalScore: currentRecord.score || currentRecord.totalScore,
        level: currentRecord.level,
        risk: currentRecord.suggestion || currentRecord.risk,
        resultDesc: currentRecord.suggestion || getDescriptionByLevel(currentRecord.level),
        resultName: currentRecord.scaleName || '心理测评',
        appName: currentRecord.scaleName || '心理测评',
        createTime: currentRecord.createTime,
        content: getReportContent(currentRecord.level, currentRecord.score || currentRecord.totalScore),
        choices: currentRecord.answers ? (typeof currentRecord.answers === 'string' ? JSON.parse(currentRecord.answers) : currentRecord.answers) : [],
        user: loginUser.value ? {
          userName: loginUser.value.userName || loginUser.value.userAccount,
          userAvatar: loginUser.value.avatar
        } : null
      };
      
      console.log('加载成功:', data.value);
    } else {
      error.value = {
        title: '数据为空',
        message: '未找到对应的测评结果'
      };
    }
  } catch (err: any) {
    console.error('请求异常:', err);
    error.value = {
      title: '网络错误',
      message: err?.message || '请检查网络连接后重试'
    };
    message.error('获取数据失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

/**
 * 重试加载
 */
const retryLoad = () => {
  loadData();
};

/**
 * 返回首页
 */
const goBack = () => {
  router.push('/');
};

/**
 * 重新测评
 */
const goToDoAnswer = () => {
  if (data.value.appId) {
    router.push(`/answer/do/${data.value.appId}`);
  } else {
    router.push('/test/list');
  }
};

/**
 * 返回测评列表
 */
const goToTestList = () => {
  router.push('/test/list');
};

/**
 * 查看我的记录
 */
const goToMyAnswers = () => {
  router.push('/answer/my');
};

/**
 * 跳转到心理资讯页面
 */
const goToNews = () => {
  router.push('/news');
};

/**
 * 跳转到心理预约页面（学生端）
 */
const goToBooking = () => {
  router.push({
    path: '/booking/create',
    query: {
      from: 'test',
      result: riskLevel.value,
      testName: data.value.appName,
      score: data.value.totalScore || data.value.resultScore
    }
  });
};

/**
 * 跳转到我的预约页面
 */
const goToMyBooking = () => {
  router.push('/booking/my');
};

/**
 * 跳转到心理视频页面
 */
const goToVideos = () => {
  router.push('/video');
};

/**
 * 紧急求助
 */
const goToEmergency = () => {
  emergencyModalVisible.value = true;
};

const handleEmergencyCall = () => {
  window.location.href = 'tel:4001619995';
};

// 监听 ID 变化，重新加载数据
watchEffect(() => {
  const id = props.id || (route.params.id as string);
  if (id) {
    getUserInfo();
    loadData();
  }
});

// 组件挂载时检查
onMounted(() => {
  const id = props.id || (route.params.id as string);
  if (!id) {
    console.warn('AnswerResultPage 未接收到 id 参数');
  } else {
    getUserInfo();
    loadData();
  }
});
</script>

<style scoped>
#answerResultPage {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

#answerResultPage :deep(.arco-card) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

#answerResultPage .content-wrapper > * {
  margin-bottom: 24px;
}

.content-wrapper {
  line-height: 1.6;
}

.content-wrapper h2 {
  color: #1d2129;
  border-left: 4px solid #165dff;
  padding-left: 16px;
}

.score-container {
  background: #f7f8fa;
  padding: 16px 20px;
  border-radius: 12px;
  text-align: center;
}

.report-content {
  background: #f7f8fa;
  padding: 20px;
  border-radius: 12px;
}

.report-content h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #1d2129;
}

.content-box {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #4e5969;
}

.error-container, .empty-container {
  padding: 60px 20px;
  text-align: center;
}

strong {
  color: #1d2129;
}

.action-buttons {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e6eb;
}

.suggestion-box {
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 16px;
}

.suggestion-normal {
  background: #e8f4ff;
  color: #165dff;
}

.suggestion-moderate {
  background: #fff7e6;
  color: #ff7d00;
}

.suggestion-severe {
  background: #ffece8;
  color: #f53f3f;
}

.warning-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.warning-icon {
  font-size: 24px;
}

.warning-title {
  font-size: 18px;
  font-weight: bold;
}

.warning-text {
  margin: 0;
  font-size: 14px;
}

.urgent-btn {
  background: #f53f3f;
  border-color: #f53f3f;
  animation: pulse 1.5s infinite;
}

.urgent-btn:hover {
  background: #cb2634;
  border-color: #cb2634;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 63, 63, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(245, 63, 63, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 63, 63, 0);
  }
}

.emergency-content ul {
  margin: 16px 0;
  padding-left: 20px;
}

.emergency-content li {
  margin: 8px 0;
  line-height: 1.6;
}

.emergency-content strong {
  color: #f53f3f;
}

:deep(.arco-btn-outline) {
  transition: all 0.3s ease;
}

:deep(.arco-btn-outline:hover) {
  transform: translateY(-1px);
}
</style>