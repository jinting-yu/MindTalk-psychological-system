//@ts-nocheck
import { RouteRecordRaw } from "vue-router";
import HomePage from "@/views/HomePage.vue";
import UserLayout from "@/layouts/UserLayout.vue";
//@ts-ignore
import ACCESS_ENUM from "@/access/accessEnum";
import NoAuthPage from "@/views/NoAuthPage.vue";
import UserLoginPage from "@/views/user/UserLoginPage.vue";
import UserRegisterPage from "@/views/user/UserRegisterPage.vue";
import AdminUserPage from "@/views/admin/AdminUserPage.vue";
import AdminAppPage from "@/views/admin/AdminAppPage.vue";
import AdminQuestionPage from "@/views/admin/AdminQuestionPage.vue";
import AdminScoringResultPage from "@/views/admin/AdminScoringResultPage.vue";
import AdminUserAnswerPage from "@/views/admin/AdminUserAnswerPage.vue";
import AppDetailPage from "@/views/app/AppDetailPage.vue";
import AddAppPage from "@/views/add/AddAppPage.vue";
import AddQuestionPage from "@/views/add/AddQuestionPage.vue";
import AddScoringResultPage from "@/views/add/AddScoringResultPage.vue";
import DoAnswerPage from "@/views/answer/DoAnswerPage.vue";
import AnswerResultPage from "@/views/answer/AnswerResultPage.vue";
import MyAnswerPage from "@/views/answer/MyAnswerPage.vue";
import AppStatisticPage from "@/views/statistic/AppStatisticPage.vue";

import BookingListPage from "@/views/booking/BookingListPage.vue";
import CreateBookingPage from "@/views/booking/CreateBookingPage.vue";
import MyBookingPage from "@/views/booking/MyBookingPage.vue";
// 资讯相关
import NewsPage from "@/views/news/NewsPage.vue";
// 测评列表页（新增）
import TestListPage from "@/views/test/TestListPage.vue";
import NewsDetailPage from "@/views/news/NewsDetailPage.vue";
import UserProfilePage from "@/views/user/UserProfilePage.vue";
import BookingDetailPage from "@/views/booking/BookingDetailPage.vue";
import TeacherLayout from "@/views/teacher/TeacherLayout.vue";
import TeacherDashboard from "@/views/teacher/TeacherDashboard.vue";
import TeacherAppointments from "@/views/teacher/TeacherAppointments.vue";
import TeacherStudents from "@/views/teacher/TeacherStudents.vue";
import TeacherTestRecords from "@/views/teacher/TeacherTestRecords.vue";
import TeacherStatistics from "@/views/teacher/TeacherStatistics.vue";
import BookingFeedbackPage from "@/views/booking/BookingFeedbackPage.vue";

// 导入管理员布局组件
import AdminLayout from "@/views/admin/AdminLayout.vue";
import Dashboard from "@/views/admin/Dashboard.vue";

import VideoListPage from '@/views/video/VideoListPage.vue'
import VideoDetailPage from '@/views/video/VideoDetailPage.vue'

/**
 * 路由信息
 */
