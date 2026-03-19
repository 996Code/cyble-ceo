<template>
  <div class="cyber-dashboard">
    <div class="container">
      <div class="breadcrumb">
        <span class="breadcrumb-item" @click="goBack">任务看板</span>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-item current">{{ task.title || '任务详情' }}</span>
      </div>

      <div class="task-detail">
        <div class="task-header">
          <h2>{{ task.title }}</h2>
          <div class="task-id">ID: {{ task.id }}</div>
        </div>

        <div class="task-info">
          <div class="basic-info">
            <div class="info-row">
              <label>状态:</label>
              <span class="status-badge" :class="'status-' + task.status.toLowerCase()">
                {{ getStatusText(task.status) }}
              </span>
            </div>
            <div class="info-row" v-if="task.assignee">
              <label>负责人:</label>
              <span class="info-value">{{ task.assignee }}</span>
            </div>
            <div class="info-row" v-if="task.creator">
              <label>创建人:</label>
              <span class="info-value">{{ task.creator }}</span>
            </div>
            <div class="info-row" v-if="task.progressPercent !== undefined">
              <label>进度:</label>
              <div class="progress-container">
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :class="'progress-' + task.progressPercent"
                  ></div>
                </div>
                <span class="progress-text">{{ task.progressPercent }}%</span>
              </div>
            </div>
            <div class="info-row" v-if="task.currentProgress">
              <label>当前进展:</label>
              <span class="info-value pre-wrap">{{ task.currentProgress }}</span>
            </div>
            <div class="info-row" v-if="task.output">
              <label>产出路径:</label>
              <span class="info-value">{{ task.output }}</span>
            </div>
            <div class="info-row" v-if="task.summary">
              <label>完成摘要:</label>
              <span class="info-value pre-wrap">{{ task.summary }}</span>
            </div>
          </div>

          <div class="time-info" v-if="hasTimeInfo">
            <h3>⏰ 时间信息</h3>
            <div class="info-row" v-if="task.createdAt">
              <label>创建时间:</label>
              <span class="info-value">{{ formatDate(task.createdAt) }}</span>
            </div>
            <div class="info-row" v-if="task.startedAt">
              <label>开始时间:</label>
              <span class="info-value">{{ formatDate(task.startedAt) }}</span>
            </div>
            <div class="info-row" v-if="task.completedAt">
              <label>完成时间:</label>
              <span class="info-value">{{ formatDate(task.completedAt) }}</span>
            </div>
            <div class="info-row" v-if="task.updatedAt">
              <label>更新时间:</label>
              <span class="info-value">{{ formatDate(task.updatedAt) }}</span>
            </div>
          </div>
        </div>

        <div class="task-description" v-if="task.description">
          <h3>📋 任务描述</h3>
          <p class="description-content pre-wrap">{{ task.description }}</p>
        </div>

        <div class="flow-timeline" v-if="flows.length > 0">
          <h3>🔄 流转时间线</h3>
          <div class="timeline">
            <div 
              v-for="(flow, index) in flows" 
              :key="flow.id || index" 
              class="timeline-item"
            >
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="flow-info">
                  <span class="flow-status">{{ getStatusText(flow.toStatus) }}</span>
                  <span class="flow-time">{{ formatDate(flow.createdAt) }}</span>
                </div>
                <div class="flow-agent" v-if="flow.fromAgent || flow.toAgent">
                  <span v-if="flow.fromAgent && flow.toAgent">
                    {{ flow.fromAgent }} → {{ flow.toAgent }}
                  </span>
                  <span v-else-if="flow.fromAgent">
                    {{ flow.fromAgent }}
                  </span>
                  <span v-else-if="flow.toAgent">
                    {{ flow.toAgent }}
                  </span>
                </div>
                <div class="flow-remark" v-if="flow.remark">
                  {{ flow.remark }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="subtasks" v-if="subtasks.length > 0">
          <h3>🔧 子任务</h3>
          <div class="subtask-list">
            <div 
              v-for="subtask in subtasks" 
              :key="subtask.id" 
              class="subtask-item"
              :class="'subtask-' + subtask.status"
            >
              <div class="subtask-header">
                <span class="subtask-seq">{{ subtask.seq }}.</span>
                <span class="subtask-title">{{ subtask.title }}</span>
                <span class="subtask-status">{{ getSubtaskStatusText(subtask.status) }}</span>
              </div>
              <div class="subtask-detail" v-if="subtask.detail">
                {{ subtask.detail }}
              </div>
              <div class="subtask-assignee" v-if="subtask.assignee">
                负责人: {{ subtask.assignee }}
              </div>
              <div class="subtask-updated" v-if="subtask.updatedAt">
                更新于: {{ formatDate(subtask.updatedAt) }}
              </div>
            </div>
          </div>
        </div>

        <div class="blocking-reason" v-if="blockingReason">
          <h3>⚠️ 阻塞原因</h3>
          <p class="blocking-content pre-wrap">{{ blockingReason }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formatDate } from '../utils/dateFormat'

export default {
  name: 'TaskDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const task = ref({})
    const flows = ref([])
    const subtasks = ref([])
    
    const taskId = route.params.id
    
    const getStatusText = (status) => {
      const statusMap = {
        'CREATED': '已创建',
        'ASSIGNED': '已分派',
        'DOING': '执行中',
        'REVIEW': '待审查',
        'DONE': '已完成',
        'BLOCKED': '阻塞中',
        'REJECTED': '被驳回',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }
    
    const getSubtaskStatusText = (status) => {
      const statusMap = {
        'not-started': '未开始',
        'in-progress': '进行中',
        'completed': '已完成'
      }
      return statusMap[status] || status
    }
    
    const hasTimeInfo = () => {
      return task.value.createdAt || 
             task.value.startedAt || 
             task.value.completedAt || 
             task.value.updatedAt
    }
    
    const blockingReason = () => {
      // 如果任务状态为 BLOCKED，尝试从备注或其他字段获取阻塞原因
      if (task.value.status === 'BLOCKED') {
        // 查找最近的流转记录中包含阻塞相关信息的备注
        const latestFlow = flows.value.find(f => f.toStatus === 'BLOCKED')
        return latestFlow?.remark || '任务处于阻塞状态'
      }
      return null
    }
    
    const goBack = () => {
      router.go(-1) || router.push('/taskboard')
    }
    
    const fetchTaskDetail = async () => {
      try {
        const response = await fetch(`/api/v1/tasks/${taskId}`)
        const result = await response.json()
        
        if (result.code === 200) {
          // 根据API返回的数据结构解析任务详情
          const taskData = result.data?.task || result.data || {}
          task.value = taskData
          
          // 解析流转记录
          const flowsData = result.data?.flows || []
          flows.value = flowsData.map(flow => ({
            ...flow,
            createdAt: flow.createdAt || flow.createTime || flow.timestamp
          })).sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
          
          // 解析子任务
          const subtasksData = result.data?.subTasks || result.data?.subtasks || []
          subtasks.value = subtasksData
        } else {
          console.error('获取任务详情失败:', result.message)
          // 可能是旧的API格式，尝试直接使用result作为任务数据
          if(result.task) {
            task.value = result
            flows.value = []
            subtasks.value = []
          }
        }
      } catch (error) {
        console.error('请求任务详情出错:', error)
      }
    }
    
    onMounted(() => {
      fetchTaskDetail()
    })
    
    return {
      task,
      flows,
      subtasks,
      getStatusText,
      getSubtaskStatusText,
      hasTimeInfo,
      blockingReason: blockingReason(),
      goBack,
      formatDate
    }
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

.task-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.task-header {
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(17, 24, 39, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(0, 217, 255, 0.3);
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  backdrop-filter: blur(10px);
}

.task-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #00d9ff;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.7);
}

.task-id {
  font-size: 14px;
  color: #00ff88;
  text-shadow: 0 0 5px rgba(0, 255, 136, 0.5);
}

.task-info {
  background: rgba(17, 24, 39, 0.8);
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  border: 1px solid rgba(0, 217, 255, 0.3);
  backdrop-filter: blur(10px);
}

.basic-info {
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 217, 255, 0.1);
}

.info-row label {
  width: 120px;
  font-weight: bold;
  color: #9ca3af;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #e5e7eb;
  line-height: 1.6;
  text-shadow: 0 0 5px rgba(229, 231, 235, 0.3);
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.3);
}

.status-created { 
  background: rgba(102, 126, 234, 0.2); 
  color: #667eea; 
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.3); 
}
.status-assigned { 
  background: rgba(102, 126, 234, 0.2); 
  color: #667eea; 
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.3); 
}
.status-doing { 
  background: rgba(0, 217, 255, 0.2); 
  color: #00d9ff; 
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.3); 
}
.status-review { 
  background: rgba(118, 75, 162, 0.2); 
  color: #764ba2; 
  box-shadow: 0 0 10px rgba(118, 75, 162, 0.3); 
}
.status-done { 
  background: rgba(0, 255, 136, 0.2); 
  color: #00ff88; 
  box-shadow: 0 0 10px rgba(0, 255, 136, 0.3); 
}
.status-blocked { 
  background: rgba(255, 68, 102, 0.2); 
  color: #ff4466; 
  box-shadow: 0 0 10px rgba(255, 68, 102, 0.3); 
}
.status-rejected { 
  background: rgba(255, 68, 102, 0.2); 
  color: #ff4466; 
  box-shadow: 0 0 10px rgba(255, 68, 102, 0.3); 
}
.status-cancelled { 
  background: rgba(140, 140, 140, 0.2); 
  color: #8c8c8c; 
  box-shadow: 0 0 10px rgba(140, 140, 140, 0.3); 
}

