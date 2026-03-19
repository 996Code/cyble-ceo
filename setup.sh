#!/bin/bash
# Cyble CEO Dashboard — 一键环境搭建脚本
# 由 OpenClaw 多 Agent 协作自主开发

set -e

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════╗"
echo "║       🤖 Cyble CEO Dashboard Setup          ║"
echo "║    OpenClaw Multi-Agent Collaboration        ║"
echo "╚══════════════════════════════════════════════╝"
echo -e "${NC}"

# ========== 1. 检查 Docker ==========
echo -e "${YELLOW}[1/5] 检查 Docker 环境...${NC}"

if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker 未安装${NC}"
    echo "   请先安装 Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo -e "${RED}❌ Docker 未启动${NC}"
    echo "   请先启��� Docker Desktop"
    exit 1
fi

if ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Docker Compose 未安装${NC}"
    echo "   请安装 Docker Compose v2+: https://docs.docker.com/compose/install/"
    exit 1
fi

DOCKER_VERSION=$(docker version --format '{{.Server.Version}}' 2>/dev/null || echo "unknown")
COMPOSE_VERSION=$(docker compose version --short 2>/dev/null || echo "unknown")
echo -e "${GREEN}✅ Docker ${DOCKER_VERSION} + Compose ${COMPOSE_VERSION}${NC}"

# ========== 2. 检查 OpenClaw ==========
echo -e "${YELLOW}[2/5] 检查 OpenClaw 环境...${NC}"

OPENCLAW_HOME=""
AGENTS_PATH=""

# 自动发现 OpenClaw agents 目录
SEARCH_PATHS=(
    "$HOME/.openclaw/agents"
    "$HOME/.config/openclaw/agents"
    "/opt/openclaw/agents"
)

for p in "${SEARCH_PATHS[@]}"; do
    if [ -d "$p" ]; then
        AGENTS_PATH="$p"
        OPENCLAW_HOME="$(dirname "$p")"
        break
    fi
done

if [ -z "$AGENTS_PATH" ]; then
    echo -e "${YELLOW}⚠️  未自动检测到 OpenClaw agents 目录${NC}"
    echo ""
    read -p "请输入 OpenClaw agents 目录路径（例如 /home/user/.openclaw/agents）: " AGENTS_PATH
    
    if [ ! -d "$AGENTS_PATH" ]; then
        echo -e "${RED}❌ 目录不存在: $AGENTS_PATH${NC}"
        echo "   请确认 OpenClaw 已安装并配置了 Agent"
        echo "   安装 OpenClaw: https://github.com/nicepkg/openclaw"
        exit 1
    fi
fi

# 统计 agent 数量
AGENT_COUNT=$(ls -d "$AGENTS_PATH"/*/ 2>/dev/null | wc -l | tr -d ' ')
echo -e "${GREEN}✅ OpenClaw agents 目录: $AGENTS_PATH （${AGENT_COUNT} 个 Agent）${NC}"

# ========== 3. 生成 .env ==========
echo -e "${YELLOW}[3/5] 生成配置文件...${NC}"

# 端口选择
DEFAULT_PORT=80
read -p "Dashboard 访问端口 [${DEFAULT_PORT}]: " PORT
PORT=${PORT:-$DEFAULT_PORT}

# 检查端口占用
if lsof -i ":$PORT" &>/dev/null 2>&1 || ss -tln 2>/dev/null | grep -q ":$PORT "; then
    echo -e "${YELLOW}⚠️  端口 $PORT 已被占用${NC}"
    read -p "请输入其他端口: " PORT
fi

cat > .env << EOF
# Cyble CEO Dashboard 配置
# 自动生成于 $(date '+%Y-%m-%d %H:%M:%S')

# OpenClaw agents 目录路径
OPENCLAW_AGENTS_PATH=$AGENTS_PATH

# H2 数据库密码（可选，留空即可）
DB_PASSWORD=

# 前端访问端口
PORT=$PORT
EOF

echo -e "${GREEN}✅ .env 已生成${NC}"

# ========== 4. 构建并启动 ==========
echo -e "${YELLOW}[4/5] 构建并启动服务（首次约 2-3 分钟）...${NC}"
echo ""

docker compose build --no-cache 2>&1 | while IFS= read -r line; do
    # 只显示关键进度
    if echo "$line" | grep -qE "^(#[0-9]+ (DONE|exporting)|Step|Successfully|Building)"; then
        echo "  $line"
    fi
done

docker compose up -d 2>&1

# ========== 5. 等待健康检查 ==========
echo -e "${YELLOW}[5/5] 等待服务启动...${NC}"

MAX_WAIT=90
WAIT=0
while [ $WAIT -lt $MAX_WAIT ]; do
    STATUS=$(docker inspect --format='{{.State.Health.Status}}' cyble-ceo-api 2>/dev/null || echo "starting")
    if [ "$STATUS" = "healthy" ]; then
        break
    fi
    echo -ne "  ⏳ 等待后端启动... (${WAIT}s)\r"
    sleep 3
    WAIT=$((WAIT + 3))
done

echo ""

# 最终检查
API_STATUS=$(docker inspect --format='{{.State.Health.Status}}' cyble-ceo-api 2>/dev/null || echo "unknown")
FRONTEND_STATUS=$(docker inspect --format='{{.State.Status}}' cyble-ceo-frontend 2>/dev/null || echo "unknown")

if [ "$API_STATUS" = "healthy" ] && [ "$FRONTEND_STATUS" = "running" ]; then
    echo -e "${GREEN}"
    echo "╔══════════════════════════════════════════════╗"
    echo "║         🎉 部署成功！                        ║"
    echo "╠══════════════════════════════════════════════╣"
    echo "║                                              ║"
    echo "║  Dashboard:  http://localhost:${PORT}             ║"
    echo "║  API:        http://localhost:${PORT}/api/v1      ║"
    echo "║  Agent 数:   ${AGENT_COUNT}                              ║"
    echo "║                                              ║"
    echo "╠══════════════════════════════════════════════╣"
    echo "║  常用命令:                                    ║"
    echo "║  查看日志:   docker compose logs -f          ║"
    echo "║  停止服务:   docker compose down             ║"
    echo "║  重启服务:   docker compose restart          ║"
    echo "╚══════════════════════════════════════════════╝"
    echo -e "${NC}"
else
    echo -e "${RED}"
    echo "╔══════════════════════════════════════════════╗"
    echo "║  ⚠️  服务启动可能有问题                       ║"
    echo "╠══════════════════════════════════════════════╣"
    echo "║  后端状态: $API_STATUS"
    echo "║  前端状态: $FRONTEND_STATUS"
    echo "║                                              ║"
    echo "║  查看日志排查:                                ║"
    echo "║  docker compose logs api                     ║"
    echo "║  docker compose logs frontend                ║"
    echo "╚══════════════════════════════════════════════╝"
    echo -e "${NC}"
    exit 1
fi
