# Cyble CEO Dashboard

> 🤖 OpenClaw 多 Agent 协作监控看板 —— 像 CEO 一样掌控 AI 团队全局

Cyble CEO Dashboard 是一个为 [OpenClaw](https://github.com/nicepkg/openclaw) 多 Agent 协作场景设计的实时监控面板。它能展示各 Agent 的任务状态、工作进度、日志和交互记录，让你一目了然地掌控 AI 团队的运作。

![Tech Stack](https://img.shields.io/badge/Vue_3-4FC08D?style=flat&logo=vue.js&logoColor=white)
![Tech Stack](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![Tech Stack](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)

## ✨ 功能

- 📊 **全局概览** — 一页总览所有 Agent 运行状态和关键指标
- 🤖 **Agent 详情** — 查看每个 Agent 的实时状态、当前任务和历史记录
- 📋 **任务看板** — 可视化任务流转（待分派 → 执行中 → 待审查 → 已完成）
- 📝 **日志查看器** — 集中浏览 Agent 上报的日志
- 💬 **交互记录** — 追踪 Agent 之间的协作交互
- 🚨 **告警横幅** — 自动检测异常 Agent 并高亮展示
- 📈 **进度追踪** — 实时进度条展示任务完成度
- 🗄️ **任务归档** — 已完成任务自动归档，看板保持清爽
- 📄 **分页 & 时间过滤** — 大量数据轻松管理

## 🛠 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + Vue Router + Vite |
| 后端 | Spring Boot 3 + Spring Data JPA |
| 数据库 | H2（嵌入式，零配置） |
| 部署 | Docker + Docker Compose + Nginx |

## 🚀 快速开始

### 前置条件

- [Docker](https://docs.docker.com/get-docker/) 20.10+
- [Docker Compose](https://docs.docker.com/compose/install/) v2+
- [OpenClaw](https://github.com/nicepkg/openclaw) 已安装并配置好 Agent

### 一键启动

```bash
git clone https://github.com/996Code/cyble-ceo.git
cd cyble-ceo

# 设置 OpenClaw agents 目录路径
export OPENCLAW_AGENTS_PATH=/path/to/your/.openclaw/agents

# 启动（首次会自动构建镜像，约 2-3 分钟）
docker compose up -d
```

启动后访问 **http://localhost** 即可看到 Dashboard。

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `OPENCLAW_AGENTS_PATH` | OpenClaw agents 目录路径 | `./data/agents` |
| `DB_PASSWORD` | H2 数据库密码（可选） | _(空)_ |
| `PORT` | 前端访问端口 | `80` |

### 使用 `.env` 文件

```bash
cp .env.example .env
# 编辑 .env 填入你的配置
```

## 📁 项目结构

```
cyble-ceo/
├── docker-compose.yml          # 一键部署编排文件
├── backend/                    # Spring Boot 后端
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── frontend/                   # Vue 3 前端
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── src/
├── scripts/                    # Agent 上报脚本
│   ├── agent-report.sh         # Agent 数据上报 CLI
│   ├── dashboard-task.sh       # 任务管理 CLI
│   └── report.sh               # CEO 快捷上报入口
├── openclaw-config/            # OpenClaw Agent 配置模板
│   └── templates/              # 各角色的 AGENTS.md / SOUL.md / HEARTBEAT.md
└── docs/                       # API 文档
```

## 🤖 OpenClaw Agent 配置

Dashboard 需要配合 OpenClaw 的多 Agent 体系使用。项目提供了 7 个预设角色的配置模板：

| 角色 | 说明 |
|------|------|
| `ceo` | 最高决策者，分派任务、验收交付物 |
| `architect` | 架构师，负责技术选型和架构设计 |
| `backend-dev` | 后端开发，实现 API 和业务逻辑 |
| `frontend-dev` | 前端开发，实现 UI 和交互 |
| `product-designer` | 产品设计师，负责需求分析和原型设计 |
| `qa-engineer` | 测试工程师，负责质量保障 |
| `devops-engineer` | 运维工程师，负责部署和运维 |

### 使用配置模板

将模板复制到对应 Agent 的 workspace 目录：

```bash
# 假设你的 OpenClaw workspace 在 ~/.openclaw/
cp -r openclaw-config/templates/ceo/* ~/.openclaw/workspace-ceo/
cp -r openclaw-config/templates/backend-dev/* ~/.openclaw/workspace-backend-dev/
# ... 其他角色同理
```

## 📡 Agent 数据上报

Agent 通过脚本向 Dashboard 上报状态。上报脚本在 `scripts/` 目录下。

### 任务管理

```bash
# 创建任务（自动流转到执行中）
./scripts/dashboard-task.sh create "T-001" "实现登录功能" "backend-dev"

# 更新进度
./scripts/dashboard-task.sh progress "T-001" "接口开发完成" 80

# 完成任务
./scripts/dashboard-task.sh done "T-001" "登录功能已上线"

# 查看任务列表
./scripts/dashboard-task.sh list
./scripts/dashboard-task.sh list DOING
```

### 状态上报

```bash
# 上报心跳
./scripts/agent-report.sh heartbeat "backend-dev"

# 上报日志
./scripts/agent-report.sh log "backend-dev" "INFO" "数据库迁移完成"

# 上报进度
./scripts/agent-report.sh progress "backend-dev" "API 开发中" "接口设计完成" 60
```

## 🔧 本地开发

### 后端

```bash
cd backend
mvn spring-boot:run
# API 运行在 http://localhost:8080
```

### 前端

```bash
cd frontend
npm install
npm run dev
# 开发服务器运行在 http://localhost:3000（自动代理 API 到 8080）
```

## 📡 API 概览

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/v1/tasks` | 任务列表（支持分页、时间过滤） |
| `POST` | `/api/v1/tasks` | 创建任务（支持 `initialStatus` 一步到位） |
| `GET` | `/api/v1/tasks/{id}` | 任务详情（含流转记录和子任务） |
| `PUT` | `/api/v1/tasks/{id}/state` | 更新任务状态 |
| `PUT` | `/api/v1/tasks/{id}/progress` | 更新任务进度 |
| `PUT` | `/api/v1/tasks/{id}/done` | 完成任务 |
| `POST` | `/api/v1/tasks/archive` | 归档已完成任务 |
| `GET` | `/api/v1/dashboard/overview` | 全局概览 |
| `GET` | `/api/v1/dashboard/agents/{id}` | Agent 详情 |

详细 API 文档见 [docs/API_DOC.md](docs/API_DOC.md)。

## 🎨 任务状态流转

```
CREATED → ASSIGNED → DOING → REVIEW → DONE
                       ↓        ↓
                    BLOCKED   REJECTED → ASSIGNED
                       ↓
                     DOING
                     
任何状态 → CANCELLED
```

## 📄 License

MIT

## 🙏 致谢

- [OpenClaw](https://github.com/nicepkg/openclaw) — 强大的多 Agent 协作框架
- [Vue 3](https://vuejs.org/) + [Spring Boot](https://spring.io/projects/spring-boot) — 可靠的技术栈
