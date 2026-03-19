<template>
  <div :class="['status-badge', statusClass]">
    <span class="status-dot"></span>
    <span class="status-text">{{ statusText }}</span>
  </div>
</template>

<script>
export default {
  name: 'StatusTag',
  props: {
    status: {
      type: String,
      default: 'IDLE',
      validator: (value) => !value || ['IDLE', 'BUSY', 'ERROR', 'WARNING'].includes(value)
    }
  },
  computed: {
    statusClass() {
      if (!this.status) return 'status-idle'
      return `status-${this.status.toLowerCase()}`
    },
    statusText() {
      if (!this.status) return '未知'
      const map = {
        'IDLE': '空闲',
        'BUSY': '忙碌',
        'ERROR': '异常',
        'WARNING': '警告'
      }
      return map[this.status] || this.status
    }
  }
}
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  min-width: 80px;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: 0 0 15px rgba(0, 217, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: cyber-pulse 2s infinite;
  box-shadow: 0 0 10px currentColor;
}

.status-text {
  white-space: nowrap;
  text-shadow: 0 0 5px currentColor;
}

/* IDLE 空闲 - 绿色 */
.status-idle {
  background: rgba(0, 255, 136, 0.1);
  color: #00ff88;
  border: 1px solid rgba(0, 255, 136, 0.3);
}

.status-idle .status-dot {
  background: #00ff88;
  box-shadow: 0 0 15px #00ff88;
}

/* BUSY 忙碌 - 蓝色 */
.status-busy {
  background: rgba(0, 217, 255, 0.1);
  color: #00d9ff;
  border: 1px solid rgba(0, 217, 255, 0.3);
}

.status-busy .status-dot {
  background: #00d9ff;
  box-shadow: 0 0 15px #00d9ff;
}

/* ERROR 异常 - 红色 */
.status-error {
  background: rgba(255, 68, 102, 0.1);
  color: #ff4466;
  border: 1px solid rgba(255, 68, 102, 0.3);
}

.status-error .status-dot {
  background: #ff4466;
  box-shadow: 0 0 15px #ff4466;
}

/* WARNING 警告 - 橙色 */
.status-warning {
  background: rgba(255, 170, 0, 0.1);
  color: #ffaa00;
  border: 1px solid rgba(255, 170, 0, 0.3);
}

.status-warning .status-dot {
  background: #ffaa00;
  box-shadow: 0 0 15px #ffaa00;
}

@keyframes cyber-pulse {
  0%, 100% { 
    opacity: 0.6; 
    transform: scale(1);
    box-shadow: 0 0 10px currentColor;
  }
  50% { 
    opacity: 1; 
    transform: scale(1.3);
    box-shadow: 0 0 20px currentColor;
  }
}
</style>
