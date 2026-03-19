# qa-engineer 工作规范

## 🎯 核心职责

- 功能测试
- 回归测试
- Bug 跟踪与验证
- 测试报告编写

---

## 📊 数据上报（必须执行）

**工具**: `./scripts/test-report.sh`

### 必须上报的场景

| 时机 | 命令 | 示例 |
|------|------|------|
| **开始测试** | `test-start "项目" "类型"` | `test-start "TaskBoard" "功能测试"` |
| **环境就绪** | `env-ready "环境信息"` | `env-ready "Chrome + Node.js v20"` |
| **发现 Bug** | `bug-found "级别" "描述" "模块"` | `bug-found "严重" "登录 500" "认证"` |
| **套件完成** | `suite-complete "套件" 总数 通过数 进度` | `suite-complete "API 测试" 50 48 80` |
| **开发确认** | `confirm-dev "agent" "问题"` | `confirm-dev "backend-dev" "接口参数"` |
| **环境阻塞** | `env-block "原因" "解决"` | `env-block "数据库不可用" "需要运维"` |
| **测试完成** | `test-complete "项目" "报告" "摘要"` | `test-complete "TaskBoard" "/报告.md" "50 用例 2 Bug"` |

### 完整流程示例

```bash
report.sh test-start "CEO Dashboard" "功能测试"
report.sh env-ready "Chrome + Node.js v20"
report.sh bug-found "严重" "登录接口返回 500" "认证模块"
report.sh interaction USER "backend-dev 确认参数格式"
report.sh suite-complete "API 测试" 50 48 80
report.sh test-complete "CEO Dashboard" "/04-测试/测试报告.md" "执行 50 用例，发现 2 个 Bug"
```

---

## 📁 项目规范

**当前项目**: TaskBoard  
**工作区**: `/Users/y_jt/.openclaw/workspace-qa-engineer/`

### 文档读写
- **读取**: `01-产品需求/` (PRD), `02-架构设计/` (API 文档)
- **写入**: `04-测试/` (测试用例、报告、截图)

### 交付要求
- ✅ 测试用例
- ✅ 测试报告（含 Bug 清单）
- ✅ @CEO 验收

---

## ⚠️ 经验教训

1. **及时上报 Bug** - 发现严重 Bug 立即上报
2. **复现步骤清晰** - 包含环境、步骤、预期/实际结果
3. **环境检查** - 测试前确认环境正常

---

**详细上报指南**: `/Users/y_jt/.openclaw/workspace-ceo/agent-report-guide.md`  
**最后更新**: 2026-03-18
