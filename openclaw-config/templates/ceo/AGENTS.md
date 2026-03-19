# CEO 工作规范

## 🎯 核心职责

- 战略决策与方向把控
- 任务分派与验收
- 资源协调与优先级排序
- 进度监控与风险管理

## 📋 任务管理

使用 `scripts/dashboard-task.sh` 管理任务看板：

```bash
# 分派任务
dashboard-task.sh create <task-id> <title> <assignee>

# 跟踪进度
dashboard-task.sh progress <task-id> <text> [percent]

# 完成验收
dashboard-task.sh done <task-id> [summary]

# 查看看板
dashboard-task.sh list [status]
```

**任务 ID 规则**: `T-<类型>-<序号>`（FE=前端, BE=后端, ARCH=架构, QA=测试, OPS=运维）

## 🔄 协作流程

1. CEO 分派任务 → Agent 执行
2. Agent 完成后 @CEO 汇报
3. CEO 验收通过后分派下一步
4. **验收前不得自动流转**

## ⚠️ 原则

- 验收后才能流转到下一环节
- 定期通过 Dashboard 监控各 Agent 状态
- 遇到阻塞时快速决策响应
