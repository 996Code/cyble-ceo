<template>
  <div class="cyber-dashboard">
    <alert-banner :alerts="alerts" />

    <div class="container">
      <div class="filter-bar">
        <div class="filter-group">
          <div class="tab-container">
            <div 
              :class="['tab-item', { active: filterStatus === 'ALL' }]"
              @click="filterStatus = 'ALL'"
            >
              <span class="tab-dot"></span>
              <span class="tab-text">全部</span>
              <span class="tab-count">{{ agents.length }}</span>
            </div>
            <div 
              :class="['tab-item', { active: filterStatus === 'BUSY' }]"
              @click="filterStatus = 'BUSY'"
            >
              <span class="tab-dot"></span>
              <span class="tab-text">忙碌</span>
              <span class="tab-count">{{ stats.busy }}</span>
            </div>
            <div 
              :class="['tab-item', { active: filterStatus === 'IDLE' }]"
              @click="filterStatus = 'IDLE'"
            >
              <span class="tab-dot"></span>
              <span class="tab-text">空闲</span>
              <span class="tab-count">{{ stats.idle }}</span>
            </div>
            <div 
              :class="['tab-item', { active: filterStatus === 'ERROR' }]"
              @click="filterStatus = 'ERROR'"
            >
              <span class="tab-dot"></span>
              <span class="tab-text">异常</span>
              <span class="tab-count">{{ stats.error }}</span>
            </div>
          </div>
        </div>
        <div class="stats">
          <div class="stat-item">
            <span class="stat-dot running"></span>
            <span>运行中：<strong>{{ stats.busy }}</strong></span>
          </div>
          <div class="stat-item">
            <span class="stat-dot idle"></span>
            <span>空闲：<strong>{{ stats.idle }}</strong></span>
          </div>
          <div class="stat-item">
            <span class="stat-dot error"></span>
            <span>异常：<strong>{{ stats.error }}</strong></span>
          </div>
        </div>
      </div>

      <div class="card-grid">
        <div class="agent-card-wrapper" v-for="agent in filteredAgents" :key="agent.agentId">
          <agent-card :agent="agent" @click="viewDetail" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AgentCard from '../components/AgentCard.vue'
import AlertBanner from '../components/AlertBanner.vue'

export default {
  name: 'Overview',
  components: {
    AgentCard,
    AlertBanner
  },
  data() {
    return {
      agents: [],
      filterStatus: 'ALL',
      alerts: [],
      loading: false
    }
  },
  computed: {
    filteredAgents() {
      if (this.filterStatus === 'ALL') return this.agents
      
      // 添加状态映射，处理中文状态到英文状态的转换
      const statusMap = {
        '空闲': 'IDLE',
        '忙碌': 'BUSY',
        '进行中': 'BUSY',
        '异常': 'ERROR'
      };
      
      // 如果 filterStatus 是中文，则转换为英文进行比较
      const actualStatus = statusMap[this.filterStatus] || this.filterStatus;
      return this.agents.filter(a => a.status === actualStatus)
    },
    stats() {
      return {
        busy: this.agents.filter(a => a.status === 'BUSY').length,
        idle: this.agents.filter(a => a.status === 'IDLE').length,
        error: this.agents.filter(a => a.status === 'ERROR').length
      }
    }
  },
  mounted() {
    this.loadAgents()
    this.loadAlerts()
    
    // 监听全局刷新事件
    window.addEventListener('global-refresh', this.refresh)
  },
  beforeUnmount() {
    // 移除事件监听器
    window.removeEventListener('global-refresh', this.refresh)
  },
  methods: {
    async loadAgents() {
      this.loading = true
      try {
        const response = await fetch('/api/v1/agents')
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data = await response.json()
        // 后端返回的是数组格式
        if (Array.isArray(data)) {
          this.agents = data
        } else if (data.code === 200 && Array.isArray(data.data)) {
          // 或者包装格式
          this.agents = data.data
        } else {
          console.error('❌ 数据格式错误:', data)
        }
      } catch (error) {
        console.error('❌ 加载 Agent 列表失败:', error)
        // 显示用户友好的错误消息
        this.$message?.error('加载 Agent 列表失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    async loadAlerts() {
      try {
        const response = await fetch('/api/v1/dashboard/alerts')
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data = await response.json()
        if (data.success) {
          this.alerts = data.data.alerts
        }
      } catch (error) {
        console.error('加载告警失败:', error)
        // 显示用户友好的错误消息
        this.$message?.error('加载告警信息失败')
      }
    },
    refresh() {
      this.loadAgents()
      this.loadAlerts()
    },
    viewDetail(agent) {
      this.$router.push(`/agent/${agent.agentId}`)
    }
  }
}
</script>

<style scoped>
.cyber-dashboard {
  min-height: calc(100vh - 60px); /* 减去 header 高度 */
  background: linear-gradient(135deg, #0a0e17 0%, #1a1f35 100%);
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(0, 217, 255, 0.05) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(0, 255, 136, 0.05) 0%, transparent 20%),
    linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
  background-size: 100% 100%, 100% 100%, 20px 20px, 20px 20px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}



.container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 30px 40px;
}

.filter-bar {
  background: rgba(17, 24, 39, 0.8);  /* 半透明深色 */
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid rgba(0, 217, 255, 0.2);  /* 霓虹边框 */
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  backdrop-filter: blur(10px);  /* 毛玻璃效果 */
}

.filter-group {
  display: flex;
  gap: 10px;
}

/* Tab 容器 */
.tab-container {
  display: flex;
  gap: 8px;
  background: rgba(17, 24, 39, 0.6);
  backdrop-filter: blur(10px);
  padding: 8px;
  border-radius: 20px;
  border: 1px solid rgba(0, 217, 255, 0.3);
  box-shadow: 
    0 0 20px rgba(0, 217, 255, 0.2),
    inset 0 0 10px rgba(0, 217, 255, 0.1);
}

/* Tab 项 */
.tab-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 15px;
  font-weight: 600;
  color: #9ca3af;
  background: transparent;
  border: none;
  position: relative;
  overflow: hidden;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.tab-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #4b5563;
  transition: all 0.3s;
  box-shadow: 0 0 0 rgba(0, 0, 0, 0);
}

