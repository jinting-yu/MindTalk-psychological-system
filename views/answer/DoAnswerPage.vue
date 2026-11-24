<template>
  <div id="doAnswerPage">
    <a-card :loading="loading">
      <h1>{{ app.appName }}</h1>
      <p>{{ app.appDesc }}</p>
      
      <!-- 题目进度 -->
      <div class="progress-section">
        <a-progress :percent="Math.round((current / questionContent.length) * 100)" />
        <div class="progress-text">第 {{ current }} / {{ questionContent.length }} 题</div>
      </div>

      <h2 style="margin-bottom: 16px">
        {{ current }}、{{ currentQuestion?.title }}
      </h2>
      
      <div>
        <a-radio-group
          direction="vertical"
          v-model="currentAnswer"
          :options="questionOptions"
          @change="doRadioChange"
        />
      </div>
      
      <div style="margin-top: 24px">
        <a-space size="large">
          <a-button
            type="primary"
            v-if="current < questionContent.length"
            :disabled="!currentAnswer"
            @click="nextQuestion"
          >
            下一题
          </a-button>
          <a-button
            type="primary"
            v-if="current === questionContent.length"
            :loading="submitting"
            :disabled="!currentAnswer"
            @click="doSubmit"
          >
            {{ submitting ? "提交中..." : "查看结果" }}
          </a-button>
          <a-button v-if="current > 1" @click="prevQuestion">
            上一题
          </a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  ref,
  watchEffect,
  onMounted,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import message from "@arco-design/web-vue/es/message";
import { getAppVoByIdUsingGet } from "@/api/appController";
import request from '@/request';

const route = useRoute();
const router = useRouter();

// 从路由参数获取 appId
const appId = ref<string>("");
const loading = ref(false);
const app = ref<any>({});
const questionContent = ref<any[]>([]);

const current = ref(1);
const currentQuestion = ref<any>({});
const questionOptions = computed(() => {
  return currentQuestion.value?.options
    ? currentQuestion.value.options.map((option: any) => {
        return {
          label: `${option.key}. ${option.value}`,
          value: option.key,
        };
      })
    : [];
});
const currentAnswer = ref<string>();
const answerList = ref<string[]>([]);
const submitting = ref(false);

/**
 * 加载应用和题目数据
 */
const loadData = async () => {
  if (!appId.value) {
    console.error('appId 为空');
    message.error('应用ID不存在');
    return;
  }

  loading.value = true;
  console.log('开始加载数据, appId:', appId.value);

  try {
    // 1. 获取应用信息
    const appRes = await getAppVoByIdUsingGet({
      id: appId.value as any,
    });

    console.log('App接口返回:', appRes);

    const appCode = appRes.data?.code ?? appRes.code;
    if (appCode === 200 || appCode === 20000) {
      app.value = appRes.data?.data ?? appRes.data;
      console.log('App加载成功:', app.value);
    } else {
      const errorMsg = appRes.data?.message || appRes.data?.msg || '获取应用失败';
      message.error(errorMsg);
      return;
    }

    // 2. 获取题目列表
    const questionRes = await listQuestionVoByPageUsingPost({
      appId: appId.value as any,
      current: 1,
      pageSize: 100,
    });

    console.log('题目接口返回:', questionRes);
    const questionCode = questionRes.data?.code ?? questionRes.code;
    
    if (questionCode === 200 || questionCode === 20000) {
      const data = questionRes.data?.data ?? questionRes.data;
      console.log('题目数据:', data);

      // 多种格式兼容
      if (data && data.questionContent && Array.isArray(data.questionContent)) {
        questionContent.value = data.questionContent;
      } else if (Array.isArray(data)) {
        questionContent.value = data;
      } else if (data && data.records && data.records.length > 0 && data.records[0].questionContent) {
        questionContent.value = data.records[0].questionContent;
      } else if (data && data.list && data.list.length > 0 && data.list[0].questionContent) {
        questionContent.value = data.list[0].questionContent;
      } else {
        console.warn('未找到题目数据，data结构:', data);
        message.error("获取题目失败：数据格式错误");
        return;
      }

      console.log('成功加载题目，数量:', questionContent.value.length);
      
      // 初始化答案数组
      answerList.value = new Array(questionContent.value.length);
      
    } else {
      const errorMsg = questionRes.data?.message || questionRes.data?.msg || '获取题目失败';
      message.error(errorMsg);
    }
  } catch (error: any) {
    console.error('加载数据失败:', error);
    message.error(error?.message || '加载数据失败，请重试');
  } finally {
    loading.value = false;
  }
};

/**
 * 下一题
 */
const nextQuestion = () => {
  if (current.value < questionContent.value.length) {
    // 保存当前答案
    if (currentAnswer.value) {
      answerList.value[current.value - 1] = currentAnswer.value;
    }
    current.value += 1;
  }
};

/**
 * 上一题
 */
const prevQuestion = () => {
  if (current.value > 1) {
    // 保存当前答案
    if (currentAnswer.value) {
      answerList.value[current.value - 1] = currentAnswer.value;
    }
    current.value -= 1;
  }
};