export const routes: any[] = [
  {
    path: "/",
    name: "主页",
    component: HomePage,
  },
  // 个人中心路由
  {
    path: "/user/profile",
    name: "个人中心",
    component: () => import("@/views/user/UserProfilePage.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      title: "个人中心"
    },
  },
  // 个人资料编辑
  {
    path: "/user/profile/edit",
    name: "编辑资料",
    component: () => import("@/views/user/UserProfileEdit.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      title: "编辑资料",
      hideInMenu: true,
    },
  },
  // ==================== 心理测评核心模块 ====================
  {
    path: "/test/list",
    name: "心理测评",
    component: TestListPage,
    meta: {
      title: "心理测评",
    },
  },
  {
    path: "/news",
    name: "心理资讯",
    component: NewsPage,
    meta: {
      title: "心理资讯",
    },
  },
  {
    path: "/news/detail/:id",
    name: "资讯详情",
    component: NewsDetailPage,
    meta: {
      hideInMenu: true,
      title: "资讯详情",
    },
  },
  // 答题相关
  {
    path: "/answer/do/:appId",
    name: "开始测评",
    component: DoAnswerPage,
    props: true,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
      title: "开始测评",
    },
  },
  {
    path: "/answer/result/:id",
    name: "测评结果",
    component: AnswerResultPage,
    props: true,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
      title: "测评结果",
    },
  },
  {
    path: "/answer/my",
    name: "我的测评记录",
    component: MyAnswerPage,
    meta: {
      access: ACCESS_ENUM.USER,
      title: "我的测评记录",
    },
  },
  // ==================== 心理预约模块 ====================
  {
    path: "/booking/list",
    name: "心理预约",
    component: BookingListPage,
    meta: {
      title: "心理预约",
    },
  },
  {
    path: "/booking/create",
    name: "创建预约",
    component: CreateBookingPage,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
      title: "创建预约",
    },
  },
  {
    path: "/booking/my",
    name: "我的预约",
    component: MyBookingPage,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
      title: "我的预约",
    },
  },
  {
    path: "/booking/detail/:id",
    name: "预约详情",
    component: BookingDetailPage,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
      title: "预约详情"
    },
  },
  {
    path: "/booking/feedback/:id",
    name: "写反馈",
    component: BookingFeedbackPage,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
      title: "写反馈"
    },
  },

  // ==================== 紧急联系人模块 ====================
  {
    path: "/emergency-contact",
    name: "紧急联系人",
    component: () => import("@/views/EmergencyContact.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      title: "紧急联系人",
    },
  },

  // ==================== 应用管理模块 ====================
  {
    path: "/add/app",
    name: "创建应用",
    component: AddAppPage,
  },
  {
    path: "/add/app/:id",
    name: "修改应用",
    props: true,
    component: AddAppPage,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/add/question/:appId",
    name: "创建题目",
    component: AddQuestionPage,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/add/scoring_result/:appId",
    name: "创建评分",
    component: AddScoringResultPage,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/app/detail/:id",
    name: "应用详情页",
    props: true,
    component: AppDetailPage,
    meta: {
      hideInMenu: true,
    },
  },
  
  // ==================== 管理员工作台模块 ====================
  {
    path: "/admin",
    name: "管理员工作台",
    component: AdminLayout,
    redirect: "/admin/dashboard",
    meta: {
      access: ACCESS_ENUM.ADMIN,
      hideInMenu: true,
    },
    children: [
      {
        path: "dashboard",
        name: "数据看板",
        component: Dashboard,
        meta: { access: ACCESS_ENUM.ADMIN, title: "数据看板" }
      },
      {
        path: "user",
        name: "用户管理",
        component: AdminUserPage,
        meta: { access: ACCESS_ENUM.ADMIN, title: "用户管理" }
      },
      {
        path: "app",
        name: "应用管理",
        component: AdminAppPage,
        meta: { access: ACCESS_ENUM.ADMIN, title: "应用管理" }
      },
      {
        path: "question",
        name: "题目管理",
        component: AdminQuestionPage,
        meta: { access: ACCESS_ENUM.ADMIN, title: "题目管理" }
      },
      {
        path: "scoring_result",
        name: "评分管理",
        component: AdminScoringResultPage,
        meta: { access: ACCESS_ENUM.ADMIN, title: "评分管理" }
      },
      {
        path: "user_answer",
        name: "回答管理",
        component: AdminUserAnswerPage,
        meta: { access: ACCESS_ENUM.ADMIN, title: "回答管理" }
      },
      {
        path: "statistic",
        name: "应用统计",
        component: AppStatisticPage,
        meta: { access: ACCESS_ENUM.ADMIN, title: "应用统计" }
      },
      {
        path: "appointments",
        name: "预约管理",
        component: () => import("@/views/admin/AdminAppointments.vue"),
        meta: { access: ACCESS_ENUM.ADMIN, title: "预约管理" }
      },
      {
        path: "tests",
        name: "测评管理",
        component: () => import("@/views/admin/AdminTests.vue"),
        meta: { access: ACCESS_ENUM.ADMIN, title: "测评管理" }
      },
      {
        path: "teachers",
        name: "咨询师管理",
        component: () => import("@/views/admin/AdminTeachers.vue"),
        meta: { access: ACCESS_ENUM.ADMIN, title: "咨询师管理" }
      },
      {
        path: "news",
        name: "资讯管理",
        component: () => import("@/views/admin/AdminNews.vue"),
        meta: { access: ACCESS_ENUM.ADMIN, title: "资讯管理" }
      },
      {
        path: "statistics",
        name: "数据统计",
        component: () => import("@/views/admin/AdminStatistics.vue"),
        meta: { access: ACCESS_ENUM.ADMIN, title: "数据统计" }
      }
    ]
  },

  // 保留原来的管理员路由作为兼容
  {
    path: "/admin/user",
    redirect: "/admin/user",
    meta: { hideInMenu: true }
  },
  {
    path: "/admin/app",
    redirect: "/admin/app",
    meta: { hideInMenu: true }
  },
  {
    path: "/admin/question",
    redirect: "/admin/question",
    meta: { hideInMenu: true }
  },
  {
    path: "/admin/scoring_result",
    redirect: "/admin/scoring_result",
    meta: { hideInMenu: true }
  },
  {
    path: "/admin/user_answer",
    redirect: "/admin/user_answer",
    meta: { hideInMenu: true }
  },
  {
    path: "/app_statistic",
    redirect: "/admin/statistic",
    meta: { hideInMenu: true }
  },

  {
    path: "/noAuth",
    name: "无权限",
    component: NoAuthPage,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/hide",
    name: "隐藏页面",
    component: HomePage,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/user",
    name: "用户",
    component: UserLayout,
    children: [
      {
        path: "/user/login",
        name: "用户登录",
        component: UserLoginPage,
      },
      {
        path: "/user/register",
        name: "用户注册",
        component: UserRegisterPage,
      },
    ],
    meta: {
      hideInMenu: true,
    },
  },
  // ==================== 老师工作台模块 ====================
  {
    path: "/teacher",
    name: "TeacherLayout",
    component: TeacherLayout,
    meta: {
      access: ACCESS_ENUM.TEACHER,
      hideInMenu: true,
    },
    children: [
      {
        path: "/teacher/dashboard",
        name: "TeacherDashboard",
        component: TeacherDashboard,
        meta: { access: ACCESS_ENUM.TEACHER, name: "工作台首页" }
      },
      {
        path: "/teacher/appointments",
        name: "TeacherAppointments",
        component: TeacherAppointments,
        meta: { access: ACCESS_ENUM.TEACHER, name: "我的预约" }
      },
      {
        path: "/user/archive/s",
        name: "TeacherStudents",
        component: TeacherStudents,
        meta: { access: ACCESS_ENUM.TEACHER, name: "学生管理" }
      },
      {
        path: "/teacher/test-records",
        name: "TeacherTestRecords",
        component: TeacherTestRecords,
        meta: { access: ACCESS_ENUM.TEACHER, name: "测评记录" }
      },
      {
        path: "/teacher/test-detail/:id",
        name: "TestDetail",
        component: () => import("@/views/teacher/TestDetail.vue"),
        meta: { access: ACCESS_ENUM.TEACHER, name: "测评详情" }
      },
      {
        path: "/teacher/statistics",
        name: "TeacherStatistics",
        component: TeacherStatistics,
        meta: { access: ACCESS_ENUM.TEACHER, name: "数据统计" }
      },
      // 咨询对话路由（从第二个文件合并）
      {
        path: "/teacher/consultation/:id",
        name: "咨询对话",
        component: () => import("@/views/teacher/ConsultationPage.vue"),
        meta: { access: ACCESS_ENUM.TEACHER, name: "咨询对话" }
      },
      // 文章管理路由（从第二个文件合并）
      {
        path: "article/create",
        name: "ArticleCreate",
        component: () => import("@/views/teacher/ArticleCreate.vue"),
        meta: { access: ACCESS_ENUM.TEACHER, title: "发布文章" }
      },
      {
        path: "article/edit/:id",
        name: "ArticleEdit", 
        component: () => import("@/views/teacher/ArticleCreate.vue"),
        meta: { access: ACCESS_ENUM.TEACHER, title: "编辑文章" }
      },
      {
        path: "articles",
        name: "MyArticles",
        component: () => import("@/views/teacher/MyArticles.vue"),
        meta: { access: ACCESS_ENUM.TEACHER, title: "我的文章" }
      }
    ]
  },
  // 用户档案路由
  {
    path: "/user/archive/:id",
    name: "用户档案",
    component: () => import("@/views/user/UserArchivePage.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      title: "用户档案",
      hideInMenu: true
    }
  },
  // 心理视频路由
  {
    path: '/video',
    name: 'VideoList',
    component: () => import('@/views/video/VideoListPage.vue'),
    meta: { title: '心理视频', hideInMenu: true }
  },
  {
    path: '/video/detail/:id',
    name: 'VideoDetail',
    component: () => import('@/views/video/VideoDetailPage.vue'),
    meta: { title: '视频详情', hideInMenu: true }
  }
];

export default routes;