.progress-container {
  display: flex;
  align-items: center;
  flex: 1;
}

.progress-bar {
  flex: 1;
  height: 12px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 6px;
  overflow: hidden;
  margin-right: 12px;
  border: 1px solid rgba(0, 217, 255, 0.2);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00d9ff, #00ff88);
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.5);
}

.progress-text {
  font-size: 14px;
  color: #00d9ff;
  min-width: 50px;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.7);
}

.time-info h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #00d9ff;
  border-bottom: 1px solid rgba(0, 217, 255, 0.3);
  padding-bottom: 10px;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.7);
}

.task-description, .flow-timeline, .subtasks, .blocking-reason {
  background: rgba(17, 24, 39, 0.8);
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: 0 0 20px rgba(0, 217, 255, 0.2);
  border: 1px solid rgba(0, 217, 255, 0.3);
  backdrop-filter: blur(10px);
}

.task-description h3, .flow-timeline h3, .subtasks h3, .blocking-reason h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #00d9ff;
  border-bottom: 1px solid rgba(0, 217, 255, 0.3);
  padding-bottom: 10px;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.7);
}

.description-content {
  color: #e5e7eb;
  line-height: 1.8;
}

.timeline {
  position: relative;
  padding-left: 30px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #00d9ff, #00ff88, #764ba2);
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
  padding-left: 24px;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -29px;
  top: 5px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00d9ff, #00ff88);
  border: 2px solid rgba(17, 24, 39, 0.8);
  z-index: 1;
  box-shadow: 0 0 10px rgba(0, 217, 255, 0.8);
}