/**
 * 选中选项后，保存选项记录
 */
const doRadioChange = (value: string) => {
  answerList.value[current.value - 1] = value;
  console.log('当前答案列表:', answerList.value);
};

/**
 * 计算总分
 * 根据选项计算得分 (A=1, B=2, C=3, D=4)
 */
/**
 * 计算总分
 * 根据选项值转换为分数
 */
const calculateTotalScore = (answers: string[]): number => {
  console.log('========== 计算总分 ==========');
  console.log('原始答案列表:', answers);
  
  let totalScore = 0;
  for (let i = 0; i < answers.length; i++) {
    const answer = answers[i];
    let score = 0;
    
    // 将答案转换为数字分数
    const numValue = Number(answer);
    if (!isNaN(numValue)) {
      // 如果答案本身就是数字（如 '1', '2', '3', '4', '5'）
      score = numValue;
    } else if (answer === 'A' || answer === 'a') {
      score = 1;
    } else if (answer === 'B' || answer === 'b') {
      score = 2;
    } else if (answer === 'C' || answer === 'c') {
      score = 3;
    } else if (answer === 'D' || answer === 'd') {
      score = 4;
    } else {
      // 默认给 1 分
      score = 1;
    }
    
    totalScore += score;
    console.log(`第${i+1}题: 答案=${answer}, 得分=${score}, 累计=${totalScore}`);
  }
  
  console.log('最终总分:', totalScore);
  console.log('===============================');
  return totalScore;
};

/**
 * 提交答案 - 调用 /api/exam/submit 接口
 */
const doSubmit = async () => {
  // 验证应用ID
  if (!appId.value) {
    message.error('应用ID不存在');
    return;
  }

  // 保存最后一题的答案
  if (currentAnswer.value) {
    answerList.value[current.value - 1] = currentAnswer.value;
  }

  // 验证是否完成所有题目
  const answeredCount = answerList.value.filter(a => a !== undefined && a !== null && a !== '').length;
  
  if (answeredCount !== questionContent.value.length) {
    message.warning(`请完成所有题目 (已完成 ${answeredCount}/${questionContent.value.length})`);
    return;
  }

  // 计算总分
  const totalScore = calculateTotalScore(answerList.value);
  console.log('答案列表:', answerList.value);
  console.log('总分:', totalScore);

  submitting.value = true;
  
  try {
    // 调用后端 /api/exam/submit 接口
    const res = await request({
      url: '/api/exam/submit',
      method: 'POST',
      data: {
        scaleId: Number(appId.value),  // 测评ID
        totalScore: totalScore,         // 总分
      }
    });

    console.log('提交结果:', res);

    // 兼容多种返回格式
    const code = res.code ?? res.data?.code;
    const recordId = res.data?.id ?? res.data?.data?.id ?? res.id;
    
    if (code === 20000 || code === 200 || code === 0) {
      message.success('提交成功！正在查看结果...');
      // 跳转到结果页，传递记录ID
      setTimeout(() => {
        if (recordId) {
          router.push(`/answer/result/${recordId}`);
        } else {
          // 如果没有返回ID，跳转到个人中心
          router.push('/user/profile');
        }
      }, 500);
    } else {
      const errorMsg = res.msg || res.message || res.data?.message || '提交失败';
      message.error(errorMsg);
    }
  } catch (error: any) {
    console.error('提交失败:', error);
    
    if (error.response?.status === 401) {
      message.error('登录已过期，请重新登录');
      setTimeout(() => router.push('/user/login'), 1500);
    } else if (error.response?.status === 500) {
      message.error('服务器错误，请稍后重试');
    } else {
      message.error(error.message || '提交答案失败，请重试');
    }
  } finally {
    submitting.value = false;
  }
};

// 监听当前题目变化，更新当前答案
watchEffect(() => {
  if (questionContent.value && questionContent.value.length > 0) {
    currentQuestion.value = questionContent.value[current.value - 1];
    currentAnswer.value = answerList.value[current.value - 1];
  }
});

// 监听路由参数变化
watchEffect(() => {
  const id = route.params.appId as string;
  if (id) {
    appId.value = id;
    loadData();
  }
});

// 组件挂载时检查
onMounted(() => {
  const id = route.params.appId as string;
  if (!id) {
    console.error('DoAnswerPage 缺少 appId 参数');
    message.error('参数错误，请返回重试');
  } else {
    appId.value = id;
    loadData();
  }
});
</script>

<style scoped>
#doAnswerPage {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

#doAnswerPage :deep(.arco-card) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

#doAnswerPage :deep(.arco-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

#doAnswerPage :deep(.arco-radio) {
  padding: 8px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  transition: all 0.3s;
}

#doAnswerPage :deep(.arco-radio-checked) {
  border-color: #165dff;
  background-color: #f2f3ff;
}

.progress-section {
  margin-bottom: 24px;
}

.progress-text {
  text-align: center;
  margin-top: 8px;
  color: #86909c;
  font-size: 13px;
}
</style>