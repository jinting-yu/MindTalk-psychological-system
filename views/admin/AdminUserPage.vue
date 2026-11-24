<template>
  <div class="admin-user-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <a-button type="primary" @click="handleAdd">
        <template #icon><icon-plus /></template>
        新增用户
      </a-button>
    </div>

    <!-- 搜索栏 -->
    <a-form
      :model="formSearchParams"
      :style="{ marginBottom: '20px' }"
      layout="inline"
      @submit="doSearch"
    >
      <a-form-item field="username" label="账号">
        <a-input
          allow-clear
          v-model="formSearchParams.username"
          placeholder="请输入账号"
          :style="{ width: '200px' }"
          @press-enter="doSearch"
        />
      </a-form-item>
      <a-form-item field="realName" label="姓名">
        <a-input
          allow-clear
          v-model="formSearchParams.realName"
          placeholder="请输入姓名"
          :style="{ width: '200px' }"
          @press-enter="doSearch"
        />
      </a-form-item>
      <a-form-item field="role" label="角色">
        <a-select
          allow-clear
          v-model="formSearchParams.role"
          placeholder="请选择角色"
          :style="{ width: '150px' }"
        >
          <a-option value="ADMIN">管理员</a-option>
          <a-option value="TEACHER">咨询师</a-option>
          <a-option value="USER">普通用户</a-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100px">
          <template #icon><icon-search /></template>
          搜索
        </a-button>
        <a-button style="margin-left: 10px" @click="resetSearch">
          重置
        </a-button>
      </a-form-item>
    </a-form>

    <!-- 数据表格 -->
    <a-table
      :columns="columns"
      :data="dataList"
      :loading="loading"
      :pagination="{
        current: searchParams.current,
        pageSize: searchParams.pageSize,
        total: total,
        showTotal: true,
        showJumper: true,
      }"
      @page-change="onPageChange"
      row-key="id"
      bordered
      stripe
    >
      <template #avatar="{ record }">
        <a-avatar :size="32">
          {{ getUserInitial(record) }}
        </a-avatar>
      </template>

      <template #role="{ record }">
        <a-tag :color="getRoleColor(record.role)" size="small">
          {{ getRoleName(record.role) }}
        </a-tag>
      </template>

      <template #createTime="{ record }">
        {{ formatDateTime(record.createTime) }}
      </template>

      <template #optional="{ record }">
        <a-space>
          <a-button type="text" size="small" @click="handleEdit(record)">
            <template #icon><icon-edit /></template>
            编辑
          </a-button>
          <a-popconfirm content="确定要删除该用户吗？" type="warning" @ok="doDelete(record)">
            <a-button type="text" size="small" status="danger">
              <template #icon><icon-delete /></template>
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @cancel="handleCancel"
      @before-ok="handleSubmit"
      :ok-loading="submitLoading"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="账号" required>
          <a-input
            v-model="formData.username"
            placeholder="请输入账号"
            :disabled="!!editingUser"
          />
        </a-form-item>
        <a-form-item label="姓名" required>
          <a-input v-model="formData.realName" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item v-if="!editingUser" label="密码" required>
          <a-input-password
            v-model="formData.password"
            placeholder="请输入密码"
          />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model="formData.role" placeholder="请选择角色">
            <a-option value="USER">普通用户</a-option>
            <a-option value="TEACHER">咨询师</a-option>
            <a-option value="ADMIN">管理员</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="手机号">
          <a-input
            v-model="formData.phone"
            placeholder="请输入手机号"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from "vue";
import {
  listUserByPageUsingPost,
  addUserUsingPost,
  updateUserUsingPost,
  deleteUserUsingPost,
} from "@/api/userController";
import { Message } from "@arco-design/web-vue";
import dayjs from "dayjs";
import {
  IconPlus,
  IconSearch,
  IconEdit,
  IconDelete,
} from "@arco-design/web-vue/es/icon";

// 搜索表单
const formSearchParams = ref({
  username: "",
  realName: "",
  role: "",
});

// 初始化搜索条件
const initSearchParams = {
  current: 1,
  pageSize: 10,
};

const searchParams = ref<any>({
  ...initSearchParams,
});

const dataList = ref<any[]>([]);
const total = ref<number>(0);
const loading = ref(false);

// 弹窗相关
const modalVisible = ref(false);
const modalTitle = ref("新增用户");
const submitLoading = ref(false);
const editingUser = ref<any | null>(null);

// 表单数据 - 匹配数据库字段
const formData = ref({
  username: "",
  realName: "",
  password: "",
  role: "USER",
  phone: "",
});

/**
 * 格式化日期时间
 */
const formatDateTime = (dateStr: string | undefined) => {
  if (!dateStr) return "-";
  return dayjs(dateStr).format("YYYY-MM-DD HH:mm:ss");
};

/**
 * 获取用户头像初始字母
 */
const getUserInitial = (record: any) => {
  return (record.realName || record.username || "U").charAt(0);
};

/**
 * 加载数据
 */
