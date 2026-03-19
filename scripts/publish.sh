#!/bin/bash
# Cyble CEO Dashboard — 提交并推送到 GitHub
# 用法：./scripts/publish.sh [commit message]
# 示例：./scripts/publish.sh "fix: 修复任务归档接口"
#       ./scripts/publish.sh  （不传参数则交互式输入）

set -e

CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 定位项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(dirname "$SCRIPT_DIR")"
cd "$REPO_DIR"

echo -e "${CYAN}📦 Cyble CEO Dashboard — GitHub 发布${NC}"
echo ""

# 1. 检查 git 状态
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${RED}❌ 当前目录不是 git 仓库${NC}"
    exit 1
fi

# 2. 同步最新的 agent 配置（从本地 workspace 到仓库）
echo -e "${YELLOW}🔄 同步 Agent 配置模板...${NC}"

OPENCLAW_DIR="${OPENCLAW_HOME:-$HOME/.openclaw}"
SYNC_COUNT=0

for agent in ceo architect backend-dev frontend-dev product-designer qa-engineer devops-engineer; do
    ws="$OPENCLAW_DIR/workspace-$agent"
    tpl="$REPO_DIR/openclaw-config/templates/$agent"
    
    if [ -d "$ws" ] && [ -d "$tpl" ]; then
        for f in AGENTS.md SOUL.md HEARTBEAT.md; do
            src="$ws/$f"
            dst="$tpl/$f"
            if [ -f "$src" ] && [ -f "$dst" ]; then
                if ! diff -q "$src" "$dst" > /dev/null 2>&1; then
                    cp "$src" "$dst"
                    SYNC_COUNT=$((SYNC_COUNT + 1))
                    echo "  ✅ $agent/$f 已同步"
                fi
            fi
        done
    fi
done

if [ "$SYNC_COUNT" -eq 0 ]; then
    echo "  (无需同步)"
fi

# 3. 安全检查：扫描敏感信息
echo ""
echo -e "${YELLOW}🔍 安全检查...${NC}"

ISSUES=0

# 检查硬编码路径
HARDCODED=$(grep -rn "/Users/\|/home/.*\." "$REPO_DIR" \
    --include="*.md" --include="*.sh" --include="*.java" \
    --include="*.properties" --include="*.yml" --include="*.vue" \
    --include="*.js" --include="*.json" \
    2>/dev/null | grep -v ".git/" | grep -v "node_modules/" | grep -v "publish.sh" || true)

if [ -n "$HARDCODED" ]; then
    echo -e "${RED}⚠️  发现硬编码路径:${NC}"
    echo "$HARDCODED" | head -5
    ISSUES=$((ISSUES + 1))
fi

# 检查明文密码
SECRETS=$(grep -rn "password.*=.*[a-zA-Z0-9]" "$REPO_DIR" \
    --include="*.properties" --include="*.yml" \
    2>/dev/null | grep -v ".git/" | grep -v "\${" | grep -v "example" || true)

if [ -n "$SECRETS" ]; then
    echo -e "${RED}⚠️  发现可能的明文密码:${NC}"
    echo "$SECRETS" | head -5
    ISSUES=$((ISSUES + 1))
fi

if [ "$ISSUES" -gt 0 ]; then
    echo ""
    read -p "发现 $ISSUES 个潜在问题，是否继续提交？(y/N) " CONFIRM
    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
        echo "已取消"
        exit 1
    fi
else
    echo "  ✅ 无敏感信息"
fi

# 4. 查看变更
echo ""
echo -e "${YELLOW}📋 变更文件:${NC}"
git add -A
CHANGES=$(git diff --cached --stat)

if [ -z "$CHANGES" ]; then
    echo "  (无变更，无需提交)"
    exit 0
fi

echo "$CHANGES"

# 5. 获取 commit message
echo ""
MSG="$1"
if [ -z "$MSG" ]; then
    read -p "📝 Commit message: " MSG
    if [ -z "$MSG" ]; then
        echo -e "${RED}❌ 需要 commit message${NC}"
        exit 1
    fi
fi

# 6. 提交并推送
echo ""
echo -e "${YELLOW}🚀 提交并推送...${NC}"
git commit -m "$MSG"
git push

echo ""
echo -e "${GREEN}✅ 发布成功！${NC}"
echo -e "   仓库: $(git remote get-url origin)"
echo -e "   提交: $(git log --oneline -1)"