.tab-text {
  position: relative;
  z-index: 1;
  color: #9ca3af;
}

.tab-count {
  background: rgba(31, 41, 55, 0.8);
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  color: #9ca3af;
  transition: all 0.3s;
  border: 1px solid rgba(0, 217, 255, 0.2);
}

.tab-item:hover {
  background: rgba(0, 217, 255, 0.1);
  color: #00d9ff;
  transform: translateY(-2px);
  box-shadow: 0 0 15px rgba(0, 217, 255, 0.3);
}

.tab-item:hover .tab-dot {
  background: #00d9ff;
  box-shadow: 0 0 12px rgba(0, 217, 255, 0.8);
}

.tab-item:hover .tab-count {
  background: rgba(0, 217, 255, 0.2);
  color: #00d9ff;
  border-color: rgba(0, 217, 255, 0.4);
}

/* 激活状态 */
.tab-item.active {
  background: rgba(0, 217, 255, 0.2);
  color: #00d9ff;
  box-shadow: 
    0 0 20px rgba(0, 217, 255, 0.4),
    inset 0 0 10px rgba(0, 217, 255, 0.3);
  border: 1px solid rgba(0, 217, 255, 0.5);
}

.tab-item.active .tab-dot {
  background: #00d9ff;
  box-shadow: 0 0 16px rgba(0, 217, 255, 0.8);
  animation: cyber-pulse 2s infinite;
}

.tab-item.active .tab-count {
  background: rgba(0, 217, 255, 0.3);
  color: #00d9ff;
  border-color: rgba(0, 217, 255, 0.6);
}

.stats {
  display: flex;
  gap: 30px;
  font-size: 14px;
  color: #9ca3af;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.stat-dot.running {
  background: #00ff88;  /* 状态色-空闲：绿色 */
  box-shadow: 0 0 8px #00ff88;
}

.stat-dot.idle {
  background: #00d9ff;  /* 状态色-忙碌：蓝色 */
  box-shadow: 0 0 8px #00d9ff;
}

.stat-dot.error {
  background: #ff4466;  /* 状态色-异常：粉色 */
  box-shadow: 0 0 8px #ff4466;
}

/* 卡片网格布局 - 强制等宽 4 列 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  width: 100%;
  max-width: 1800px;
  margin: 0 auto;
}

@media (max-width: 1400px) {
  .card-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 700px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
}

.agent-card-wrapper {
  width: 100%;
  min-height: 320px;
}

/* 添加脉冲动画 */
@keyframes cyber-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
</style>
