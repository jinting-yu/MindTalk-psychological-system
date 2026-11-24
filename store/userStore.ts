import { defineStore } from "pinia";
import { ref } from "vue";
import { getLoginUserUsingGet } from "@/api/userController";
import ACCESS_ENUM from "@/access/accessEnum";

/**
 * 登录用户信息全局状态
 */
export const useLoginUserStore = defineStore("loginUser", () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: "未登录",
    userRole: ACCESS_ENUM.NOT_LOGIN,
  });

  // 设置登录用户
  function setLoginUser(newLoginUser: API.LoginUserVO) {
    loginUser.value = newLoginUser;
    // 存储到 localStorage
    localStorage.setItem("loginUser", JSON.stringify(newLoginUser));
  }

  // 清除登录用户
  function clearLoginUser() {
    loginUser.value = {
      userName: "未登录",
      userRole: ACCESS_ENUM.NOT_LOGIN,
    };
    localStorage.removeItem("loginUser");
  }

  // 从 localStorage 恢复用户信息
  function restoreLoginUser() {
    const stored = localStorage.getItem("loginUser");
    if (stored) {
      try {
        const user = JSON.parse(stored);
        loginUser.value = user;
        return true;
      } catch (e) {
        console.error("恢复用户信息失败", e);
      }
    }
    return false;
  }

  // 获取当前登录用户
  async function fetchLoginUser() {
    try {
      const res = await getLoginUserUsingGet();
      if (res.data.code === 20000 && res.data.data) {
        const userData = res.data.data;
        if (!userData.userRole) {
          userData.userRole = ACCESS_ENUM.USER;
        }
        setLoginUser(userData);
        return userData;
      } else {
        clearLoginUser();
        return null;
      }
    } catch (error) {
      console.error("获取用户信息失败", error);
      clearLoginUser();
      return null;
    }
  }

  // 检查是否是管理员
  function isAdmin() {
    return loginUser.value.userRole === ACCESS_ENUM.ADMIN;
  }

  // 检查是否已登录
  function isLogin() {
    return loginUser.value.userRole !== ACCESS_ENUM.NOT_LOGIN && 
           loginUser.value.userRole !== undefined &&
           loginUser.value.id !== undefined;
  }

  return { 
    loginUser, 
    setLoginUser, 
    clearLoginUser,
    restoreLoginUser,
    fetchLoginUser,
    isAdmin,
    isLogin
  };
});