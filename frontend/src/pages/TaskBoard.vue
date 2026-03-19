<template>
  <div class="cyber-dashboard">
    <div class="container">
      <div class="breadcrumb">
        <span class="breadcrumb-item" @click="goHome">任务总览</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item current">任务看板</span>
      </div>



      <div class="board-container">
        <div 
          v-for="column in columns" 
          :key="column.key" 
          class="column"
        >
          <div class="column-header">
            <h3>{{ column.title }}</h3>
            <div class="header-controls">
              <span class="status-badge">{{ getTasksCountByStatus(column.status) }}</span>
              <!-- 仅为"已完成"列添加时间范围选择器 -->
              <select 
                v-if="column.status === 'DONE'" 
                v-model="timeRange" 
                @change="changeTimeRange(timeRange)"
                class="time-range-select"
              >
                <option value="7days">最近7天</option>
                <option value="30days">最近30天</option>
                <option value="all">全部</option>
              </select>
            </div>
          </div>
          <div class="task-list">
            <div 
              v-for="task in getTasksByStatus(column.status)" 
              :key="task.id" 
              class="task-card"
              @click="viewTask(task.id)"
            >
              <h4 class="task-title">{{ task.title }}</h4>
              <p class="task-desc pre-wrap" v-if="task.description">{{ task.description }}</p>
              <div class="task-meta">
                <div v-if="task.assignee" class="assignee">负责人: {{ task.assignee }}</div>
                <div v-if="task.creator" class="creator">创建人: {{ task.creator }}</div>
                <div v-if="task.createdAt" class="created-at">创建时间: {{ formatDate(task.createdAt) }}</div>
                <div class="progress" v-if="task.status !== 'DONE' || task.progressPercent">
                  <div class="progress-bar">
                    <div 
                      class="progress-fill" 
                      :style="{ width: (task.status === 'DONE' ? 100 : (task.progressPercent || 0)) + '%' }"
                      :class="{ 'progress-complete': task.status === 'DONE' }"
                    ></div>
                  </div>
                  <span class="progress-text">{{ task.status === 'DONE' ? '100%' : (task.progressPercent !== null && task.progressPercent !== undefined ? task.progressPercent + '%' : '-') }}</span>
                </div>
                <div v-if="task.startedAt" class="started-at">开始时间: {{ formatDate(task.startedAt) }}</div>
                <div v-if="task.completedAt" class="completed-at">完成时间: {{ formatDate(task.completedAt) }}</div>
                <div v-if="task.updatedAt" class="updated-at">更新时间: {{ formatDate(task.updatedAt) }}</div>
              </div>
            </div>
            
            <!-- 空状态提示 -->
            <div 
              v-if="getTasksByStatus(column.status).length === 0" 
              class="empty-state"
            >
              <div v-if="column.status === 'CREATED,ASSIGNED'">暂无待分派任务 📋</div>
              <div v-else-if="column.status === 'DOING'">暂无执行中的任务 🔧</div>
              <div v-else-if="column.status === 'REVIEW'">暂无待审查任务 🔍</div>
              <div v-else-if="column.status === 'DONE'">
                <div v-if="timeRange === '7days'">最近7天内暂无已完成任务 ✅</div>
                <div v-else-if="timeRange === '30days'">最近30天内暂无已完成任务 ✅</div>
                <div v-else>暂无已完成任务 ✅</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { formatDate } from '../utils/dateFormat'

