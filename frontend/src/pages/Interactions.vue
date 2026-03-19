<template>
  <div class="cyber-dashboard">
    <div class="container">
      <div class="breadcrumb">
        <span class="breadcrumb-item" @click="goBack('overview')">任务总览</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item" @click="goBack('detail')">{{ agentId }} 详情</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item current">交互记录</span>
      </div>

      <div class="detail-card">
        <div class="log-header">
          <h2>💬 {{ agentId }} 的交互记录</h2>
          <div class="log-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索交互内容..."
              size="small"
              class="search-input"
              @input="filterInteractions"
            />
            <el-select v-model="roleFilter" size="small" placeholder="所有角色">
              <el-option label="所有角色" value="all" />
              <el-option label="USER" value="USER" />
              <el-option label="AGENT" value="AGENT" />
              <el-option label="SYSTEM" value="SYSTEM" />
            </el-select>
          </div>
        </div>

        <div class="conversation-list">
          <div
            v-for="(conv, index) in filteredInteractions"
            :key="index"
            :class="['conversation-item', conv.role.toLowerCase()]"
          >
            <div class="conversation-avatar">
              {{ conv.role === 'AGENT' ? 'A' : conv.role === 'USER' ? 'U' : 'S' }}
            </div>
            <div class="conversation-content">
              <div class="conversation-header">
                <span :class="['conversation-role', (conv.role || 'SYSTEM').toLowerCase()]">{{ conv.role || 'SYSTEM' }}</span>
                <span class="conversation-time">{{ formatTime(conv.interactionTime || conv.timestamp) }}</span>
              </div>
              <div class="conversation-text">{{ conv.content || conv.message }}</div>
            </div>
          </div>
          <div v-if="!filteredInteractions || filteredInteractions.length === 0" class="empty-message">
            暂无交互记录
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { formatTime } from '../utils/dateFormat'

export default {
  name: 'Interactions',
  data() {
    return {
      agentId: '',
      interactions: [],
      searchKeyword: '',
      roleFilter: 'all',
      loading: false,
      lastUpdate: '刚刚'
    }
  },
  computed: {
    filteredInteractions() {
      if (!this.interactions) return []
      return this.interactions.filter(conv => {
        const matchKeyword = !this.searchKeyword || 
          (conv.content && conv.content.toLowerCase().includes(this.searchKeyword.toLowerCase()))
        const matchRole = this.roleFilter === 'all' || conv.role === this.roleFilter
        return matchKeyword && matchRole
      })
    }
  },
  mounted() {
    this.loadInteractions()
  },
  methods: {
    async loadInteractions() {
      this.loading = true
      this.agentId = this.$route.params.agentId
      
      if (!this.agentId) {
        console.error('❌ agentId 为空')
        this.loading = false
        return
      }
      
      try {
        const response = await fetch(`/api/v1/dashboard/interactions/${this.agentId}`)
        const data = await response.json()
        
        if (data.code === 200 && data.data) {
          this.interactions = data.data.interactions || []
        } else {
          console.error('❌ 数据格式错误:', data)
          this.interactions = []
        }
      } catch (error) {
        console.error('❌ 加载交互记录失败:', error)
        this.interactions = []
      } finally {
        this.loading = false
      }
    },
    refresh() {
      this.loadInteractions()
      this.lastUpdate = '刚刚'
    },
    filterInteractions() {
      // 过滤交互记录，关键字: this.searchKeyword, 角色: this.roleFilter
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

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 600px;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  background: rgba(31, 41, 55, 0.6);
  border-radius: 12px;
  transition: all 0.3s;
  border: 1px solid rgba(0, 217, 255, 0.1);
}

.conversation-item:hover {
  background: rgba(0, 217, 255, 0.1);
  box-shadow: 0 0 15px rgba(0, 217, 255, 0.3);
}

.conversation-item.user {
  border-left: 4px solid #00d9ff;
  background: rgba(0, 217, 255, 0.05);
}

.conversation-item.agent {
  border-left: 4px solid #00ff88;
  background: rgba(0, 255, 136, 0.05);
}

.conversation-item.system {
  border-left: 4px solid #764ba2;
  background: rgba(118, 75, 162, 0.05);
}

.conversation-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.5);
}

.conversation-item.user .conversation-avatar {
  background: linear-gradient(135deg, #00d9ff 0%, #00ff88 100%);
}

.conversation-item.agent .conversation-avatar {
  background: linear-gradient(135deg, #00ff88 0%, #00d9ff 100%);
}

.conversation-item.system .conversation-avatar {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 12px;
}

.conversation-role {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.conversation-role.user {
  background: rgba(0, 217, 255, 0.1);
  color: #00d9ff;
  box-shadow: 0 0 8px rgba(0, 217, 255, 0.3);
}

.conversation-role.agent {
  background: rgba(0, 255, 136, 0.1);
  color: #00ff88;
  box-shadow: 0 0 8px rgba(0, 255, 136, 0.3);
}

.conversation-role.system {
  background: rgba(118, 75, 162, 0.1);
  color: #764ba2;
  box-shadow: 0 0 8px rgba(118, 75, 162, 0.3);
}

.conversation-time {
  font-size: 13px;
  color: #9ca3af;
  text-shadow: 0 0 5px rgba(156, 163, 175, 0.3);
}

.conversation-text {
  color: #e5e7eb;
  font-size: 14px;
  line-height: 1.6;
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
.conversation-list::-webkit-scrollbar {
  width: 8px;
}

.conversation-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

.conversation-list::-webkit-scrollbar-thumb {
  background: rgba(0, 217, 255, 0.3);
  border-radius: 4px;
}

.conversation-list::-webkit-scrollbar-thumb:hover {
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
