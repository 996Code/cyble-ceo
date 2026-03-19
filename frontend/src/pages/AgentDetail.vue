<template>
  <div class="cyber-dashboard">
    <div class="container">
      <div class="breadcrumb">
        <span class="breadcrumb-item" @click="goBack">任务总览</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item current">{{ agent.agentId }} - 详情</span>
      </div>

      <div class="detail-layout">
        <div class="detail-sidebar">
          <div class="agent-detail-header">
            <div class="agent-detail-avatar">
              {{ getInitials(agent.agentId) }}
            </div>
            <div class="agent-detail-name">{{ agent.agentId }}</div>
            <div class="agent-detail-workspace">{{ getWorkspaceName(agent.workspace) }}</div>
          </div>

          <div class="info-row">
            <span class="info-label">状态</span>
            <status-tag :status="agent.status" />
          </div>
          <div class="info-row">
            <span class="info-label">当前任务</span>
            <span class="info-value">{{ agent.currentTaskName || '无' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">最后活跃</span>
            <span class="info-value">{{ agent.lastActive ? formatDateTime(agent.lastActive) : '刚刚' }}</span>
          </div>
        </div>

        <div class="detail-main">
          <div class="detail-card">
            <h3>
              📋 任务历史
              <el-button size="small" @click="viewAllTasks">查看全部</el-button>
            </h3>
            <div class="task-history-list">
              <div
                v-for="task in sortedTaskHistory"
                :key="task.id"
                class="task-history-item"
              >
                <div class="task-history-info">
                  <div class="task-history-name">{{ task.taskName || '未知任务' }}</div>
                  <div class="task-history-time">
                    {{ formatDateTime(task.reportTime) }}
                    <span v-if="getTaskDuration(task)" class="task-duration">· 耗时 {{ getTaskDuration(task) }}</span>
                  </div>
                </div>
                <span :class="['task-history-status', getTaskStatusClass(task.reportType)]">
                  {{ getTaskStatusText(task.reportType) }}
                </span>
              </div>
              <div v-if="!sortedTaskHistory || sortedTaskHistory.length === 0" class="empty-message">
                暂无任务历史
              </div>
            </div>
          </div>

          <div class="detail-card">
            <h3>
              📝 实时日志
              <el-button size="small" @click="viewLogs">完整日志</el-button>
            </h3>
            <div class="log-viewer">
              <div v-for="(log, index) in recentLogs" :key="log.id || index" class="log-entry">
                <span class="log-time">{{ formatTime(log.logTime) }}</span>
                <span :class="['log-level', log.level]">{{ log.level }}</span>
                <span class="log-content">{{ log.message }}</span>
              </div>
              <div v-if="!recentLogs || recentLogs.length === 0" class="empty-message">
                暂无日志
              </div>
            </div>
          </div>

          <div class="detail-card">
            <h3>
              💬 交互记录
              <el-button size="small" @click="viewInteractions">查看全部</el-button>
            </h3>
            <div class="conversation-list">
              <div
                v-for="(conv, index) in recentConversations"
                :key="index"
                :class="['conversation-item', conv.role.toLowerCase()]"
              >
                <div class="conversation-avatar">
                  {{ conv.role === 'AGENT' ? 'A' : conv.role === 'USER' ? 'U' : 'C' }}
                </div>
                <div class="conversation-bubble">
                  {{ conv.content }}
                </div>
              </div>
              <div v-if="!recentConversations || recentConversations.length === 0" class="empty-message">
                暂无交互记录
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import StatusTag from '../components/StatusTag.vue'
import { formatDateTime, formatTime } from '../utils/dateFormat'

export default {
  name: 'AgentDetail',
  components: { StatusTag },
  data() {
    return {
      agent: {
        agentId: '',
        workspace: '',
        status: 'IDLE',
        lastActive: '',
        currentTaskName: null,
        taskHistory: [],
        logs: [],
        conversations: []
      },
      loading: false,
      lastUpdate: '刚刚'
    }
  },
  computed: {
    recentLogs() {
      if (!this.agent || !this.agent.logs) return []
      return this.agent.logs.slice(0, 5)
    },
    recentConversations() {
      if (!this.agent || !this.agent.conversations) return []
      return this.agent.conversations.slice(0, 2)
    },
    sortedTaskHistory() {
      // 显示所有任务历史，按时间倒序
      if (!this.agent || !this.agent.taskHistory) return []
      return [...this.agent.taskHistory].sort((a, b) => 
        new Date(b.reportTime) - new Date(a.reportTime)
      )
    }
  },
  mounted() {
    this.loadAgentDetail()
  },
  methods: {
    getInitials(agentId) {
      if (!agentId) return '?'
      return agentId.charAt(0).toUpperCase()
    },
    getWorkspaceName(workspace) {
      if (!workspace) return ''
      const parts = workspace.split('/')
      return parts[parts.length - 1] || workspace
    },
    getTaskDuration(task) {
      // 对 TASK_COMPLETE 和 PROGRESS 类型计算耗时（从 TASK_START 开始）
      if (!task.reportTime) return null
      
      // 查找同一个任务名的 TASK_START 记录
      const startTask = this.agent.taskHistory.find(t => 
        t.taskName === task.taskName && t.reportType === 'TASK_START'
      )
      
      if (!startTask || !startTask.reportTime) return null
      
      const startTime = new Date(startTask.reportTime)
      const endTime = new Date(task.reportTime)
      const diffMs = endTime - startTime
      
      if (isNaN(diffMs) || diffMs < 0) return null
      
      const hours = Math.floor(diffMs / (1000 * 60 * 60))
      const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
      
      if (hours > 0) {
        return `${hours}h ${minutes}m`
      } else if (minutes > 0) {
        return `${minutes}m`
      } else {
        const seconds = Math.floor(diffMs / 1000)
        return seconds > 0 ? `${seconds}s` : '< 1s'
      }
    },
    getTaskStatusClass(reportType) {
      // TASK_COMPLETE 显示为已完成，其他显示对应状态
      if (!reportType) return 'completed'
      const map = {
        'TASK_COMPLETE': 'completed',
        'TASK_START': 'started',
        'PROGRESS': 'in-progress',
        'BLOCK': 'pending',
        'ERROR': 'error'
      }
      return map[reportType] || 'completed'
    },
    getTaskStatusText(reportType) {
      // 根据 reportType 显示状态文本
      if (!reportType) return '未知'
      const map = {
        'TASK_COMPLETE': '已完成',
        'TASK_START': '已启动',
        'PROGRESS': '进行中',
        'BLOCK': '已阻塞',
        'ERROR': '错误'
      }
      return map[reportType] || reportType
    },
    async loadAgentDetail() {
      this.loading = true
      const agentId = this.$route.params.id
      
      try {
        const response = await fetch(`/api/v1/dashboard/agent/${agentId}`)
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data = await response.json()
        
        if (data.code === 200 && data.data) {
          // 后端字段映射到前端
          this.agent = {
            agentId: data.data.id || data.data.agentId,
            workspace: data.data.workspace || data.data.location,
            status: data.data.status,
            lastActive: data.data.lastHeartbeat || data.data.lastActive,
            currentTaskName: data.data.currentTask?.name || data.data.currentTaskName,
            taskHistory: data.data.taskHistory || [],
            logs: data.data.logs || [],
            conversations: data.data.conversations || []
          }
        } else {
          console.error('❌ 数据格式错误:', data)
          this.$message?.error('获取 Agent 数据格式错误')
        }
      } catch (error) {
        console.error('❌ 加载 Agent 详情失败:', error)
        this.$message?.error('加载 Agent 详情失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    refresh() {
      this.loadAgentDetail()
      this.lastUpdate = '刚刚'
    },
    goBack() {
      this.$router.push('/')
    },
    viewAllTasks() {
      this.$router.push(`/tasks/${this.agent.agentId}`)
    },
    viewLogs() {
      this.$router.push(`/logs/${this.agent.agentId}`)
    },
    viewInteractions() {
      this.$router.push(`/interactions/${this.agent.agentId}`)
    },
    formatDateTime,
    formatTime,
    getTaskStatusClass(reportType) {
      if (!reportType) return 'completed'
      const map = {
        'TASK_COMPLETE': 'completed',
        'TASK_START': 'in-progress',
        'PROGRESS': 'in-progress',
        'BLOCK': 'pending',
        'ERROR': 'error'
      }
      return map[reportType] || 'completed'
    },
    getTaskStatusText(reportType) {
      if (!reportType) return '未知'
      const map = {
        'TASK_COMPLETE': '已完成',
        'TASK_START': '已启动',
        'PROGRESS': '进行中',
        'BLOCK': '已阻塞',
        'ERROR': '错误'
      }
      return map[reportType] || reportType
    }
  }
}
</script>

<style scoped>
.cyber-dashboard {
  min-height: 100vh;
  background: linear-gradient(0deg, #0a0e17 0%, #0d1117 100%);
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(0, 217, 255, 0.05) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(0, 255, 136, 0.05) 0%, transparent 20%),
    linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
  background-size: 100% 100%, 100% 100%, 20px 20px, 20px 20px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 40px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #9ca3af;
}

.breadcrumb-item {
  cursor: pointer;
  transition: color 0.2s;
  color: #9ca3af;
}

.breadcrumb-item:hover {
  color: #00d9ff;
  text-shadow: 0 0 8px rgba(0, 217, 255, 0.8);
}

.breadcrumb-item.current {
  color: #00d9ff;
  font-weight: 500;
  cursor: default;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.7);
}

.breadcrumb-separator {
  color: #4b5563;
}

.detail-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
}

.detail-sidebar {
  background: rgba(17, 24, 39, 0.8);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  height: fit-content;
  position: sticky;
  top: 100px;
  border: 1px solid rgba(0, 217, 255, 0.3);
  backdrop-filter: blur(10px);
}

.agent-detail-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(0, 217, 255, 0.2);
}

.agent-detail-avatar {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #00d9ff 0%, #00ff88 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 32px;
  margin: 0 auto 16px;
  box-shadow: 0 0 25px rgba(0, 217, 255, 0.6);
  animation: cyber-pulse 3s ease-in-out infinite alternate;
}

@keyframes cyber-pulse {
  0%, 100% {
    box-shadow: 0 0 25px rgba(0, 217, 255, 0.6);
  }
  50% {
    box-shadow: 0 0 40px rgba(0, 217, 255, 0.8);
  }
}

.agent-detail-name {
  font-size: 20px;
  font-weight: 600;
  color: #00d9ff;
  margin-bottom: 8px;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.7);
}

