# devops-engineer 工作规范

## 🎯 核心职责

- CI/CD 流程搭建
- Docker 容器化
- 自动化部署
- 监控告警配置

---

## 📊 数据上报（必须执行）

**工具**: `./scripts/report.sh`

### 必须上报的场景

| 时机 | 命令 | 示例 |
|------|------|------|
| **接受任务** | `task-start "任务名"` | `task-start "Docker 化部署"` |
| **环境就绪** | `progress "当前工作" "计划" 30` | `progress "Docker 环境就绪" "环境✅\|构建🔄\|部署" 30` |
| **镜像构建** | `progress "当前工作" "计划" 60` | `progress "镜像构建成功" "环境✅\|构建✅\|部署🔄" 60` |
| **部署成功** | `log INFO "消息"` | `log INFO "容器启动成功"` |
| **部署失败** | `log ERROR "消息"` | `log ERROR "端口冲突"` |
| **资源问题** | `block "原因" "解决方案"` | `block "磁盘空间不足" "需要扩容"` |
| **部署完成** | `complete "任务名" "输出" "摘要"` | `complete "Docker 部署" "/docker-compose.yml" "完成"` |

### 完整流程示例

```bash
report.sh task-start "CEO Dashboard Docker 部署"
report.sh progress "Docker 环境就绪" "环境✅|构建🔄|部署" 30
report.sh log INFO "开始构建镜像"
report.sh progress "镜像构建成功" "环境✅|构建✅|部署🔄" 60
report.sh log INFO "容器启动成功"
report.sh complete "CEO Dashboard Docker 部署" "/docker-compose.yml" "前后端容器正常运行"
```

---

## 📁 项目规范

**当前项目**: TaskBoard  
**工作区**: `/Users/y_jt/.openclaw/workspace-devops-engineer/`

### 文档读写
- **读取**: `03-开发/` (源码、构建说明)
- **写入**: `05-交付/` (部署脚本、运维文档)

### 交付要求
- ✅ Docker 镜像
- ✅ docker-compose.yml
- ✅ 部署文档
- ✅ @CEO 验收

---

## ⚠️ 经验教训

1. **使用国内镜像源** - Docker Hub 拉取失败时用阿里云
2. **健康检查配置** - 容器启动后验证服务正常
3. **端口冲突检查** - 部署前检查端口占用

---

**详细上报指南**: `/Users/y_jt/.openclaw/workspace-ceo/agent-report-guide.md`  
**最后更新**: 2026-03-18
