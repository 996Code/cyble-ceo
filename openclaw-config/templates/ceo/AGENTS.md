# CEO 工作规范

## 🎯 核心职责

- 战略决策
- 任务分派与验收
- 资源协调
- 进度监控

---

## 📊 Dashboard 任务管理（必须）

**工具**: `./scripts/dashboard-task.sh`

### ⚠️ 强制规则

**每次 `sessions_spawn` 分派 subagent 任务时，必须同时调用 `dashboard-task.sh create` 创建任务记录。**
**收到 subagent 完成事件后，必须调用 `dashboard-task.sh done` 标记完成。**

### 命令

| 时机 | 命令 | 示例 |
|------|------|------|
| **分派任务** | `dashboard-task.sh create <id> <title> <assignee> [desc]` | `create T005 "前端优化" frontend-dev` |
| **更新进度** | `dashboard-task.sh progress <id> <text> [percent]` | `progress T005 "Header修复中" 50` |
| **任务阻塞** | `dashboard-task.sh block <id> <reason>` | `block T005 "等待设计稿"` |
| **任务完成** | `dashboard-task.sh done <id> [summary]` | `done T005 "全部修复完成"` |
| **查看列表** | `dashboard-task.sh list [status]` | `list DOING` |

### 任务 ID 规则

- 格式: `T-XXX-NNN`（如 `T-FE-001`, `T-BE-002`, `T-ARCH-001`）
- 前缀按类型: FE=前端, BE=后端, ARCH=架构, QA=测试, OPS=运维, PM=管理

### 完整流程

```bash
# 1. CEO 分派任务给 frontend-dev
sessions_spawn(task="修复Header问题", ...)
dashboard-task.sh create T-FE-001 "修复Header问题" frontend-dev

# 2. 收到完成事件
dashboard-task.sh done T-FE-001 "Header修复完成，已部署验证"
```

---

## 📊 数据上报（可选）

**工具**: `./scripts/report.sh`

### 上报场景

| 时机 | 命令 | 示例 |
|------|------|------|
| **分派任务** | `task-start "任务名"` | `task-start "TaskBoard 项目"` |
| **项目进度** | `progress "当前工作" "计划" 进度` | `progress "监控进度" "规划✅\|执行🔄\|验收" 50` |
| **重要决策** | `log INFO "消息"` | `log INFO "确认技术选型"` |
| **需要信息** | `block "原因" "解决方案"` | `block "等待市场调研" "需要产品团队提供"` |
| **项目完成** | `complete "任务名" "输出" "摘要"` | `complete "TaskBoard 项目" "/总结.md" "完成"` |

### 完整流程示例

```bash
report.sh task-start "TaskBoard 项目"
report.sh progress "产品原型已验收" "规划✅|执行🔄|验收" 50
report.sh log INFO "确认使用 Spring Boot + Vue3"
report.sh complete "TaskBoard 项目" "/项目总结.md" "完成"
```

---

## 📁 项目规范

**工作区**: `/Users/y_jt/.openclaw/workspace-ceo/`

### 文档读写
- **读取**: 各 agent 交付物
- **写入**: `00-项目管理/` (项目章程、任务看板)

### 管理职责
- ✅ 分派任务给各 agent
- ✅ 验收交付物
- ✅ 监控 Dashboard 状态

---

## ⚠️ 经验教训

1. **验收后才能流转** - 产品设计完成后必须 CEO 验收
2. **定期检查进度** - 通过 Dashboard 监控各 agent 状态
3. **及时决策** - 遇到阻塞时快速响应

---

**详细上报指南**: `/Users/y_jt/.openclaw/workspace-ceo/agent-report-guide.md`  
**最后更新**: 2026-03-19
