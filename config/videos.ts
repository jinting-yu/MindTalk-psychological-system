// config/videos.ts
import firstJpg from '@/assets/第一集.jpg'
import secondJpg from '@/assets/第二集.jpg'
import thirdJpg from '@/assets/第三季.jpg'
import fourthJpg from '@/assets/第四集.jpg'
import fifthJpg from '@/assets/第五集.jpg'
import sixthJpg from '@/assets/第六集.jpg'
import fanShengJpg from '@/assets/凡是发生.jpg'
import heHuaJpg from '@/assets/荷花.jpg'
import huanJieJpg from '@/assets/缓解压力.jpg'
import jiaoDianJpg from '@/assets/焦点.jpg'
import shiFangJpg from '@/assets/释放.jpg'
import weiHuJpg from '@/assets/维护心理.jpg'
import xiGuanJpg from '@/assets/习惯性焦虑.jpg'

export interface VideoItem {
  id: number
  title: string
  teacher: string
  description: string
  url: string
  cover: string
  duration: number // 秒
  viewCount: number
  createTime: string
  category: string
}

// 根据你 public/videos 目录下的视频文件配置
export const videoListData: VideoItem[] = [
  {
    id: 1,
    title: '大学生心理健康知识第一集',
    teacher: '心理专家',
    description: '大学生心理健康知识科普，帮助大学生了解心理健康的重要性，掌握基本的心理调适方法。本集主要介绍心理健康的基本概念和标准。',
    url: '/videos/大学生心理健康知识第一集.mp4',
    cover: firstJpg,  // 改为本地图片
    duration: 600,
    viewCount: 0,
    createTime: '2024-03-20',
    category: '心理健康'
  },
  {
    id: 2,
    title: '大学生心理健康知识第二集',
    teacher: '心理专家',
    description: '大学生心理健康知识科普第二集，深入讲解大学生常见的心理问题及应对策略。',
    url: '/videos/大学生心理健康知识第二集.mp4',
    cover: secondJpg,  // 改为本地图片
    duration: 580,
    viewCount: 0,
    createTime: '2024-03-21',
    category: '心理健康'
  },
  {
    id: 3,
    title: '大学生心理健康知识第三集',
    teacher: '心理专家',
    description: '大学生心理健康知识科普第三集，介绍情绪管理的方法和技巧。',
    url: '/videos/大学生心理健康知识第三集.mp4',
    cover: thirdJpg,  // 改为本地图片
    duration: 620,
    viewCount: 0,
    createTime: '2024-03-22',
    category: '心理健康'
  },
  {
    id: 4,
    title: '大学生心理健康知识第四集',
    teacher: '心理专家',
    description: '大学生心理健康知识科普第四集，探讨人际关系与沟通技巧。',
    url: '/videos/大学生心理健康知识第四集.mp4',
    cover: fourthJpg,  // 改为本地图片
    duration: 590,
    viewCount: 0,
    createTime: '2024-03-23',
    category: '心理健康'
  },
  {
    id: 5,
    title: '大学生心理健康知识第五集',
    teacher: '心理专家',
    description: '大学生心理健康知识科普第五集，讲解压力应对与自我调适。',
    url: '/videos/大学生心理健康知识第五集.mp4',
    cover: fifthJpg,  // 改为本地图片
    duration: 610,
    viewCount: 0,
    createTime: '2024-03-24',
    category: '心理健康'
  },
  {
    id: 6,
    title: '大学生心理健康知识第六集',
    teacher: '心理专家',
    description: '大学生心理健康知识科普第六集，总结与展望，培养积极心态。',
    url: '/videos/大学生心理健康知识第六集.mp4',
    cover: sixthJpg,  // 改为本地图片
    duration: 600,
    viewCount: 0,
    createTime: '2024-03-25',
    category: '心理健康'
  },
  {
    id: 7,
    title: '凡事发生皆有利于我',
    teacher: '心理导师',
    description: '积极心理学视角，教你如何从积极的角度看待生活中的每一件事。',
    url: '/videos/凡事发生皆有利于我.mp4',
    cover: fanShengJpg,  // 改为本地图片
    duration: 480,
    viewCount: 0,
    createTime: '2024-03-26',
    category: '积极心理'
  },
  {
    id: 8,
    title: '缓解压力的方法',
    teacher: '心理导师',
    description: '实用的压力缓解技巧，帮助你在忙碌的生活中找到内心的平静。',
    url: '/videos/缓解压力的方法.mp4',
    cover: huanJieJpg,  // 改为本地图片
    duration: 520,
    viewCount: 0,
    createTime: '2024-03-27',
    category: '压力管理'
  },
  {
    id: 9,
    title: '如何正确对待习惯性焦虑',
    teacher: '心理导师',
    description: '深入解析习惯性焦虑的成因，并提供有效的应对策略。',
    url: '/videos/如何正确对待习惯性焦虑.mp4',
    cover: xiGuanJpg,  // 改为本地图片
    duration: 550,
    viewCount: 0,
    createTime: '2024-03-28',
    category: '焦虑管理'
  },
  {
    id: 10,
    title: '维护心理健康状态的方法',
    teacher: '心理导师',
    description: '日常生活中维护心理健康的实用方法和小技巧。',
    url: '/videos/维护心理健康状态的方法.mp4',
    cover: weiHuJpg,  // 改为本地图片
    duration: 500,
    viewCount: 0,
    createTime: '2024-03-29',
    category: '心理健康'
  },
  {
    id: 11,
    title: '心理学中的荷花效应',
    teacher: '心理导师',
    description: '有趣的心理学效应解读，了解荷花效应及其在生活中的应用。',
    url: '/videos/心理学中的荷花效应.mp4',
    cover: heHuaJpg,  // 改为本地图片
    duration: 450,
    viewCount: 0,
    createTime: '2024-03-30',
    category: '心理学'
  },
  {
    id: 12,
    title: '心理学中的焦点效应',
    teacher: '心理导师',
    description: '焦点效应解析，了解为什么我们总觉得自己是人群的焦点。',
    url: '/videos/心理学中的焦点效应.mp4',
    cover: jiaoDianJpg,  // 改为本地图片
    duration: 460,
    viewCount: 0,
    createTime: '2024-03-31',
    category: '心理学'
  },
  {
    id: 13,
    title: '学会释放情绪',
    teacher: '心理导师',
    description: '健康的情绪释放方式，学会表达和处理自己的情绪。',
    url: '/videos/学会释放情绪.mp4',
    cover: shiFangJpg,  // 改为本地图片
    duration: 490,
    viewCount: 0,
    createTime: '2024-04-01',
    category: '情绪管理'
  }
]

// 获取视频详情
export const getVideoById = (id: number): VideoItem | undefined => {
  return videoListData.find(v => v.id === id)
}

// 增加播放量
export const incrementViewCount = (id: number) => {
  const video = videoListData.find(v => v.id === id)
  if (video) {
    video.viewCount++
    // 保存到 localStorage 持久化
    const views = JSON.parse(localStorage.getItem('video_views') || '{}')
    views[id] = video.viewCount
    localStorage.setItem('video_views', JSON.stringify(views))
  }
}

// 加载保存的播放量
export const loadViewCounts = () => {
  const views = JSON.parse(localStorage.getItem('video_views') || '{}')
  videoListData.forEach(video => {
    if (views[video.id]) {
      video.viewCount = views[video.id]
    }
  })
}