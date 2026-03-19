# 任务看板后端 - 新功能说明

## 功能概述

本次更新实现了任务看板后端的根本性修复，解决了历史任务永久堆积的问题。

## API 更新

### 1. 任务列表分页 + 过滤

**请求**: `GET /api/v1/tasks`

**新增参数**:
- `page`: 页码 (默认 0)
- `size`: 每页数量 (默认 20)
- `days`: 只返回最近 N 天的任务 (可选)
- `includeArchived`: 是否包含已归档任务 (默认 false)

**响应格式**:
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "currentPage": 0,
  "size": 20
}
```

### 2. 任务归档

**请求**: `POST /api/v1/tasks/archive`

**请求体**:
```json
{
  "beforeDays": 30,      // 归档N天前的已完成任务
  "taskIds": ["T-xxx"]   // 或指定任务ID列表
}
```

### 3. 创建任务指定初始状态

**请求**: `POST /api/v1/tasks`

**新增字段**:
```json
{
  "title": "任务标题",
  "description": "任务描述",
  "initialStatus": "DOING"  // 可选，直接设置初始状态
}
```

### 4. 清理历史数据

**请求**: `DELETE /api/v1/tasks/clear-all`

> 注意：仅限开发/测试环境使用

## 技术改进

- 实现了软删除机制，使用 `archived` 字段标记归档任务
- 优化了状态转换逻辑，支持创建时直接设置任意合法状态
- 增强了分页查询性能
- 保持了向后兼容性

## 验证

- ✅ 代码编译通过
- ✅ 单元测试覆盖新功能
- ✅ 向后兼容性保证