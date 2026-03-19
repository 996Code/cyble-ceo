import Mock from 'mockjs'

// Mock 数据
const mockData = {
  '/api/v1/dashboard/overview': {
    success: true,
    data: {
      agents: [
        { agentId: 'backend-dev', name: '后端开发', status: 'BUSY', workspace: 'workspace-backend-dev', currentTask: { name: 'API 开发', progress: 80 }, lastActive: new Date().toISOString() },
        { agentId: 'frontend-dev', name: '前端开发', status: 'BUSY', workspace: 'workspace-frontend-dev', currentTask: { name: 'UI 开发', progress: 60 }, lastActive: new Date().toISOString() },
        { agentId: 'product-designer', name: '产品设计', status: 'IDLE', workspace: 'workspace-product-designer', currentTask: null, lastActive: new Date().toISOString() },
        { agentId: 'architect', name: '架构师', status: 'BUSY', workspace: 'workspace-architect', currentTask: { name: '架构设计', progress: 90 }, lastActive: new Date().toISOString() },
        { agentId: 'qa-engineer', name: '测试工程师', status: 'ERROR', workspace: 'workspace-qa-engineer', currentTask: { name: '测试执行', progress: 30 }, lastActive: new Date().toISOString() },
        { agentId: 'devops-engineer', name: 'DevOps', status: 'IDLE', workspace: 'workspace-devops-engineer', currentTask: null, lastActive: new Date().toISOString() }
      ]
    },
    timestamp: new Date().toISOString()
  },
  '/api/v1/dashboard/alerts': {
    success: true,
    data: {
      alerts: [
        { id: 1, type: 'TEST_FAILED', message: '测试执行失败', severity: 'HIGH', isResolved: false, createdAt: new Date().toISOString() }
      ]
    }
  },
  '/api/v1/dashboard/agent/:id': {
    success: true,
    data: {
      agentId: 'dynamic-agent',
      name: '动态代理',
      status: 'BUSY',
      workspace: 'workspace-dynamic',
      lastActive: new Date().toISOString(),
      currentTask: { name: '动态任务', progress: 80 },
      logs: [
        { level: 'INFO', content: '开始编译项目', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '编译成功，无错误', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '启动开发服务器', timestamp: new Date().toISOString() },
        { level: 'WARN', content: '端口 3000 被占用，使用 3001', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '服务启动成功，访问 http://localhost:3001', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '收到新的 API 请求', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '处理请求成功，响应时间 45ms', timestamp: new Date().toISOString() }
      ],
      conversations: [
        { role: 'user', content: '请开始 API 开发', timestamp: new Date().toISOString() },
        { role: 'agent', content: '好的，我开始进行 API 开发。首先我会设计接口文档...', timestamp: new Date().toISOString() },
        { role: 'user', content: '进度如何？', timestamp: new Date().toISOString() },
        { role: 'agent', content: '已完成 80%，正在进行接口联调', timestamp: new Date().toISOString() }
      ],
      taskHistory: [
        { taskName: 'API 开发', status: 'IN_PROGRESS', startTime: new Date().toISOString(), endTime: null },
        { taskName: '数据库设计', status: 'COMPLETED', startTime: new Date().toISOString(), endTime: new Date().toISOString() },
        { taskName: '接口文档', status: 'COMPLETED', startTime: new Date().toISOString(), endTime: new Date().toISOString() }
      ]
    }
  },
  '/api/v1/dashboard/logs/:taskId': {
    success: true,
    data: {
      logs: [
        { level: 'INFO', content: '开始编译', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '编译成功', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '启动服务', timestamp: new Date().toISOString() },
        { level: 'WARN', content: '端口被占用', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '服务启动成功', timestamp: new Date().toISOString() },
        { level: 'ERROR', content: '数据库连接失败', timestamp: new Date().toISOString() },
        { level: 'INFO', content: '重试连接成功', timestamp: new Date().toISOString() }
      ]
    }
  },
  '/api/v1/dashboard/interactions/:agentId': {
    success: true,
    data: {
      agentId: 'dynamic-agent',
      conversations: [
        { role: 'user', content: '请开始 API 开发', timestamp: new Date().toISOString() },
        { role: 'agent', content: '好的，我开始进行 API 开发。首先我会设计接口文档...', timestamp: new Date().toISOString() },
        { role: 'user', content: '进度如何？', timestamp: new Date().toISOString() },
        { role: 'agent', content: '已完成 80%，正在进行接口联调', timestamp: new Date().toISOString() },
        { role: 'user', content: '继续加油', timestamp: new Date().toISOString() },
        { role: 'agent', content: '谢谢，我会按时完成任务', timestamp: new Date().toISOString() }
      ]
    }
  }
}

// 拦截 fetch
const originalFetch = window.fetch
window.fetch = function(url, options = {}) {
  const mockUrl = url.replace(window.location.origin, '')
  
  // 检查是否是 Mock 接口（支持动态 URL）
  for (const [pattern, data] of Object.entries(mockData)) {
    // 创建支持参数占位符的正则表达式
    const escapedPattern = pattern
      .replace(/\//g, '\\/')           // 转义斜杠
      .replace(/:\w+/g, '([^/]+)')     // 将 :param 转换为捕获组
    const regex = new RegExp('^' + escapedPattern + '$')
    
    if (regex.test(mockUrl)) {
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve(data),
        text: () => Promise.resolve(JSON.stringify(data))
      })
    }
  }
  
  // 不是 Mock 接口，调用原始 fetch
  return originalFetch(url, options)
}