.timeline-content {
  background: rgba(31, 41, 55, 0.6);
  padding: 16px;
  border-radius: 12px;
  border-left: 3px solid #00d9ff;
  border: 1px solid rgba(0, 217, 255, 0.2);
}

.flow-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.flow-status {
  font-weight: bold;
  color: #00d9ff;
  text-shadow: 0 0 5px rgba(0, 217, 255, 0.7);
}

.flow-time {
  font-size: 13px;
  color: #9ca3af;
  text-shadow: 0 0 5px rgba(156, 163, 175, 0.3);
}

.flow-agent {
  color: #00ff88;
  font-size: 13px;
  text-shadow: 0 0 5px rgba(0, 255, 136, 0.5);
}

.flow-remark {
  margin-top: 8px;
  color: #e5e7eb;
  font-style: italic;
  padding: 8px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  border-left: 2px solid #764ba2;
}

.subtask-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.subtask-item {
  padding: 16px;
  border: 1px solid rgba(0, 217, 255, 0.2);
  border-radius: 12px;
  background: rgba(31, 41, 55, 0.6);
  transition: all 0.3s;
}

.subtask-item:hover {
  background: rgba(0, 217, 255, 0.1);
  box-shadow: 0 0 15px rgba(0, 217, 255, 0.3);
}

.subtask-item.subtask-completed {
  background: rgba(0, 255, 136, 0.1);
  border-color: rgba(0, 255, 136, 0.3);
}

.subtask-item.subtask-in-progress {
  background: rgba(0, 217, 255, 0.1);
  border-color: rgba(0, 217, 255, 0.3);
}

.subtask-item.subtask-not-started {
  background: rgba(55, 65, 81, 0.4);
  border-color: rgba(107, 114, 128, 0.3);
}

.subtask-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.subtask-seq {
  font-weight: bold;
  color: #9ca3af;
  margin-right: 8px;
}

.subtask-title {
  flex: 1;
  font-weight: bold;
  color: #e5e7eb;
}

.subtask-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.subtask-detail {
  color: #d1d5db;
  font-size: 14px;
  margin: 8px 0;
  padding: 8px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  border-left: 2px solid #00d9ff;
}

.subtask-assignee, .subtask-updated {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 6px;
  text-shadow: 0 0 5px rgba(156, 163, 175, 0.3);
}

.blocking-content {
  color: #e5e7eb;
  line-height: 1.8;
  background: rgba(255, 68, 102, 0.1);
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 68, 102, 0.3);
}

.pre-wrap {
  white-space: pre-wrap !important;
  word-break: break-word !important;
}

.progress-0 { width: 0%; }
.progress-5 { width: 5%; }
.progress-10 { width: 10%; }
.progress-15 { width: 15%; }
.progress-20 { width: 20%; }
.progress-25 { width: 25%; }
.progress-30 { width: 30%; }
.progress-35 { width: 35%; }
.progress-40 { width: 40%; }
.progress-45 { width: 45%; }
.progress-50 { width: 50%; }
.progress-55 { width: 55%; }
.progress-60 { width: 60%; }
.progress-65 { width: 65%; }
.progress-70 { width: 70%; }
.progress-75 { width: 75%; }
.progress-80 { width: 80%; }
.progress-85 { width: 85%; }
.progress-90 { width: 90%; }
.progress-95 { width: 95%; }
.progress-100 { width: 100%; }

@media (max-width: 1024px) {
  .container {
    padding: 20px 20px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .info-row label {
    width: auto;
  }
}
</style>