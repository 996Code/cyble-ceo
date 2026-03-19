# API 测试脚本

## 测试步骤

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 测试API端点

# 创建任务
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试任务",
    "description": "这是一个测试任务",
    "status": "待办",
    "priority": "高",
    "dueDate": "2024-12-31T23:59:59"
  }'

# 获取所有任务
curl -X GET http://localhost:8080/api/tasks

# 获取单个任务 (假设返回的ID是1)
curl -X GET http://localhost:8080/api/tasks/1

# 更新任务
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "更新的测试任务",
    "description": "这是更新的测试任务",
    "status": "进行中",
    "priority": "中",
    "dueDate": "2024-11-30T23:59:59"
  }'

# 删除任务
curl -X DELETE http://localhost:8080/api/tasks/1