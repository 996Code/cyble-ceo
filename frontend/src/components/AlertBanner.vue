<template>
  <div v-if="alerts && alerts.length > 0" :class="['alert-banner', { show: true, error: hasError, warning: hasWarning }]">
    <span>⚠️</span>
    <span>{{ alertMessage }}</span>
  </div>
</template>

<script>
export default {
  name: 'AlertBanner',
  props: {
    alerts: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    hasError() {
      return this.alerts.some(a => a.type === 'error')
    },
    hasWarning() {
      return this.alerts.some(a => a.type === 'warning')
    },
    alertMessage() {
      if (this.alerts.length === 0) return ''
      if (this.alerts.length === 1) {
        return this.alerts[0].message
      }
      return `检测到 ${this.alerts.length} 条告警信息，请密切关注`
    }
  }
}
</script>

<style scoped>
.alert-banner {
  background: rgba(255, 170, 0, 0.1);
  border-left: 4px solid #ffaa00;
  padding: 15px 40px;
  display: none;
  align-items: center;
  gap: 10px;
  color: #ffaa00;
  font-size: 14px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  box-shadow: 0 0 20px rgba(255, 170, 0, 0.3);
  animation: cyber-alert 3s infinite alternate;
}

.alert-banner.error {
  background: rgba(255, 68, 102, 0.1);
  border-left-color: #ff4466;
  color: #ff4466;
  box-shadow: 0 0 20px rgba(255, 68, 102, 0.3);
  animation: cyber-alert-error 1s infinite alternate;
}

.alert-banner.warning {
  background: rgba(255, 170, 0, 0.1);
  border-left-color: #ffaa00;
  color: #ffaa00;
  box-shadow: 0 0 20px rgba(255, 170, 0, 0.3);
  animation: cyber-alert 2s infinite alternate;
}

.alert-banner.show {
  display: flex;
}

@keyframes cyber-alert {
  0%, 100% {
    box-shadow: 0 0 10px rgba(255, 170, 0, 0.3);
  }
  50% {
    box-shadow: 0 0 30px rgba(255, 170, 0, 0.6);
  }
}

@keyframes cyber-alert-error {
  0%, 100% {
    box-shadow: 0 0 10px rgba(255, 68, 102, 0.3);
  }
  50% {
    box-shadow: 0 0 30px rgba(255, 68, 102, 0.6);
  }
}
</style>
