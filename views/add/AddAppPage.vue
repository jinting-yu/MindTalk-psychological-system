<template>
  <div id="addAppPage">
    <h2 style="margin-bottom: 32px">{{ props.id ? '编辑应用' : '创建应用' }}</h2>
    <a-form
      :model="form"
      :style="{ width: '480px' }"
      label-align="left"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item field="appName" label="应用名称" required>
        <a-input v-model="form.appName" placeholder="请输入应用名称" />
      </a-form-item>
      <a-form-item field="appDesc" label="应用描述" required>
        <a-input v-model="form.appDesc" placeholder="请输入应用描述" />
      </a-form-item>
      <a-form-item field="appIcon" label="应用图标">
        <a-input v-model="form.appIcon" placeholder="请输入应用图标" />
      </a-form-item>
      <!-- 图片上传组件（可选，根据需要启用） -->
      <!-- <a-form-item field="appIcon" label="应用图标">
        <PictureUploader
          :value="form.appIcon"
          :onChange="(value) => (form.appIcon = value)"
        />
      </a-form-item> -->
      <a-form-item field="appType" label="应用类型" required>
        <a-select
          v-model="form.appType"
          :style="{ width: '320px' }"
          placeholder="请选择应用类型"
        >
          <a-option
            v-for="(value, key) of APP_TYPE_MAP"
            :value="Number(key)"
            :label="value"
          />
        </a-select>
      </a-form-item>
      <a-form-item field="scoringStrategy" label="评分策略" required>
        <a-select
          v-model="form.scoringStrategy"
          :style="{ width: '320px' }"
          placeholder="请选择评分策略"
        >
          <a-option
            v-for="(value, key) of APP_SCORING_STRATEGY_MAP"
            :value="Number(key)"
            :label="value"
          />
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px" :loading="loading">
          提交
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from "vue";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import {
  addAppUsingPost,
  editAppUsingPost,
  getAppVoByIdUsingGet,
} from "@/api/appController";
import { APP_SCORING_STRATEGY_MAP, APP_TYPE_MAP } from "@/constant/app";

interface Props {
  id?: string; // 改为可选，支持创建和编辑两种模式
}

const props = withDefaults(defineProps<Props>(), {
  id: "",
});

const router = useRouter();
const loading = ref(false);

// 表单数据
const form = ref<API.AppAddRequest>({
  appDesc: "",
  appIcon: "",
  appName: "",
  appType: 0,
  scoringStrategy: 0,
});

const oldApp = ref<API.AppVO>();

/**
 * 加载数据（编辑模式）
 */
const loadData = async () => {
  if (!props.id) {
    return;
  }
  
  try {
    const res = await getAppVoByIdUsingGet({
      id: props.id as any,
    });
    
    console.log("获取应用详情响应:", res);
    
    // 兼容多种返回格式
    const isSuccess = res.data?.code === 20000 || res.code === 20000;
    const responseData = res.data?.data || res.data;
    const errorMsg = res.data?.message || res.message;
    
    if (isSuccess && responseData) {
      oldApp.value = responseData;
      // 字段映射：兼容不同的字段名
      form.value = {
        appDesc: responseData.appDesc || responseData.appDescription || "",
        appIcon: responseData.appIcon || "",
        appName: responseData.appName || "",
        appType: responseData.appType ?? 0,
        scoringStrategy: responseData.scoringStrategy ?? 0,
      };
    } else {
      message.error("获取数据失败，" + (errorMsg || "请重试"));
    }
  } catch (error: any) {
    console.error("加载数据失败:", error);
    
    // 详细的错误处理
    if (error.response?.status === 404) {
      message.error(`接口不存在，请检查后端服务是否启动: ${error.config?.url}`);
    } else if (error.response?.status === 500) {
      message.error("服务器内部错误");
    } else if (!error.response) {
      message.error("网络错误，请确认后端服务是否运行");
    } else {
      message.error("加载数据失败，" + (error.message || "请重试"));
    }
  }
};

/**
 * 表单验证
 */
const validateForm = (): boolean => {
  if (!form.value.appName || !form.value.appName.trim()) {
    message.warning("请输入应用名称");
    return false;
  }
  if (!form.value.appDesc || !form.value.appDesc.trim()) {
    message.warning("请输入应用描述");
    return false;
  }
  if (form.value.appType === undefined || form.value.appType === null || form.value.appType === 0) {
    message.warning("请选择应用类型");
    return false;
  }
  if (form.value.scoringStrategy === undefined || form.value.scoringStrategy === null || form.value.scoringStrategy === 0) {
    message.warning("请选择评分策略");
    return false;
  }
  return true;
};

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 表单验证
  if (!validateForm()) {
    return;
  }
  
  loading.value = true;
  try {
    let res: any;
    let isEdit = !!props.id;
    
    // 清理表单数据（去除首尾空格）
    const submitData = {
      ...form.value,
      appName: form.value.appName.trim(),
      appDesc: form.value.appDesc.trim(),
      appIcon: form.value.appIcon?.trim() || "",
    };
    
    if (isEdit) {
      // 编辑模式
      res = await editAppUsingPost({
        id: props.id as any,
        ...submitData,
      });
      console.log("编辑应用响应:", res);
    } else {
      // 创建模式
      res = await addAppUsingPost(submitData);
      console.log("创建应用响应:", res);
    }
    
    // 判断响应成功（兼容多种返回格式）
    const isSuccess = res.data?.code === 20000 || res.code === 20000;
    const responseData = res.data?.data || res.data;
    const messageText = res.data?.message || res.message;
    
    if (isSuccess) {
      message.success(isEdit ? "修改成功，即将跳转到应用详情页" : "创建成功，即将跳转到应用详情页");
      setTimeout(() => {
        const detailId = props.id || responseData;
        router.push(`/app/detail/${detailId}`);
      }, 2000);
    } else {
      message.error("操作失败，" + (messageText || "未知错误"));
    }
  } catch (error: any) {
    console.error("提交失败:", error);
    
    // 详细的错误处理
    if (error.response?.status === 404) {
      message.error(`接口不存在，请检查后端服务是否启动: ${error.config?.url}`);
    } else if (error.response?.status === 500) {
      message.error("服务器内部错误，请检查后端日志");
    } else if (!error.response) {
      message.error("网络错误，请确认后端服务是否运行在 http://localhost:8081");
    } else {
      message.error(error.message || "操作失败，请重试");
    }
  } finally {
    loading.value = false;
  }
};

// 获取旧数据（编辑模式）
watchEffect(() => {
  loadData();
});
</script>

<style scoped>
#addAppPage {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
}

:deep(.arco-form-item) {
  margin-bottom: 24px;
}

:deep(.arco-btn-primary) {
  background: #165dff;
  border-radius: 6px;
}
</style>