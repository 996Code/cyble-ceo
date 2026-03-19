import { createRouter, createWebHistory } from 'vue-router'
import Overview from '../pages/Overview.vue'
import AgentDetail from '../pages/AgentDetail.vue'
import LogViewer from '../pages/LogViewer.vue'
import Interactions from '../pages/Interactions.vue'
import TaskHistory from '../pages/TaskHistory.vue'
import TaskBoard from '../pages/TaskBoard.vue'
import TaskDetail from '../pages/TaskDetail.vue'

const routes = [
  { path: '/', name: 'Overview', component: Overview },
  { path: '/agent/:id', name: 'AgentDetail', component: AgentDetail },
  { path: '/logs/:agentId', name: 'LogViewer', component: LogViewer },
  { path: '/interactions/:agentId', name: 'Interactions', component: Interactions },
  { path: '/tasks/:agentId', name: 'TaskHistory', component: TaskHistory },
  { path: '/taskboard', name: 'TaskBoard', component: TaskBoard },
  { path: '/task/:id', name: 'TaskDetail', component: TaskDetail }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router