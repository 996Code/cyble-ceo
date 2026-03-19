<template>
  <div class="cyber-dashboard">
    <div class="container">
      <div class="breadcrumb">
        <span class="breadcrumb-item" @click="goBack('overview')">任务总览</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item" @click="goBack('detail')">{{ agentId || 'Agent' }} 详情</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item current">任务历史</span>
      </div>

      <div class="detail-card">
        <div class="log-header">
          <h2>📋 {{ agentId || 'Agent' }} 的任务历史</h2>
          <div class="log-actions">
            <el-select v-model="statusFilter" size="small" placeholder="所有状态">
              <el-option label="所有状态" value="all" />
              <el-option label="已完成" value="TASK_COMPLETE" />
              <el-option label="已启动" value="TASK_START" />
              <el-option label="进行中" value="PROGRESS" />
              <el-option label="已阻塞" value="BLOCK" />
              <el-option label="错误" value="ERROR" />
            </el-select>
          </div>
        </div>

        <div class="task-history-list">
          <div
            v-for="task in filteredTasks"
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
          <div v-if="!filteredTasks || filteredTasks.length === 0" class="empty-message">
            暂无任务历史
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { formatDateTime, formatTime } from '../utils/dateFormat'

export default {
  name: 'TaskHistory',
  data() {
    return {
      agentId: '',
      tasks: [],
      statusFilter: 'all',
      loading: false,
      lastUpdate: '刚刚'
    }
  },
  computed: {
    filteredTasks() {
      if (!this.tasks) return []
      if (!this.statusFilter || this.statusFilter === 'all') {
        return this.tasks
      }
      return this.tasks.filter(task => task.reportType === this.statusFilter)
    }
  },
  mounted() {
    this.loadTasks()
  },
  methods: {
    async loadTasks() {
      this.loading = true
      this.agentId = this.$route.params.agentId
      
      if (!this.agentId) {
        console.error('❌ agentId 为空')
        this.loading = false
        return
      }
      
      try {
        const response = await fetch(`/api/v1/dashboard/agent/${this.agentId}`)
        const data = await response.json()
        
        if (data.code === 200 && data.data) {
          this.tasks = data.data.taskHistory || []
        } else {
          console.error('❌ 数据格式错误:', data)
          this.tasks = []
        }
      } catch (error) {
        console.error('❌ 加载任务历史失败:', error)
        this.tasks = []
      } finally {
        this.loading = false
      }
    },
    refresh() {
      this.loadTasks()
      this.lastUpdate = '刚刚'
    },
    goBack(page) {
      if (page === 'overview') {
        this.$router.push('/')
      } else if (page === 'detail') {
        if (this.agentId) {
          this.$router.push(`/agent/${this.agentId}`)
        } else {
          this.$router.push('/')
        }
      }
    },
    getTaskDuration(task) {
      if (!task.reportTime) return null
      
      const startTask = this.tasks.find(t => 
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
    formatDateTime,
    formatTime
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
  max-width: 1200px;
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

.detail-card {
  background: rgba(17, 24, 39, 0.8);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  border: 1px solid rgba(0, 217, 255, 0.3);
  backdrop-filter: blur(10px);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.log-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #00d9ff;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.7);
}

.log-actions {
  display: flex;
  gap: 12px;
  align-items: center;
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
  transition: all 0.3s;
  border: 1px solid rgba(0, 217, 255, 0.1);
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
  text-shadow: 0 0 5px rgba(229, 231, 235, 0.3);
}

.task-history-time {
  font-size: 13px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 8px;
  text-shadow: 0 0 5px rgba(156, 163, 175, 0.3);
}

.task-duration {
  color: #00d9ff;
  font-weight: 500;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.7);
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

.task-history-status.started {
  background: rgba(0, 217, 255, 0.1);
  color: #00d9ff;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.3);
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

.empty-message {
  text-align: center;
  color: #6b7280;
  padding: 40px 20px;
  font-size: 14px;
  text-shadow: 0 0 5px rgba(107, 114, 128, 0.3);
}

/* 输入框和选择框的样式 */
.el-select .el-input__wrapper {
  background: rgba(0, 0, 0, 0.4) !important;
  border: 1px solid rgba(0, 217, 255, 0.3) !important;
  color: #e5e7eb !important;
}

/* 下拉选项样式 */
.el-select-dropdown {
  background: rgba(17, 24, 39, 0.9) !important;
  border: 1px solid rgba(0, 217, 255, 0.3) !important;
}

.el-popper {
  background: rgba(17, 24, 39, 0.9) !important;
  border: 1px solid rgba(0, 217, 255, 0.3) !important;
  color: #e5e7eb !important;
}
</style>
