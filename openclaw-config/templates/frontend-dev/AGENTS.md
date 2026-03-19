# frontend-dev 工作规范

## 🎯 核心职责

- 前端页面开发
- 组件封装与复用
- UI 还原（像素级）
- 性能优化（首屏 < 2s）

---

## 📊 数据上报（必须执行）

**工具**: `./scripts/frontend-report.sh`

### 必须上报的场景

| 时机 | 命令 | 示例 |
|------|------|------|
| **接受任务** | `dev-start "任务名"` | `dev-start "用户管理页面"` |
| **依赖安装** | `deps-installed` | `deps-installed` |
| **组件完成** | `component-done "组件" 50` | `component-done "用户列表" 50` |
| **构建成功** | `build-success` | `build-success` |
| **构建失败** | `build-failed "错误"` | `build-failed "TypeScript 错误"` |
| **UI 问题** | `ui-issue "问题"` | `ui-issue "Safari 样式错乱"` |
| **等待设计** | `style-block "原因"` | `style-block "等待设计稿"` |
| **页面完成** | `page-complete "页面" "输出" "摘要"` | `page-complete "用户管理" "/pages/user" "完成"` |

### 完整流程示例

```bash
report.sh dev-start "用户管理页面开发"
report.sh deps-installed
report.sh component-done "用户列表组件" 50
report.sh log WARN "Safari 兼容性问题"
report.sh interaction USER "产品设计师要求调整配色"
report.sh build-success
report.sh page-complete "用户管理页面" "/pages/user-management" "完成列表、详情、编辑功能"
```

---

## 📁 项目规范

**当前项目**: TaskBoard  
**工作区**: `/Users/y_jt/.openclaw/workspace-frontend-dev/`

### 文档读写
- **读取**: `01-产品需求/` (原型、PRD)
- **写入**: `03-开发/前端/` (源码、构建产物)

### 交付要求
- ✅ 源码 + 构建产物
- ✅ 像素级还原设计稿
- ✅ @CEO 验收（UI 走查）

---

## ⚠️ 经验教训

1. **像素级还原** - 不要估算，用精确值
2. **空值检查** - 组件必须处理空数据
3. **动画适度** - 不要过度影响性能

---

**详细上报指南**: `/Users/y_jt/.openclaw/workspace-ceo/agent-report-guide.md`  
**最后更新**: 2026-03-18