const loadData = async () => {
  loading.value = true;
  try {
    const res = await listUserByPageUsingPost(searchParams.value);
    console.log("用户管理接口返回:", res);
    
    let responseCode = null;
    let responseData = null;
    
    if (res && res.data) {
      responseCode = res.data.code;
      responseData = res.data.data;
    } else if (res && res.code !== undefined) {
      responseCode = res.code;
      responseData = res.data;
    }
    
    if (responseCode === 200 || responseCode === 20000) {
      let users = [];
      let totalCount = 0;
      
      if (Array.isArray(responseData)) {
        users = responseData;
        totalCount = users.length;
      } else if (responseData && responseData.records) {
        users = responseData.records;
        totalCount = responseData.total || 0;
      }
      
      // 映射到前端使用的字段
      dataList.value = users.map((user: any) => ({
        id: user.id,
        username: user.username,
        realName: user.realName,
        role: user.role,
        phone: user.phone,
        createTime: user.createTime,
      }));
      total.value = totalCount;
    } else {
      Message.error(res?.data?.msg || "获取数据失败");
      dataList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("加载数据失败:", error);
    Message.error("加载数据失败，请检查网络连接");
    dataList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

/**
 * 执行搜索
 */
const doSearch = () => {
  searchParams.value = {
    ...initSearchParams,
    username: formSearchParams.value.username,
    realName: formSearchParams.value.realName,
    role: formSearchParams.value.role,
  };
};

/**
 * 重置搜索
 */
const resetSearch = () => {
  formSearchParams.value = {
    username: "",
    realName: "",
    role: "",
  };
  searchParams.value = {
    ...initSearchParams,
  };
};

/**
 * 当分页变化时
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

/**
 * 新增用户
 */
const handleAdd = () => {
  editingUser.value = null;
  modalTitle.value = "新增用户";
  formData.value = {
    username: "",
    realName: "",
    password: "",
    role: "USER",
    phone: "",
  };
  modalVisible.value = true;
};

/**
 * 编辑用户
 */
const handleEdit = (record: any) => {
  editingUser.value = record;
  modalTitle.value = "编辑用户";
  formData.value = {
    username: record.username || "",
    realName: record.realName || "",
    password: "",
    role: record.role || "USER",
    phone: record.phone || "",
  };
  modalVisible.value = true;
};

/**
 * 提交表单
 */
const handleSubmit = async (done: (closed: boolean) => void) => {
  if (!formData.value.username) {
    Message.warning("请输入账号");
    done(false);
    return;
  }
  if (!formData.value.realName) {
    Message.warning("请输入姓名");
    done(false);
    return;
  }
  if (!editingUser.value && !formData.value.password) {
    Message.warning("请输入密码");
    done(false);
    return;
  }

  submitLoading.value = true;
  try {
    let res;
    if (editingUser.value) {
      // 编辑用户
      res = await updateUserUsingPost({
        id: editingUser.value.id,
        username: formData.value.username,
        realName: formData.value.realName,
        role: formData.value.role,
        phone: formData.value.phone,
      });
    } else {
      // 新增用户 - 使用数据库字段名
      res = await addUserUsingPost({
        username: formData.value.username,
        realName: formData.value.realName,
        password: formData.value.password,
        role: formData.value.role,
        phone: formData.value.phone,
      });
    }

    const responseCode = res?.data?.code || res?.code;
    const responseMsg = res?.data?.msg || res?.data?.message || res?.msg;
    
    if (responseCode === 200 || responseCode === 20000) {
      Message.success(editingUser.value ? "更新成功" : "添加成功");
      modalVisible.value = false;
      loadData();
      done(true);
    } else {
      Message.error(responseMsg || "操作失败");
      done(false);
    }
  } catch (error) {
    console.error("操作失败:", error);
    Message.error("操作失败，请稍后重试");
    done(false);
  } finally {
    submitLoading.value = false;
  }
};

/**
 * 取消弹窗
 */
const handleCancel = () => {
  modalVisible.value = false;
  editingUser.value = null;
};

/**
 * 删除用户
 */
const doDelete = async (record: any) => {
  if (!record.id) return;

  try {
    const res = await deleteUserUsingPost({
      id: String(record.id),
    });
    
    const responseCode = res?.data?.code || res?.code;
    const responseMsg = res?.data?.msg || res?.data?.message;
    
    if (responseCode === 200 || responseCode === 20000) {
      Message.success("删除成功");
      loadData();
    } else {
      Message.error("删除失败，" + (responseMsg || ""));
    }
  } catch (error) {
    console.error("删除失败:", error);
    Message.error("删除失败，请稍后重试");
  }
};

/**
 * 获取角色颜色
 */
const getRoleColor = (role: string) => {
  const colors: Record<string, string> = {
    ADMIN: "red",
    TEACHER: "blue",
    COUNSELOR: "blue",
    USER: "green",
  };
  return colors[role] || "gray";
};

/**
 * 获取角色名称
 */
const getRoleName = (role: string) => {
  const names: Record<string, string> = {
    ADMIN: "管理员",
    TEACHER: "咨询师",
    COUNSELOR: "咨询师",
    USER: "普通用户",
  };
  return names[role] || role;
};

/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});

// 表格列配置
const columns = [
  { title: "ID", dataIndex: "id", width: 80 },
  { title: "账号", dataIndex: "username", width: 150 },
  { title: "姓名", dataIndex: "realName", width: 150 },
  { title: "头像", slotName: "avatar", width: 80 },
  { title: "角色", slotName: "role", width: 100 },
  { title: "手机号", dataIndex: "phone", width: 150 },
  { title: "创建时间", slotName: "createTime", width: 180 },
  { title: "操作", slotName: "optional", width: 150, fixed: "right" },
];
</script>

<style scoped>
.admin-user-page {
  padding: 20px;
  background: #fff;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}
</style>