export default {
  name: 'TaskBoard',
  setup() {
    const router = useRouter()
    const tasks = ref([])
    const loading = ref(false)
    
    // 时间范围状态
    const timeRange = ref('7days') // '7days', '30days', 'all'
    
    const columns = [
      { key: 'todo', title: '📋 待分派', status: 'CREATED,ASSIGNED' },
      { key: 'doing', title: '🔧 执行中', status: 'DOING' },
      { key: 'review', title: '🔍 待审查', status: 'REVIEW' },
      { key: 'done', title: '✅ 已完成', status: 'DONE' }
    ]
    
    const fetchTasks = async () => {
      loading.value = true
      try {
        // 对于已完成列，根据时间范围添加相应参数
        let url = '/api/v1/tasks'
        const params = []
        
        // 添加时间范围参数（仅对已完成任务有意义）
        if (timeRange.value !== 'all') {
          const days = timeRange.value === '7days' ? 7 : 30
          params.push(`days=${days}`)
        }
        
        // 添加查询参数
        if (params.length > 0) {
          url += '?' + params.join('&')
        }
        
        const response = await fetch(url)
        const result = await response.json()
        
        if (result.code === 200) {
          tasks.value = result.data || []
        } else {
          console.error('获取任务列表失败:', result.message)
        }
      } catch (error) {
        console.error('请求任务列表出错:', error)
      } finally {
        loading.value = false
      }
    }
    
    // 计算7天前的时间戳
    const sevenDaysAgo = computed(() => {
      const now = new Date()
      now.setDate(now.getDate() - 7)
      return now.getTime()
    })
    
    // 计算30天前的时间戳
    const thirtyDaysAgo = computed(() => {
      const now = new Date()
      now.setDate(now.getDate() - 30)
      return now.getTime()
    })
    
    // 根据时间范围过滤已完成任务
    const getFilteredDoneTasks = (tasks) => {
      if (timeRange.value === 'all') return tasks
      
      const cutoffTime = timeRange.value === '7days' ? sevenDaysAgo.value : thirtyDaysAgo.value
      return tasks.filter(task => {
        // 只有当任务有完成时间时才进行时间范围过滤
        if (!task.completedAt) return true // 如果没有完成时间，则保留
        // 将时间字符串转换为时间戳进行比较
        const completedTime = new Date(task.completedAt).getTime()
        return completedTime >= cutoffTime
      })
    }
    
    const getTasksByStatus = (statusString) => {
      const statuses = statusString.split(',')
      let filteredTasks = tasks.value.filter(task => {
        // 过滤状态匹配的任务
        const statusMatch = statuses.includes(task.status)
        // 对于非DONE任务，排除已归档的任务
        if (statusString !== 'DONE') {
          return statusMatch && !task.archived
        }
        // 对于DONE任务，也要考虑是否已归档
        return statusMatch && !task.archived
      })
      
      // 如果是已完成任务，根据时间范围进一步过滤
      if (statusString === 'DONE') {
        filteredTasks = getFilteredDoneTasks(filteredTasks)
      }
      
      // 按 startedAt 倒序排序（新任务在前）
      filteredTasks.sort((a, b) => {
        const aTime = a.startedAt ? new Date(a.startedAt).getTime() : 0
        const bTime = b.startedAt ? new Date(b.startedAt).getTime() : 0
        return bTime - aTime
      })
      
      return filteredTasks
    }
    
    // 获取指定状态的任务数量（用于显示徽章）
    const getTasksCountByStatus = (statusString) => {
      const statuses = statusString.split(',')
      let allTasks = tasks.value.filter(task => {
        // 过滤状态匹配的任务
        const statusMatch = statuses.includes(task.status)
        // 对于非DONE任务，排除已归档的任务
        if (statusString !== 'DONE') {
          return statusMatch && !task.archived
        }
        // 对于DONE任务，也要考虑是否已归档
        return statusMatch && !task.archived
      })
      
      // 如果是已完成任务，计算总数用于显示徽章
      if (statusString === 'DONE') {
        // 显示实际总数而非过滤后的数量
        return allTasks.length
      }
      
      return allTasks.length
    }
    
    // 获取过滤后已完成任务的数量
    const getFilteredTasksCountByStatus = (statusString) => {
      const statuses = statusString.split(',')
      let filteredTasks = tasks.value.filter(task => {
        // 过滤状态匹配的任务
        const statusMatch = statuses.includes(task.status)
        // 对于非DONE任务，排除已归档的任务
        if (statusString !== 'DONE') {
          return statusMatch && !task.archived
        }
        // 对于DONE任务，也要考虑是否已归档
        return statusMatch && !task.archived
      })
      
      if (statusString === 'DONE') {
        filteredTasks = getFilteredDoneTasks(filteredTasks)
      }
      
      return filteredTasks.length
    }
    
    const viewTask = (taskId) => {
      router.push(`/task/${taskId}`)
    }
    
    const refresh = async () => {
      await fetchTasks()
    }
    
    const goHome = () => {
      router.push('/')
    }
    
    // 切换时间范围
    const changeTimeRange = (range) => {
      timeRange.value = range
      // 重新获取数据以应用新的时间范围
      fetchTasks()
    }
    
    onMounted(() => {
      fetchTasks()
      
      // 监听全局刷新事件
      window.addEventListener('global-refresh', refresh)
    }),
    
    // 在组件卸载前移除事件监听器
    onUnmounted(() => {
      window.removeEventListener('global-refresh', refresh)
    })
    
    return {
      tasks,
      columns,
      timeRange,
      getTasksByStatus,
      getTasksCountByStatus,
      getFilteredTasksCountByStatus,
      changeTimeRange,
      viewTask,
      refresh,
      goHome,
      formatDate,
      loading
    }
  }
}
</script>