.agent-detail-workspace {
  font-size: 14px;
  color: #00ff88;
  text-shadow: 0 0 5px rgba(0, 255, 136, 0.5);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 217, 255, 0.1);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #00d9ff;
  font-weight: 500;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.5);
}

.detail-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-card {
  background: rgba(17, 24, 39, 0.8);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  border: 1px solid rgba(0, 217, 255, 0.3);
  backdrop-filter: blur(10px);
}

.detail-card h3 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #00d9ff;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.7);
}

.task-history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(31, 41, 55, 0.6);
  border-radius: 12px;
  transition: all 0.2s;
  border: 1px solid rgba(0, 217, 255, 0.2);
}

.task-history-item:hover {
  background: rgba(0, 217, 255, 0.1);
  transform: translateX(4px);
  box-shadow: 0 0 15px rgba(0, 217, 255, 0.3);
}

.task-history-info {
  flex: 1;
}

.task-history-name {
  font-size: 15px;
  font-weight: 600;
  color: #e5e7eb;
  margin-bottom: 6px;
  white-space: pre-wrap;
  word-break: break-word;
}

.task-history-time {
  font-size: 13px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-duration {
  color: #00d9ff;
  font-weight: 500;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.5);
}

.task-history-status {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-left: 16px;
  white-space: nowrap;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.task-history-status.completed {
  background: rgba(0, 255, 136, 0.1);
  color: #00ff88;
  box-shadow: 0 0 10px rgba(0, 255, 136, 0.3);
}

.task-history-status.in-progress {
  background: rgba(0, 217, 255, 0.1);
  color: #00d9ff;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.3);
}

.task-history-status.pending {
  background: rgba(255, 170, 0, 0.1);
  color: #ffaa00;
  box-shadow: 0 0 10px rgba(255, 170, 0, 0.3);
}

.task-history-status.error {
  background: rgba(255, 68, 102, 0.1);
  color: #ff4466;
  box-shadow: 0 0 10px rgba(255, 68, 102, 0.3);
}

.log-viewer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  background: rgba(0, 0, 0, 0.3);
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(0, 217, 255, 0.2);
}

