<template>
  <div id="app">
    <Header />
    <router-view :key="refreshKey" />
  </div>
</template>

<script>
import Header from './components/Header.vue'
import { ref, provide } from 'vue'

export default {
  name: 'App',
  components: {
    Header
  },
  setup() {
    const refreshKey = ref(0)
    
    // 监听全局刷新事件
    window.addEventListener('global-refresh', () => {
      refreshKey.value += 1
    })
    
    // 提供刷新方法给子组件
    provide('refreshKey', refreshKey)
    
    return {
      refreshKey
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  background: linear-gradient(135deg, #0a0e17 0%, #1a1f35 100%);
  /* 或者 */
  /* background: #0d1117;  GitHub 深色 */
}

#app {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0e17 0%, #1a1f35 100%);
}
</style>