<style scoped>
.cyber-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0e17 0%, #1a1f35 100%);  /* 深色渐变 */
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(0, 217, 255, 0.05) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(0, 255, 136, 0.05) 0%, transparent 20%),
    linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
  background-size: 100% 100%, 100% 100%, 20px 20px, 20px 20px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.header-actions-board {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

/* 刷新按钮样式 - 与概览页面保持一致 */
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

.board-container {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.column {
  flex: 1;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(17, 24, 39, 0.8);  /* 半透明深色 */
  border: 1px solid rgba(0, 217, 255, 0.2);  /* 霓虹边框 */
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  backdrop-filter: blur(10px);  /* 毛玻璃效果 */
}

.column-header {
  padding: 16px 20px;
  background: rgba(0, 217, 255, 0.1);
  border-bottom: 1px solid rgba(0, 217, 255, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.column-header h3 {
  margin: 0;
  font-size: 18px;
  color: #00d9ff;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.7);
  font-weight: 600;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-badge {
  background: rgba(0, 217, 255, 0.2);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  color: #00d9ff;
  font-weight: 600;
  border: 1px solid rgba(0, 217, 255, 0.3);
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.7);
}

.time-range-select {
  background: rgba(0, 217, 255, 0.1);
  border: 1px solid rgba(0, 217, 255, 0.3);
  border-radius: 8px;
  padding: 6px 10px;
  color: #00d9ff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  outline: none;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.7);
  transition: all 0.3s ease;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.time-range-select:hover {
  background: rgba(0, 217, 255, 0.2);
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.3);
}

.time-range-select:focus {
  box-shadow: 0 0 15px rgba(0, 217, 255, 0.5);
}

.task-list {
  padding: 16px;
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.task-card {
  background: rgba(17, 24, 39, 0.8);  /* 半透明深色 */
  border: 1px solid rgba(0, 217, 255, 0.2);  /* 霓虹边框 */
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.1);
  backdrop-filter: blur(5px);  /* 毛玻璃效果 */
}

.task-card:hover {
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.4);
  transform: translateY(-2px);
  border-color: rgba(0, 217, 255, 0.5);
  background: rgba(0, 217, 255, 0.1);
}

.task-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #00d9ff;  /* 主标题：霓虹青色 */
  text-shadow: 0 0 8px rgba(0, 217, 255, 0.5);
}

.task-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #e5e7eb;  /* 正文：浅灰色 */
  line-height: 1.5;
  text-shadow: 0 0 5px rgba(229, 231, 235, 0.3);
}

.task-meta {
  font-size: 12px;
  color: #9ca3af;  /* 次要文字：暗灰色 */
}

.task-meta > div {
  margin-bottom: 6px;
}

.assignee, .creator {
  color: #00d9ff;  /* 主标题：霓虹青色 */
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.5);
}

.created-at, .started-at, .completed-at, .updated-at {
  color: #9ca3af;  /* 次要文字：暗灰色 */
  text-shadow: 0 0 5px rgba(156, 163, 175, 0.3);
}

.progress {
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.progress-bar {
  flex: 1;
  height: 12px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid rgba(0, 217, 255, 0.2);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00d9ff, #00ff88);  /* 忙碌：蓝色 #00d9ff 到 空闲：绿色 #00ff88 */
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.5);
}

.progress-text {
  margin-left: 8px;
  font-size: 12px;
  color: #00d9ff;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.7);
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9ca3af;
  font-style: italic;
  text-align: center;
  min-height: 300px;
  background: rgba(17, 24, 39, 0.6);
  border-radius: 12px;
  border: 1px dashed rgba(0, 217, 255, 0.2);
  margin-top: 10px;
}

/* 为不同列设置不同的颜色主题 */
.column:nth-child(1) .column-header { /* 待分派 */
  background: rgba(0, 217, 255, 0.1);
}

.column:nth-child(2) .column-header { /* 执行中 */
  background: rgba(0, 255, 136, 0.1);
}

.column:nth-child(3) .column-header { /* 待审查 */
  background: rgba(118, 75, 162, 0.1);
}

.column:nth-child(4) .column-header { /* 已完成 */
  background: rgba(82, 196, 26, 0.1);
}

.pre-wrap {
  white-space: pre-wrap !important;
  word-break: break-word !important;
}

@media (max-width: 1024px) {
  .board-container {
    flex-direction: column;
  }
  
  .container {
    padding: 20px 20px;
  }
  
  .header-controls {
    flex-direction: column;
    align-items: flex-end;
  }
}
</style>