.log-entry {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 18px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
  border: 1px solid rgba(0, 217, 255, 0.1);
}

.log-time {
  color: #00d9ff;
  font-size: 12px;
  white-space: nowrap;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.5);
}

.log-level {
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 11px;
  text-transform: uppercase;
  min-width: 50px;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.log-level.INFO {
  background: rgba(0, 217, 255, 0.1);
  color: #00d9ff;
  box-shadow: 0 0 8px rgba(0, 217, 255, 0.3);
}

.log-level.WARN {
  background: rgba(255, 170, 0, 0.1);
  color: #ffaa00;
  box-shadow: 0 0 8px rgba(255, 170, 0, 0.3);
}

.log-level.ERROR {
  background: rgba(255, 68, 102, 0.1);
  color: #ff4466;
  box-shadow: 0 0 8px rgba(255, 68, 102, 0.3);
}

.log-content {
  color: #e5e7eb;
  flex: 1;
  white-space: pre-wrap;
  word-break: break-word;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(31, 41, 55, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(0, 217, 255, 0.1);
}

.conversation-item.user {
  background: rgba(0, 217, 255, 0.1);
  border-left: 3px solid #00d9ff;
}

.conversation-item.agent {
  background: rgba(0, 255, 136, 0.1);
  border-left: 3px solid #00ff88;
}

.conversation-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #00d9ff 0%, #00ff88 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.5);
}

.conversation-item.user .conversation-avatar {
  background: linear-gradient(135deg, #ff4466 0%, #00d9ff 100%);
  box-shadow: 0 0 10px rgba(255, 68, 102, 0.5);
}

.conversation-bubble {
  flex: 1;
  color: #e5e7eb;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.empty-message {
  text-align: center;
  color: #6b7280;
  padding: 40px 20px;
  font-size: 14px;
  text-shadow: 0 0 5px rgba(107, 114, 128, 0.3);
}

@media (max-width: 1024px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-sidebar {
    position: static;
  }
}
</style>
