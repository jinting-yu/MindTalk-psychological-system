<template>
  <div>
    <!-- AI悬浮球 - 治愈系设计 -->
    <div 
      class="ai-float-button" 
      @click="toggleChatWindow"
      :class="{ 'expanded': isChatWindowOpen }"
    >
      <div class="ripple-container" v-if="!isChatWindowOpen">
        <div class="ripple"></div>
        <div class="ripple delay-1"></div>
        <div class="ripple delay-2"></div>
      </div>
      <div class="ai-icon">
        <!-- 树的图形 -->
        <svg v-if="!isChatWindowOpen" width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L3 9L12 16L21 9L12 2Z" fill="#5B8C6F" stroke="#4A735A" stroke-width="1.2"/>
          <path d="M12 16L12 22" stroke="#5B8C6F" stroke-width="1.5" stroke-linecap="round"/>
          <circle cx="12" cy="9" r="1.5" fill="#8BAB7A"/>
          <path d="M7 12L5 14" stroke="#5B8C6F" stroke-width="1.2" stroke-linecap="round"/>
          <path d="M17 12L19 14" stroke="#5B8C6F" stroke-width="1.2" stroke-linecap="round"/>
        </svg>
        <svg v-else width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 4L5 9.5L12 15L19 9.5L12 4Z" fill="#5B8C6F" stroke="#4A735A" stroke-width="1.2"/>
          <path d="M12 15L12 20" stroke="#5B8C6F" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M8.5 11L10.5 12.5" stroke="#8BAB7A" stroke-width="1" stroke-linecap="round"/>
          <path d="M15.5 11L13.5 12.5" stroke="#8BAB7A" stroke-width="1" stroke-linecap="round"/>
          <circle cx="12" cy="9" r="1" fill="#C4A265"/>
        </svg>
      </div>
      <div class="tooltip" v-if="!isChatWindowOpen">
        <span>🌳 来树下聊聊吧</span>
      </div>
    </div>

    <!-- AI聊天窗口 - 治愈系设计 -->
    <div class="ai-chat-window" v-if="isChatWindowOpen" @click.stop>
      <div class="window-blur-bg"></div>
      
      <!-- 头部 -->
      <div class="ai-chat-header">
        <div class="header-content">
          <div class="ai-avatar">
            <div class="avatar-pulse"></div>
            <!-- 树的图形 -->
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L4 8.5L12 15L20 8.5L12 2Z" fill="#5B8C6F" stroke="#4A735A" stroke-width="1"/>
              <path d="M12 15L12 21" stroke="#5B8C6F" stroke-width="1.5" stroke-linecap="round"/>
              <path d="M9 12L10.5 13" stroke="#8BAB7A" stroke-width="1" stroke-linecap="round"/>
              <path d="M15 12L13.5 13" stroke="#8BAB7A" stroke-width="1" stroke-linecap="round"/>
              <circle cx="12" cy="8.5" r="1" fill="#C4A265"/>
            </svg>
          </div>
          <div class="header-text">
            <h3>心灵树洞</h3>
            <p>🌿 大树会守护你的秘密</p>
          </div>
        </div>
        <button class="ai-chat-close" @click="closeChatWindow">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
      
      <!-- 消息区域 -->
      <div class="ai-chat-body" ref="chatBody">
        <div class="messages-container">
          <!-- 欢迎卡片 -->
          <div class="welcome-card" v-if="messages.length === 1">
            <div class="welcome-icon">🌳</div>
            <div class="welcome-text">
              <p>欢迎来到大树下</p>
              <small>把心事说给大树听，它会用年轮记住你的每一个故事</small>
            </div>
          </div>
          
          <!-- 消息列表 -->
          <div v-for="(msg, index) in messages" :key="index" 
               :class="['message-item', msg.type === 'user' ? 'user-message' : 'ai-message']">
            <div class="message-avatar" v-if="msg.type === 'ai'">
              🌳
            </div>
            <div class="message-bubble">
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.time) }}</div>
            </div>
            <div class="message-avatar" v-if="msg.type === 'user'">
              🍃
            </div>
          </div>
          
          <!-- 加载动画 -->
          <div v-if="isLoading" class="message-item ai-message">
            <div class="message-avatar">
              🌳
            </div>
            <div class="message-bubble">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="ai-chat-input">
        <div class="input-wrapper">
          <input 
            type="text" 
            v-model="inputMessage" 
            @keyup.enter="sendMessage" 
            :placeholder="inputPlaceholder"
            :disabled="isLoading"
            ref="messageInput"
          />
          <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="suggestions" v-if="showSuggestions">
          <button 
            v-for="suggestion in suggestions" 
            :key="suggestion"
            @click="quickSend(suggestion)"
            class="suggestion-chip"
          >
            {{ suggestion }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import myAxios from '@/request'

export default {
  name: 'AIFloat',
  data() {
    return {
      isChatWindowOpen: false,
      messages: [
        { 
          type: 'ai', 
          content: '🍃 树荫下很安静，欢迎你来～\n\n有什么想和我分享的吗？开心的、难过的，我都会用年轮帮你记住。',
          time: new Date()
        }
      ],
      inputMessage: '',
      isLoading: false,
      apiBaseUrl: 'http://localhost:8081',
      showSuggestions: true,
      inputPlaceholder: '对着大树说点什么吧...',
      suggestions: [
        '🍂 今天心情有点低落',
        '☀️ 分享一件开心的事',
        '🌙 最近睡不好',
        '💪 需要一些鼓励',
        '🍃 想和你随便聊聊'
      ]
    };
  },
  methods: {
    formatTime(date) {
      if (!date) return '';
      const now = new Date();
      const diff = now - date;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    },
    
    toggleChatWindow() {
      this.isChatWindowOpen = !this.isChatWindowOpen;
      if (this.isChatWindowOpen) {
        this.scrollToBottom();
        this.$nextTick(() => {
          if (this.$refs.messageInput) {
            this.$refs.messageInput.focus();
          }
        });
        setTimeout(() => {
          this.showSuggestions = false;
        }, 5000);
      } else {
        this.showSuggestions = true;
      }
    },
    
    closeChatWindow() {
      this.isChatWindowOpen = false;
      this.showSuggestions = true;
    },
    
    quickSend(text) {
      this.inputMessage = text;
      this.sendMessage();
    },
    
    async sendMessage() {
      const message = this.inputMessage.trim();
      if (!message || this.isLoading) return;
      
      this.showSuggestions = false;
      
      // 添加用户消息
      this.messages.push({ 
        type: 'user', 
        content: message,
        time: new Date()
      });
      this.inputMessage = '';
      this.isLoading = true;
      this.scrollToBottom();
      
      // 使用 POST 请求发送消息
      const url = `${this.apiBaseUrl}/api/ai/chat`;
      console.log('📤 发送请求:', url, { message });
      
      try {
        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ message: message })
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('📥 收到回复:', data);
        
        // 处理AI回复（兼容多种返回格式）
        let aiReply = null;
        if (data.code === 200) {
          if (typeof data.data === 'string') {
            aiReply = data.data;
          } else if (data.data && data.data.reply) {
            aiReply = data.data.reply;
          } else if (data.data && data.data.message) {
            aiReply = data.data.message;
          } else if (data.message) {
            aiReply = data.message;
          }
        }
        
        if (aiReply) {
          this.messages.push({ 
            type: 'ai', 
            content: aiReply,
            time: new Date()
          });
        } else {
          this.messages.push({ 
            type: 'ai', 
            content: '🍂 风吹过树叶，声音有点模糊...能再说一次吗？',
            time: new Date()
          });
        }
      } catch (error) {
        console.error('❌ AI聊天错误:', error);
        this.messages.push({ 
          type: 'ai', 
          content: '🌿 大树暂时听不到你的声音，等风再吹一会儿再试试好吗？',
          time: new Date()
        });
      } finally {
        this.isLoading = false;
        this.scrollToBottom();
      }
    },
    
    scrollToBottom() {
      this.$nextTick(() => {
        const chatBody = this.$refs.chatBody;
        if (chatBody) {
          chatBody.scrollTop = chatBody.scrollHeight;
        }
      });
    }
  }
};
</script>

