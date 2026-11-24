<template>
  <div class="news-detail-page">
    <div class="detail-container">
      <!-- 返回按钮 -->
      <div class="back-button">
        <a-button type="text" @click="goBack">
          <icon-left /> 返回列表
        </a-button>
      </div>

      <!-- 文章内容 -->
      <div class="article-content" v-loading="loading">
        <div class="article-header">
          <div class="article-category">
            <a-tag :color="getCategoryColor(newsDetail.category)">
              {{ getCategoryName(newsDetail.category) }}
            </a-tag>
          </div>
          <h1 class="article-title">{{ newsDetail.title }}</h1>
          <div class="article-meta">
            <span><icon-calendar /> {{ newsDetail.createTime }}</span>
            <span><icon-eye /> {{ newsDetail.viewCount }}阅读</span>
            <span><icon-message /> {{ newsDetail.commentCount }}评论</span>
            <span><icon-heart /> {{ newsDetail.likeCount }}点赞</span>
          </div>
        </div>

        <div class="article-body" v-html="newsDetail.content">
        </div>

        <!-- 文章标签 -->
        <div class="article-tags" v-if="newsDetail.tags && newsDetail.tags.length">
          <span class="tags-label">相关标签：</span>
          <a-tag v-for="tag in newsDetail.tags" :key="tag" size="small">
            {{ tag }}
          </a-tag>
        </div>

        <!-- 互动区 -->
        <div class="article-interaction">
          <div class="interaction-buttons">
            <a-button :type="isLiked ? 'primary' : 'outline'" @click="handleLike">
              <icon-heart :style="{ color: isLiked ? '#f53f3f' : '' }" />
              {{ newsDetail.likeCount || 0 }}
            </a-button>
            <a-button type="outline" @click="handleCollect">
              <icon-star :style="{ color: isCollected ? '#ffb400' : '' }" />
              {{ isCollected ? '已收藏' : '收藏' }}
            </a-button>
            <a-button type="outline" @click="handleShare">
              <icon-share-alt />
              分享
            </a-button>
          </div>
        </div>

        <!-- 评论区域 -->
        <div class="comment-section">
          <div class="comment-header">
            <h3>评论 ({{ newsDetail.commentCount || 0 }})</h3>
          </div>
          
          <!-- 发表评论 -->
          <div class="comment-input">
            <a-textarea 
              v-model="commentContent" 
              placeholder="写下你的想法..." 
              :rows="3"
              :max-length="200"
            />
            <div class="comment-actions">
              <a-button type="primary" @click="submitComment" :loading="commentLoading">
                发表评论
              </a-button>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-avatar">
                <a-avatar :size="40">{{ comment.userName.charAt(0) }}</a-avatar>
              </div>
              <div class="comment-content">
                <div class="comment-user">{{ comment.userName }}</div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-time">{{ comment.createTime }}</div>
                <div class="comment-reply">
                  <a-link @click="replyComment(comment.id)">回复</a-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐文章侧边栏 -->
      <div class="recommend-sidebar">
        <div class="recommend-card">
          <h3>推荐文章</h3>
          <div class="recommend-list">
            <div 
              v-for="article in recommendArticles" 
              :key="article.id" 
              class="recommend-item"
              @click="goToDetail(article.id)"
            >
              <div class="recommend-title">{{ article.title }}</div>
              <div class="recommend-meta">{{ article.createTime }}</div>
            </div>
          </div>
        </div>
        
        <div class="hot-tags">
          <h3>热门标签</h3>
          <div class="tags-cloud">
            <a-tag v-for="tag in hotTags" :key="tag" @click="searchByTag(tag)">
              {{ tag }}
            </a-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const commentLoading = ref(false)
const commentContent = ref('')
const isLiked = ref(false)
const isCollected = ref(false)

const newsDetail = ref({
  id: 0,
  title: '',
  category: '',
  createTime: '',
  viewCount: 0,
  commentCount: 0,
  likeCount: 0,
  content: '',
  tags: [] as string[]
})

