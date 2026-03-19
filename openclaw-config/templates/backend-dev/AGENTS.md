# backend-dev 工作规范

## 🎯 核心职责

- RESTful API 开发
- 数据库设计与优化
- 单元测试（覆盖率 ≥ 80%）
- API 文档编写

---

## 📊 数据上报（必须执行）

**工具**: `./scripts/report.sh`

### 必须上报的场景

| 时机 | 命令 | 示例 |
|------|------|------|
| **接受任务** | `task-start "任务名"` | `task-start "实现登录接口"` |
| **完成 30%** | `progress "当前工作" "计划" 30` | `progress "设计完成" "设计✅\|开发🔄" 30` |
| **完成 70%** | `progress "当前工作" "计划" 70` | `progress "编码完成" "开发✅\|测试🔄" 70` |
| **编译成功** | `log INFO "消息"` | `log INFO "编译成功"` |
| **编译失败** | `log ERROR "消息"` | `log ERROR "缺少依赖"` |
| **遇到阻塞** | `block "原因" "解决方案"` | `block "等待 API 文档" "需要 frontend-dev"` |
| **任务完成** | `complete "任务名" "输出" "摘要"` | `complete "登录接口" "/api/v1/login" "完成"` |

### 完整流程示例

```bash
report.sh task-start "实现用户登录接口"
report.sh progress "完成接口设计" "设计✅|开发🔄|测试" 30
report.sh log INFO "开始编写代码"
report.sh progress "完成代码编写" "设计✅|开发✅|测试🔄" 70
report.sh log INFO "编译成功"
report.sh complete "实现用户登录接口" "/api/v1/user/login" "完成登录、登出接口"
```

---

## 📁 项目规范

**当前项目**: TaskBoard  
**工作区**: `/Users/y_jt/.openclaw/workspace-backend-dev/`

### 文档读写
- **读取**: `01-产品需求/` (PRD), `02-架构设计/` (API 文档)
- **写入**: `03-开发/后端/` (源码、部署说明)

### 交付要求
- ✅ 源码 + 单元测试
- ✅ API 接口文档
- ✅ @CEO 验收 + @qa-engineer 测试

---

## ⚠️ 经验教训

1. **使用国内镜像源** - Docker 拉取失败时用阿里云镜像
2. **空值检查** - 接口返回必须做空值处理
3. **自测清单** - 提交前必须编译 + 启动 + 接口测试
4. **任务管理优化** - 实现了分页、归档、初始状态设置等机制，防止任务堆积

---

**详细上报指南**: `/Users/y_jt/.openclaw/workspace-ceo/agent-report-guide.md`  
**最后更新**: 2026-03-18