<style scoped>
/* ========== 悬浮球样式 ========== */
.ai-float-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 30px;
  background: linear-gradient(135deg, #5B8C6F 0%, #4A735A 100%);
  cursor: pointer;
  z-index: 1000;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(75, 115, 90, 0.3);
}

.ai-float-button.expanded {
  transform: scale(0.9);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.ai-float-button:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 30px rgba(75, 115, 90, 0.4);
}

/* 波纹动画 */
.ripple-container {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.ripple {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(91, 140, 111, 0.3);
  animation: ripple 2s ease-out infinite;
}

.ripple.delay-1 {
  animation-delay: 0.6s;
}

.ripple.delay-2 {
  animation-delay: 1.2s;
}

@keyframes ripple {
  0% {
    transform: scale(1);
    opacity: 0.4;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.ai-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  transition: transform 0.3s ease;
}

.ai-float-button:hover .ai-icon {
  transform: scale(1.1);
}

.tooltip {
  position: absolute;
  right: 70px;
  background: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  white-space: nowrap;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s ease;
  pointer-events: none;
  backdrop-filter: blur(4px);
}

.ai-float-button:hover .tooltip {
  opacity: 1;
  transform: translateX(0);
}

/* ========== 聊天窗口样式 ========== */
.ai-chat-window {
  position: fixed;
  bottom: 100px;
  right: 30px;
  width: 400px;
  height: 600px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 28px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  z-index: 999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(10px);
  animation: slideUp 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.window-blur-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(91, 140, 111, 0.05) 0%, rgba(74, 115, 90, 0.08) 100%);
  pointer-events: none;
}

/* 头部 */
.ai-chat-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #5B8C6F 0%, #4A735A 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.avatar-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  animation: pulse 2s ease-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0;
  }
}

