<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <h2>赛博软件 · CEO 控制中心</h2>
      </div>
      <nav class="navigation">
        <router-link to="/" class="nav-link">概览</router-link>
        <router-link to="/taskboard" class="nav-link">任务看板</router-link>
        <button :class="['refresh-btn', { loading }]" @click="refresh" :disabled="loading">
          <span :class="['refresh-icon', { spinning: loading }]">⟳</span>
          <span class="refresh-text">{{ loading ? '刷新中...' : '刷新' }}</span>
        </button>
      </nav>
    </div>
  </header>
</template>

<script>
export default {
  name: 'Header',
  data() {
    return {
      loading: false
    }
  },
  methods: {
    async refresh() {
      this.loading = true;
      try {
        // 触发全局刷新事件
        window.dispatchEvent(new CustomEvent('global-refresh'));
      } finally {
        setTimeout(() => {
          this.loading = false;
        }, 500); // 简单模拟加载时间
      }
    }
  }
}
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #0a0e17 0%, #1a1f35 100%);
  box-shadow: 
    0 0 20px rgba(0, 217, 255, 0.3),
    0 0 0 1px rgba(0, 217, 255, 0.5);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 217, 255, 0.3);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo h2 {
  margin: 0;
  background: linear-gradient(90deg, #00d9ff, #00ff88);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.5);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.navigation {
  display: flex;
  gap: 30px;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: #9ca3af;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s ease;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  text-shadow: 0 0 5px rgba(156, 163, 175, 0.3);
}

.nav-link.router-link-active,
.nav-link:hover {
  color: #00d9ff;
  background: rgba(0, 217, 255, 0.1);
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.3);
  text-shadow: 0 0 8px rgba(0, 217, 255, 0.7);
}

/* 刷新按钮 */
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid #00d9ff;
  border-radius: 20px;
  color: #00d9ff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 0 10px rgba(0, 217, 255, 0.3),
    inset 0 0 0 rgba(0, 217, 255, 0);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  height: 36px;
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(0, 217, 255, 0.1);
  box-shadow: 
    0 0 20px rgba(0, 217, 255, 0.6),
    inset 0 0 10px rgba(0, 217, 255, 0.2);
  transform: translateY(-2px);
}

.refresh-btn:active:not(:disabled) {
  transform: translateY(0);
}

.refresh-btn.loading {
  opacity: 0.8;
  cursor: wait;
}

.refresh-icon {
  font-size: 18px;
  font-weight: 500;
  transition: all 0.3s;
  color: #00d9ff;
  filter: drop-shadow(0 0 6px rgba(0, 217, 255, 0.8));
  /* 默认缓慢旋转 */
  animation: cyber-spin-slow 3s linear infinite;
  display: inline-block;
  transform-origin: center center;
}

@keyframes cyber-spin-slow {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.refresh-btn:hover:not(:disabled) .refresh-icon {
  color: #00ff88;
  filter: drop-shadow(0 0 12px rgba(0, 255, 136, 0.8));
  /* 悬停时加速旋转 */
  animation: cyber-spin 1.5s linear infinite;
}

@keyframes cyber-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.refresh-icon.spinning {
  /* 加载中快速旋转 */
  animation: cyber-spin 1s linear infinite;
  color: #ff4466;
  filter: drop-shadow(0 0 16px rgba(255, 68, 102, 1));
}

.refresh-text {
  letter-spacing: 0.5px;
  color: #00d9ff;
}
</style>