const comments = ref<any[]>([])
const recommendArticles = ref<any[]>([])
const hotTags = ref(['心理健康', '情绪管理', '焦虑缓解', 'MBTI', '人际关系', '压力调节', '正念冥想', '职场心理'])

// 所有文章的完整内容
const articlesData: Record<number, any> = {
  1: {
    id: 1,
    title: '如何应对职场焦虑？这5个方法帮你找回平静',
    category: 'career',
    createTime: '2025-03-25',
    viewCount: 1250,
    commentCount: 32,
    likeCount: 89,
    tags: ['职场心理', '焦虑缓解', '压力管理'],
    content: `
      <h2>职场焦虑已成为现代人的普遍困扰</h2>
      <p>在快节奏的现代职场中，焦虑情绪已经成为许多人的"常客"。工作压力、人际关系、职业发展...这些因素都可能导致焦虑感的产生。但请记住，适度的焦虑是正常的，关键在于如何科学应对。</p>
      
      <h3>方法一：正念呼吸法</h3>
      <p>当感到焦虑时，尝试进行5分钟的正念呼吸练习。找个安静的地方坐下，闭上眼睛，专注于呼吸的节奏。吸气4秒，屏息4秒，呼气6秒。这种方法能迅速激活副交感神经系统，帮助你平静下来。</p>
      
      <h3>方法二：认知重构技术</h3>
      <p>焦虑往往源于对未来的灾难性想象。试着问自己："最坏的情况是什么？发生的概率有多大？即使发生了，我能应对吗？" 通过理性的思考，你会发现很多担忧其实并没有想象中那么可怕。</p>
      
      <h3>方法三：建立工作边界</h3>
      <p>学会对不合理的要求说"不"。明确工作时间，下班后尽量不处理工作事务。给自己留出充足的休息和放松时间，这是预防职业倦怠的关键。</p>
      
      <h3>方法四：运动释放压力</h3>
      <p>规律的运动是最好的"抗焦虑药"。每周保持3-4次有氧运动，每次30分钟以上。运动能促进内啡肽分泌，提升情绪，缓解压力。</p>
      
      <h3>方法五：寻求专业帮助</h3>
      <p>如果焦虑严重影响了工作和生活，不要犹豫寻求心理咨询师的专业帮助。寻求帮助是勇敢的表现，不是软弱。</p>
      
      <blockquote>
        <p>"焦虑不会消除明天的悲伤，它只会消耗今天的能量。" —— 卡夫卡</p>
      </blockquote>
    `
  },
  2: {
    id: 2,
    title: 'MBTI性格测试结果解读：你属于哪种类型？',
    category: 'growth',
    createTime: '2025-03-24',
    viewCount: 3420,
    commentCount: 128,
    likeCount: 256,
    tags: ['MBTI', '性格测试', '自我认知'],
    content: `
      <h2>MBTI是什么？</h2>
      <p>MBTI（Myers-Briggs Type Indicator）是世界上最流行的性格测试工具之一，通过四个维度将人的性格分为16种类型。</p>
      
      <h2>MBTI的四个维度</h2>
      <h3>1. 能量来源：外向(E) vs 内向(I)</h3>
      <p><strong>外向型(E)：</strong>从外部世界获得能量，喜欢社交。</p>
      <p><strong>内向型(I)：</strong>从内心世界获得能量，喜欢独处。</p>
      
      <h3>2. 信息收集：感觉(S) vs 直觉(N)</h3>
      <p><strong>感觉型(S)：</strong>关注具体事实和细节。</p>
      <p><strong>直觉型(N)：</strong>关注模式和可能性。</p>
      
      <h3>3. 决策方式：思考(T) vs 情感(F)</h3>
      <p><strong>思考型(T)：</strong>以逻辑分析做决定。</p>
      <p><strong>情感型(F)：</strong>以个人价值做决定。</p>
      
      <h3>4. 生活方式：判断(J) vs 感知(P)</h3>
      <p><strong>判断型(J)：</strong>喜欢有计划的生活。</p>
      <p><strong>感知型(P)：</strong>喜欢灵活开放的生活。</p>
      
      <h2>16种性格类型</h2>
      <p><strong>INTJ（建筑师）：</strong>富有想象力和战略性的思想家。</p>
      <p><strong>INTP（逻辑学家）：</strong>具有创造力的发明家。</p>
      <p><strong>INFJ（提倡者）：</strong>富有想象力和道德感的思想者。</p>
      <p><strong>INFP（调停者）：</strong>富有诗意的理想主义者。</p>
      
      <blockquote>
        <p>"认识你自己" —— 苏格拉底</p>
      </blockquote>
    `
  },
  3: {
    id: 3,
    title: '情绪管理入门：识别并接纳你的情绪',
    category: 'emotion',
    createTime: '2025-03-23',
    viewCount: 2150,
    commentCount: 67,
    likeCount: 123,
    tags: ['情绪管理', '心理健康', '自我调节'],
    content: `
      <h2>情绪是什么？</h2>
      <p>情绪是我们对内外刺激的反应，是连接身体和心灵的桥梁。每一种情绪都有其存在的意义和价值。</p>
      
      <h2>认识基本情绪</h2>
      <h3>1. 快乐</h3>
      <p>快乐让我们感受到生活的美好，增强人际连接。</p>
      <h3>2. 悲伤</h3>
      <p>悲伤让我们能够面对失去，促进反思和成长。</p>
      <h3>3. 愤怒</h3>
      <p>愤怒提醒我们边界被侵犯，给予我们保护自己的力量。</p>
      <h3>4. 恐惧</h3>
      <p>恐惧保护我们免受伤害，让我们在危险面前保持警觉。</p>
      
      <h2>情绪管理的四个步骤</h2>
      <h3>第一步：识别情绪</h3>
      <p>学会准确识别自己的情绪。</p>
      <h3>第二步：接纳情绪</h3>
      <p>不要评判或压抑情绪。</p>
      <h3>第三步：理解情绪</h3>
      <p>探索情绪背后的信息。</p>
      <h3>第四步：调节情绪</h3>
      <p>选择合适的表达和调节方式。</p>
      
      <blockquote>
        <p>"你无法阻止海浪，但可以学会冲浪。" —— 乔恩·卡巴金</p>
      </blockquote>
    `
  },
  4: {
    id: 4,
    title: '建立健康人际关系的5个关键要素',
    category: 'relationship',
    createTime: '2025-03-22',
    viewCount: 980,
    commentCount: 41,
    likeCount: 67,
    tags: ['人际关系', '沟通技巧', '社交'],
    content: `
      <h2>为什么人际关系如此重要？</h2>
      <p>哈佛大学长达75年的追踪研究发现，良好的人际关系是幸福最重要的预测因素。</p>
      
      <h2>健康人际关系的五大要素</h2>
      <h3>1. 真诚沟通</h3>
      <p>沟通是关系的基石，包括积极倾听、表达感受、非暴力沟通。</p>
      <h3>2. 相互尊重</h3>
      <p>接纳对方的不同观点和选择，尊重对方的边界。</p>
      <h3>3. 建立信任</h3>
      <p>言行一致，保持诚实，保守秘密，承担责任。</p>
      <h3>4. 情感支持</h3>
      <p>在对方困难时提供陪伴和理解，庆祝对方的成功。</p>
      <h3>5. 健康边界</h3>
      <p>知道自己的底线，学会说"不"，保持独立性。</p>
      
      <blockquote>
        <p>"最重要的不是你说了什么，而是你让对方感受到了什么。"</p>
      </blockquote>
    `
  },
  5: {
    id: 5,
    title: '大学生心理健康指南：如何度过迷茫期',
    category: 'mental',
    createTime: '2025-03-21',
    viewCount: 2100,
    commentCount: 89,
    likeCount: 156,
    tags: ['大学生', '心理健康', '迷茫期'],
    content: `
      <h2>大学生活的心理挑战</h2>
      <p>大学时期是人生的重要转折点，面临学业压力、人际关系、未来规划等多重挑战。许多大学生会经历迷茫期，这是正常的成长过程。</p>
      
      <h2>常见心理问题</h2>
      <h3>1. 学业压力</h3>
      <p>课程难度大、考试焦虑、专业选择困惑等问题。</p>
      <h3>2. 人际关系困扰</h3>
      <p>宿舍关系、同学交往、恋爱问题等。</p>
      <h3>3. 未来迷茫</h3>
      <p>就业压力、职业规划不清、自我价值怀疑。</p>
      
      <h2>应对建议</h2>
      <h3>建立支持系统</h3>
      <p>与家人保持联系，结交志同道合的朋友，寻求辅导员或心理咨询师的帮助。</p>
      <h3>制定合理目标</h3>
      <p>设定短期和长期目标，将大目标分解为小步骤。</p>
      <h3>培养兴趣爱好</h3>
      <p>参加社团活动，发展自己的兴趣，丰富大学生活。</p>
      <h3>学会自我关怀</h3>
      <p>接纳自己的不完美，给自己休息和放松的时间。</p>
      
      <blockquote>
        <p>"大学不是终点，而是新起点。迷茫不可怕，重要的是在迷茫中找到自己的方向。"</p>
      </blockquote>
    `
  },
  6: {
    id: 6,
    title: '亲子沟通的艺术：如何与孩子有效对话',
    category: 'parenting',
    createTime: '2025-03-20',
    viewCount: 1560,
    commentCount: 54,
    likeCount: 98,
    tags: ['亲子关系', '沟通', '家庭教育'],
    content: `
      <h2>为什么亲子沟通很重要？</h2>
      <p>良好的亲子沟通是家庭和谐的基石，影响孩子的心理健康、性格发展和未来人际关系。</p>
      
      <h2>有效沟通的技巧</h2>
      <h3>1. 积极倾听</h3>
      <p>放下手机，看着孩子的眼睛，认真听他说完，不打断不评判。</p>
      <h3>2. 表达感受而非评判</h3>
      <p>用"我感到..."代替"你总是..."，表达自己的感受而不是指责孩子。</p>
      <h3>3. 尊重孩子的情绪</h3>
      <p>允许孩子表达负面情绪，帮助他识别和命名情绪。</p>
      <h3>4. 共同解决问题</h3>
      <p>遇到问题时，与孩子一起寻找解决方案，而不是单方面命令。</p>
      
      <h2>常见沟通误区</h2>
      <ul>
        <li>说教太多，倾听太少</li>
        <li>批评太多，鼓励太少</li>
        <li>命令太多，协商太少</li>
        <li>比较太多，接纳太少</li>
      </ul>
      
      <blockquote>
        <p>"孩子不会听你说什么，但会看你做什么。"</p>
      </blockquote>
    `
  },
  7: {
    id: 7,
    title: '正念冥想：每天10分钟，改变你的生活',
    category: 'mental',
    createTime: '2025-03-19',
    viewCount: 1890,
    commentCount: 76,
    likeCount: 143,
    tags: ['正念', '冥想', '减压'],
    content: `
      <h2>什么是正念冥想？</h2>
      <p>正念冥想是一种训练注意力的方法，要求我们专注于当下，不带评判地观察自己的思想、情绪和身体感受。</p>
      
      <h2>正念冥想的益处</h2>
      <ul>
        <li><strong>减轻压力：</strong>降低皮质醇水平，缓解焦虑</li>
        <li><strong>提升专注力：</strong>改善注意力不集中问题</li>
        <li><strong>改善情绪：</strong>减少抑郁情绪，提升幸福感</li>
        <li><strong>增强自我觉察：</strong>更了解自己的内心世界</li>
      </ul>
      
      <h2>初学者指南</h2>
      <h3>第一步：找到安静的地方</h3>
      <p>找一个不会被打扰的角落，可以坐在椅子上或垫子上。</p>
      <h3>第二步：设定时间</h3>
      <p>从5分钟开始，慢慢增加到10-20分钟。</p>
      <h3>第三步：关注呼吸</h3>
      <p>闭上眼睛，专注于呼吸的感觉。当注意力飘走时，温和地带回呼吸。</p>
      <h3>第四步：不评判</h3>
      <p>不要因为走神而自责，这是正常的练习过程。</p>
      
      <h2>每日练习建议</h2>
      <p>每天固定时间练习，比如早晨起床后或睡前。坚持21天，你会感受到变化。</p>
      
      <blockquote>
        <p>"你无法控制海浪，但可以学会冲浪。"</p>
      </blockquote>
    `
  },
  8: {
    id: 8,
    title: '职场新人如何快速适应工作环境',
    category: 'career',
    createTime: '2025-03-18',
    viewCount: 2340,
    commentCount: 112,
    likeCount: 187,
    tags: ['职场', '新人', '适应'],
    content: `
      <h2>初入职场的挑战</h2>
      <p>从校园到职场，环境、角色、人际关系都发生巨大变化。新人常面临：不了解公司文化、不知道如何沟通、担心犯错等问题。</p>
      
      <h2>快速适应的方法</h2>
      <h3>1. 主动学习</h3>
      <p>向前辈请教，学习公司规章制度、业务流程。不懂就问，不要害怕暴露不足。</p>
      <h3>2. 建立良好关系</h3>
      <p>主动与同事打招呼，参加团队活动，找到可以请教的前辈导师。</p>
      <h3>3. 管理期望</h3>
      <p>与上级沟通明确工作目标和期望，及时汇报进度和困难。</p>
      <h3>4. 接受犯错</h3>
      <p>新人犯错很正常，重要的是从错误中学习，不重复同样的问题。</p>
      
      <h2>避免的误区</h2>
      <ul>
        <li>急于表现，忽视学习</li>
        <li>不懂装懂，延误工作</li>
        <li>抱怨太多，行动太少</li>
        <li>孤立自己，不融入团队</li>
      </ul>
      
      <blockquote>
        <p>"职场不是学校，没有人会为你准备好一切，但每个人都曾是新人。"</p>
      </blockquote>
    `
  }
}