.header-text h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.header-text p {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.85;
}

.ai-chat-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.ai-chat-close:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

/* 消息区域 */
.ai-chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #FAFCF8;
  position: relative;
  z-index: 1;
}

.ai-chat-body::-webkit-scrollbar {
  width: 4px;
}

.ai-chat-body::-webkit-scrollbar-track {
  background: #E5EDE5;
  border-radius: 4px;
}

.ai-chat-body::-webkit-scrollbar-thumb {
  background: #5B8C6F;
  border-radius: 4px;
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 欢迎卡片 */
.welcome-card {
  text-align: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, rgba(91, 140, 111, 0.08) 0%, rgba(74, 115, 90, 0.04) 100%);
  border-radius: 24px;
  margin-bottom: 10px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-text p {
  margin: 0 0 8px;
  font-size: 16px;
  color: #3D5A4A;
  font-weight: 500;
}

.welcome-text small {
  font-size: 12px;
  color: #8BAD9A;
}

/* 消息气泡 */
.message-item {
  display: flex;
  gap: 10px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  background: #E5EDE5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.ai-message .message-avatar {
  background: rgba(91, 140, 111, 0.12);
}

.message-bubble {
  max-width: 70%;
}

.message-content {
  padding: 12px 16px;
  border-radius: 20px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.ai-message .message-content {
  background: white;
  color: #2C4A3A;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.user-message .message-content {
  background: linear-gradient(135deg, #5B8C6F 0%, #4A735A 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-time {
  font-size: 10px;
  color: #9BBFA8;
  margin-top: 4px;
  padding: 0 8px;
}

.user-message .message-time {
  text-align: right;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #5B8C6F;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

/* 输入区域 */
.ai-chat-input {
  padding: 16px 20px 20px;
  background: white;
  border-top: 1px solid rgba(91, 140, 111, 0.1);
  position: relative;
  z-index: 1;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  background: #F2F7F2;
  border-radius: 28px;
  padding: 4px 4px 4px 20px;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  background: #EAF1EA;
  box-shadow: 0 0 0 2px rgba(91, 140, 111, 0.2);
}

.input-wrapper input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-size: 14px;
  padding: 12px 0;
  color: #2C4A3A;
}

.input-wrapper input::placeholder {
  color: #B8D0C2;
}

.input-wrapper button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5B8C6F 0%, #4A735A 100%);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.input-wrapper button:hover:not(:disabled) {
  transform: scale(1.05);
}

.input-wrapper button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 快捷建议 */
.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.suggestion-chip {
  background: #F0F5F0;
  border: none;
  padding: 8px 16px;
  border-radius: 24px;
  font-size: 13px;
  color: #5A8C6E;
  cursor: pointer;
  transition: all 0.2s;
}

.suggestion-chip:hover {
  background: #E3ECE3;
  transform: translateY(-2px);
  color: #3D6A50;
}
</style>