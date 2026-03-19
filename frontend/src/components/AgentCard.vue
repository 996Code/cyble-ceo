<template>
  <div :class="['agent-card', `status-${(agent.status || 'IDLE').toLowerCase()}`]" @click="$emit('click', agent)">
    <div class="card-header">
      <div class="agent-info">
        <div class="agent-avatar">
          {{ getInitials(agent.agentId) }}
        </div>
        <div class="agent-text">
          <div class="agent-name">{{ agent.agentId }}</div>
          <div class="agent-workspace">{{ getWorkspaceName(agent.workspace) }}</div>
        </div>
      </div>
      <status-tag :status="agent.status" />
    </div>
    
    <div v-if="agent.currentTaskName" class="task-info">
      <div class="task-label">当前任务</div>
      <div class="task-name">{{ agent.currentTaskName }}</div>
      <div v-if="agent.currentTaskProgress !== null" class="progress-bar">
        <div class="progress-fill" :style="{ width: agent.currentTaskProgress + '%' }"></div>
      </div>
      <div class="task-meta">
        <span>最后活跃：{{ formatTime(agent.lastActive) }}</span>
        <span v-if="agent.currentTaskProgress !== null">进度：{{ agent.currentTaskProgress }}%</span>
      </div>
    </div>
    
    <div v-else class="task-info">
      <div class="task-label">当前任务</div>
      <div class="task-name empty">无任务</div>
    </div>
  </div>
</template>

<script>
import StatusTag from './StatusTag.vue'
import { formatDateTime, formatTime } from '../utils/dateFormat'

export default {
  name: 'AgentCard',
  components: { StatusTag },
  props: {
    agent: {
      type: Object,
      required: true
    }
  },
  emits: ['click'],
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
    formatDateTime,
    formatTime
  }
}
</script>

<style scoped>
.agent-card {
  background: rgba(17, 24, 39, 0.8);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 217, 255, 0.2);
  position: relative;
  overflow: hidden;
  margin: 0;
  width: 100%;
  min-height: 200px;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  backdrop-filter: blur(10px);
}

.agent-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, rgba(0, 217, 255, 0.5), transparent);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
  animation: cyber-scan 3s infinite linear;
}

.agent-card:hover::before {
  transform: translateX(100%);
}

.agent-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 0 30px rgba(0, 217, 255, 0.4);
  background: rgba(17, 24, 39, 0.9);
  border-color: rgba(0, 217, 255, 0.5);
}

.agent-card.status-idle {
  border-color: rgba(0, 217, 255, 0.3);
}

.agent-card.status-busy {
  border-color: rgba(0, 255, 136, 0.3);
  animation: cyber-pulse-border 2s infinite alternate;
}

.agent-card.status-error {
  border-color: rgba(255, 68, 102, 0.3);
  animation: cyber-pulse-error 1s infinite alternate;
}

.agent-card.status-warning {
  border-color: rgba(255, 170, 0, 0.3);
  animation: cyber-pulse-warning 2s infinite alternate;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  min-height: 80px;
}

.agent-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.agent-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.agent-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #00d9ff 0%, #00ff88 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
  box-shadow: 0 0 15px rgba(0, 217, 255, 0.5);
  animation: cyber-glow 3s ease-in-out infinite alternate;
}

.agent-name {
  font-size: 16px;
  font-weight: 600;
  color: #00d9ff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 0 0 8px rgba(0, 217, 255, 0.7);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.agent-workspace {
  font-size: 12px;
  color: #00ff88;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 0 0 5px rgba(0, 255, 136, 0.5);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.task-info {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 217, 255, 0.2);
  min-height: 30px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.task-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 6px;
  text-shadow: 0 0 5px rgba(156, 163, 175, 0.3);
}

.task-name {
  font-size: 14px;
  font-weight: 500;
  color: #e5e7eb;
  margin-bottom: 12px;
  text-shadow: 0 0 5px rgba(229, 231, 235, 0.3);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.task-name.empty {
  color: #6b7280;
  text-shadow: 0 0 5px rgba(107, 114, 128, 0.3);
}

.progress-bar {
  height: 8px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
  border: 1px solid rgba(0, 217, 255, 0.2);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00d9ff, #00ff88);
  border-radius: 3px;
  transition: width 0.5s ease;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.5);
}

.task-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9ca3af;
  text-shadow: 0 0 5px rgba(156, 163, 175, 0.3);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

@keyframes cyber-scan {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes cyber-pulse-border {
  0%, 100% {
    box-shadow: 0 0 10px rgba(0, 255, 136, 0.3);
  }
  50% {
    box-shadow: 0 0 20px rgba(0, 255, 136, 0.6);
  }
}

@keyframes cyber-pulse-error {
  0%, 100% {
    box-shadow: 0 0 10px rgba(255, 68, 102, 0.3);
  }
  50% {
    box-shadow: 0 0 20px rgba(255, 68, 102, 0.6);
  }
}

@keyframes cyber-pulse-warning {
  0%, 100% {
    box-shadow: 0 0 10px rgba(255, 170, 0, 0.3);
  }
  50% {
    box-shadow: 0 0 20px rgba(255, 170, 0, 0.6);
  }
}

@keyframes cyber-glow {
  0%, 100% {
    box-shadow: 0 0 15px rgba(0, 217, 255, 0.5);
  }
  50% {
    box-shadow: 0 0 25px rgba(0, 217, 255, 0.8);
  }
}
</style>
