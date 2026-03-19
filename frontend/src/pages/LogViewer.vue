<template>
  <div class="cyber-dashboard">
    <div class="container">
      <div class="breadcrumb">
        <span class="breadcrumb-item" @click="goBack('overview')">任务总览</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item" @click="goBack('detail')">{{ agentId || 'Agent' }} 详情</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item current">任务日志</span>
      </div>

      <div class="detail-card">
        <div class="log-header">
          <h2>📝 {{ agentId || 'Agent' }} 的日志</h2>
          <div class="log-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索日志..."
              size="small"
              class="search-input"
              @input="filterLogs"
            />
            <el-select v-model="logLevelFilter" size="small" placeholder="所有级别">
              <el-option label="所有级别" value="all" />
              <el-option label="INFO" value="INFO" />
              <el-option label="WARN" value="WARN" />
              <el-option label="ERROR" value="ERROR" />
            </el-select>
          </div>
        </div>

        <div class="log-viewer">
          <div v-for="(log, index) in filteredLogs" :key="index" class="log-entry">
            <span class="log-time">{{ formatTime(log.timestamp || log.logTime) }}</span>
            <span :class="['log-level', log.level]">{{ log.level }}</span>
            <span class="log-content">{{ log.content || log.message }}</span>
          </div>
          <div v-if="!filteredLogs || filteredLogs.length === 0" class="empty-message">
            暂无日志数据
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { formatTime } from '../utils/dateFormat'

export default {
  name: 'LogViewer',
  data() {
    return {
      agentId: '',
      logs: [],
      searchKeyword: '',
      logLevelFilter: 'all',
      loading: false,
      lastUpdate: '刚刚'
    }
  },
  computed: {
    filteredLogs() {
      if (!this.logs) return []
      return this.logs.filter(log => {
        const matchKeyword = !this.searchKeyword || 
          (log.message && log.message.toLowerCase().includes(this.searchKeyword.toLowerCase()))
        // 默认显示所有级别，只有当用户选择了特定级别时才过滤
        const matchLevel = !this.logLevelFilter || this.logLevelFilter === 'all' || log.level === this.logLevelFilter
        return matchKeyword && matchLevel
      })
    }
  },
  mounted() {
    this.loadLogs()
  },
  methods: {
    async loadLogs() {
      this.loading = true
      this.agentId = this.$route.params.agentId
      
      if (!this.agentId) {
        console.error('❌ agentId 为空')
        this.loading = false
        return
      }
      
      try {
        const response = await fetch(`/api/v1/dashboard/logs/${this.agentId}`)
        const data = await response.json()
        
        if (data.code === 200 && data.data) {
          this.logs = data.data.logs || []
        } else {
          console.error('❌ 数据格式错误:', data)
          this.logs = []
        }
      } catch (error) {
        console.error('❌ 加载日志失败:', error)
        this.logs = []
      } finally {
        this.loading = false
      }
    },
    refresh() {
      this.loadLogs()
      this.lastUpdate = '刚刚'
    },
    filterLogs() {
      // 过滤日志，关键字: this.searchKeyword, 级别: this.logLevelFilter
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

.log-viewer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 600px;
  overflow-y: auto;
  background: rgba(0, 0, 0, 0.8);
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(0, 217, 255, 0.3);
  box-shadow: inset 0 0 20px rgba(0, 217, 255, 0.1);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.log-entry {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 18px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 8px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
  border: 1px solid rgba(0, 217, 255, 0.1);
}

.log-time {
  color: #00d9ff;
  font-size: 12px;
  white-space: nowrap;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.7);
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
  text-shadow: 0 0 5px rgba(229, 231, 235, 0.3);
}

.empty-message {
  text-align: center;
  color: #6b7280;
  padding: 40px 20px;
  font-size: 14px;
  text-shadow: 0 0 5px rgba(107, 114, 128, 0.3);
}

/* 自定义滚动条样式 */
.log-viewer::-webkit-scrollbar {
  width: 8px;
}

.log-viewer::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

.log-viewer::-webkit-scrollbar-thumb {
  background: rgba(0, 217, 255, 0.3);
  border-radius: 4px;
}

.log-viewer::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 217, 255, 0.6);
}

/* 输入框和选择框的样式 */
.el-input__wrapper {
  background: rgba(0, 0, 0, 0.4) !important;
  border: 1px solid rgba(0, 217, 255, 0.3) !important;
  color: #e5e7eb !important;
}

.el-input__inner {
  color: #e5e7eb !important;
}

.el-select .el-input__wrapper {
  background: rgba(0, 0, 0, 0.4) !important;
  border: 1px solid rgba(0, 217, 255, 0.3) !important;
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

/* 搜索输入框 */
.search-input {
  width: 200px;
}
</style>
