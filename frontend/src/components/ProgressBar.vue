<template>
  <div class="progress-bar">
    <div class="progress-fill" :style="{ width: percentage + '%', background: gradient || defaultGradient }"></div>
  </div>
</template>

<script>
export default {
  name: 'ProgressBar',
  props: {
    percentage: {
      type: Number,
      required: true,
      default: 0
    },
    gradient: {
      type: String,
      default: null
    }
  },
  computed: {
    defaultGradient() {
      // 使用赛博朋克风格的渐变色
      return `linear-gradient(90deg, #00d9ff, #00ff88)`
    }
  }
}
</script>

<style scoped>
.progress-bar {
  height: 12px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid rgba(0, 217, 255, 0.2);
  box-shadow: inset 0 0 10px rgba(0, 217, 255, 0.1);
}

.progress-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.5s ease;
  box-shadow: 0 0 15px rgba(0, 217, 255, 0.5);
  animation: glow-pulse 2s infinite alternate;
}

@keyframes glow-pulse {
  0% {
    box-shadow: 0 0 10px rgba(0, 217, 255, 0.5);
  }
  100% {
    box-shadow: 0 0 20px rgba(0, 217, 255, 0.8), 0 0 30px rgba(0, 255, 136, 0.6);
  }
}
</style>