// 获取分类名称
const getCategoryName = (value: string) => {
  const categories: Record<string, string> = {
    mental: '心理健康',
    emotion: '情绪管理',
    relationship: '人际关系',
    career: '职场心理',
    parenting: '亲子教育',
    growth: '自我成长'
  }
  return categories[value] || value
}

// 获取分类颜色
const getCategoryColor = (value: string) => {
  const colorMap: Record<string, string> = {
    mental: 'blue',
    emotion: 'orange',
    relationship: 'green',
    career: 'purple',
    parenting: 'red',
    growth: 'cyan'
  }
  return colorMap[value] || 'gray'
}

// 加载资讯详情
const loadNewsDetail = async () => {
  const newsId = Number(route.params.id)
  if (!newsId) return
  
  loading.value = true
  try {
    const article = articlesData[newsId]
    
    if (article) {
      newsDetail.value = article
      
      // 获取推荐文章（排除当前文章）
      recommendArticles.value = Object.values(articlesData)
        .filter((a: any) => a.id !== newsId)
        .slice(0, 3)
        .map((a: any) => ({
          id: a.id,
          title: a.title,
          createTime: a.createTime
        }))
      
      // 模拟评论数据
      comments.value = [
        {
          id: 1,
          userName: '心理爱好者',
          content: '这篇文章真的很实用，学到了很多！',
          createTime: '2025-03-25 14:30'
        },
        {
          id: 2,
          userName: '小明同学',
          content: '感谢分享，正在尝试文中的方法',
          createTime: '2025-03-25 16:20'
        }
      ]
    } else {
      Message.error('文章不存在')
      router.push('/news')
    }
  } catch (error) {
    Message.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

// 点赞
const handleLike = () => {
  isLiked.value = !isLiked.value
  if (isLiked.value) {
    newsDetail.value.likeCount++
    Message.success('感谢点赞')
  } else {
    newsDetail.value.likeCount--
  }
}

// 收藏
const handleCollect = () => {
  isCollected.value = !isCollected.value
  Message.success(isCollected.value ? '收藏成功' : '已取消收藏')
}

// 分享
const handleShare = () => {
  Message.info('分享功能开发中')
}

// 发表评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    Message.warning('请输入评论内容')
    return
  }
  
  commentLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    comments.value.unshift({
      id: Date.now(),
      userName: '当前用户',
      content: commentContent.value,
      createTime: new Date().toLocaleString()
    })
    newsDetail.value.commentCount++
    commentContent.value = ''
    Message.success('评论成功')
  } catch (error) {
    Message.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

// 回复评论
const replyComment = (commentId: number) => {
  Message.info('回复功能开发中')
}

// 跳转到详情
const goToDetail = (id: number) => {
  router.push(`/news/detail/${id}`)
}

// 按标签搜索
const searchByTag = (tag: string) => {
  router.push(`/news?tag=${tag}`)
}

// 返回列表
const goBack = () => {
  router.push('/news')
}

onMounted(() => {
  loadNewsDetail()
})
</script>

<style scoped>
/* 样式保持不变 */
.news-detail-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.detail-container {
  display: flex;
  gap: 32px;
  position: relative;
}

.article-content {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.back-button {
  margin-bottom: 20px;
}

.article-header {
  border-bottom: 1px solid #e5e6eb;
  padding-bottom: 24px;
  margin-bottom: 32px;
}

.article-category {
  margin-bottom: 16px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.3;
  margin-bottom: 20px;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  color: #86909c;
  font-size: 14px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.article-body {
  line-height: 1.8;
  color: #1e293b;
  font-size: 16px;
}

.article-body h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 32px 0 16px;
  color: #0f172a;
}

.article-body h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 24px 0 12px;
  color: #1e293b;
}

.article-body p {
  margin-bottom: 16px;
  line-height: 1.8;
}

.article-body ul {
  margin: 16px 0;
  padding-left: 24px;
}

.article-body li {
  margin: 8px 0;
  line-height: 1.6;
}

.article-body blockquote {
  margin: 24px 0;
  padding: 16px 24px;
  background: #f8fafc;
  border-left: 4px solid #3b82f6;
  font-style: italic;
  color: #475569;
}

.article-tags {
  margin: 32px 0 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e6eb;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.tags-label {
  color: #64748b;
  font-size: 14px;
}

.article-interaction {
  margin: 24px 0 32px;
  padding: 20px 0;
  border-top: 1px solid #e5e6eb;
  border-bottom: 1px solid #e5e6eb;
}

.interaction-buttons {
  display: flex;
  gap: 16px;
}

.comment-section {
  margin-top: 32px;
}

.comment-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.comment-input {
  margin-bottom: 32px;
}

.comment-actions {
  margin-top: 12px;
  text-align: right;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f2f5;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-user {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.comment-text {
  color: #475569;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-time {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.comment-reply {
  font-size: 12px;
}

.recommend-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.recommend-card, .hot-tags {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.recommend-card h3, .hot-tags h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1e293b;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-item {
  cursor: pointer;
  padding: 8px 0;
  transition: all 0.3s;
}

.recommend-item:hover {
  transform: translateX(4px);
}

.recommend-title {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 4px;
  line-height: 1.4;
}

.recommend-meta {
  font-size: 12px;
  color: #94a3b8;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tags-cloud :deep(.arco-tag) {
  cursor: pointer;
  transition: all 0.2s;
}

.tags-cloud :deep(.arco-tag):hover {
  background: #3b82f6;
  color: #fff;
}

@media (max-width: 768px) {
  .detail-container {
    flex-direction: column;
  }
  
  .article-content {
    padding: 20px;
  }
  
  .article-title {
    font-size: 24px;
  }
  
  .recommend-sidebar {
    width: 100%;
  }
}
</style>