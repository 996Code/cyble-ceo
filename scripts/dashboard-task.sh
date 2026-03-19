#!/bin/bash
# CEO Dashboard 任务管理脚本
# 用法：
#   ./dashboard-task.sh create <task-id> <title> <assignee> [description]
#   ./dashboard-task.sh start <task-id>
#   ./dashboard-task.sh done <task-id> [summary]
#   ./dashboard-task.sh progress <task-id> <progress-text> [percent]
#   ./dashboard-task.sh block <task-id> <reason>
#   ./dashboard-task.sh list [status]

API="http://localhost/api/v1/tasks"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

create() {
    local id="$1" title="$2" assignee="$3" desc="${4:-}" sessionKey="${5:-}"
    # 创建任务
    local body="{\"id\":\"$id\",\"title\":\"$title\",\"assignee\":\"$assignee\",\"description\":\"$desc\",\"creator\":\"ceo\""
    if [ -n "$sessionKey" ]; then
        body="$body,\"sessionKey\":\"$sessionKey\""
    fi
    body="$body}"
    local resp
    resp=$(curl -s --noproxy '*' -X POST "$API" \
        -H "Content-Type: application/json" \
        -d "$body")
    local code
    code=$(echo "$resp" | python3 -c "import json,sys; print(json.load(sys.stdin).get('code',0))" 2>/dev/null)
    if [ "$code" != "200" ]; then
        echo -e "${RED}❌ 创建失败: $resp${NC}"
        return 1
    fi
    # 自动流转: CREATED → ASSIGNED → DOING
    curl -s --noproxy '*' -X PUT "$API/$id/state" \
        -H "Content-Type: application/json" \
        -d '{"status":"ASSIGNED","remark":"CEO 分派"}' > /dev/null 2>&1
    curl -s --noproxy '*' -X PUT "$API/$id/state" \
        -H "Content-Type: application/json" \
        -d '{"status":"DOING","remark":"开始执行"}' > /dev/null 2>&1
    echo -e "${GREEN}✅ 任务创建并启动: $id → $title (分配给 $assignee)${NC}"
}

start() {
    local id="$1"
    local resp
    resp=$(curl -s --noproxy '*' -X PUT "$API/$id/state" \
        -H "Content-Type: application/json" \
        -d '{"status":"DOING","remark":"任务开始执行"}')
    local code
    code=$(echo "$resp" | python3 -c "import json,sys; print(json.load(sys.stdin).get('code',0))" 2>/dev/null)
    if [ "$code" = "200" ]; then
        echo -e "${GREEN}✅ 任务已开始: $id${NC}"
    else
        echo -e "${RED}❌ 启动失败: $resp${NC}"
    fi
}

done_task() {
    local id="$1" summary="${2:-已完成}"
    # 先尝试直接完成（DOING → DONE）
    local resp
    resp=$(curl -s --noproxy '*' -X PUT "$API/$id/done" \
        -H "Content-Type: application/json" \
        -d "{\"summary\":\"$summary\"}")
    local code
    code=$(echo "$resp" | python3 -c "import json,sys; print(json.load(sys.stdin).get('code',0))" 2>/dev/null)
    if [ "$code" = "200" ]; then
        echo -e "${GREEN}✅ 任务已完成: $id${NC}"
    else
        # 如果不在 DOING 状态，尝试 REVIEW → DONE
        resp=$(curl -s --noproxy '*' -X PUT "$API/$id/state" \
            -H "Content-Type: application/json" \
            -d '{"status":"DONE","remark":"'"$summary"'"}')
        code=$(echo "$resp" | python3 -c "import json,sys; print(json.load(sys.stdin).get('code',0))" 2>/dev/null)
        if [ "$code" = "200" ]; then
            echo -e "${GREEN}✅ 任务已完成: $id${NC}"
        else
            echo -e "${RED}❌ 完成失败: $resp${NC}"
        fi
    fi
}

progress() {
    local id="$1" text="$2" percent="${3:-}"
    local body="{\"currentProgress\":\"$text\""
    if [ -n "$percent" ]; then
        body="$body,\"progressPercent\":$percent"
    fi
    body="$body}"
    local resp
    resp=$(curl -s --noproxy '*' -X PUT "$API/$id/progress" \
        -H "Content-Type: application/json" \
        -d "$body")
    local code
    code=$(echo "$resp" | python3 -c "import json,sys; print(json.load(sys.stdin).get('code',0))" 2>/dev/null)
    if [ "$code" = "200" ]; then
        echo -e "${GREEN}✅ 进度已更新: $id${NC}"
    else
        echo -e "${RED}❌ 更新失败: $resp${NC}"
    fi
}

block() {
    local id="$1" reason="$2"
    local resp
    resp=$(curl -s --noproxy '*' -X PUT "$API/$id/block" \
        -H "Content-Type: application/json" \
        -d "{\"reason\":\"$reason\"}")
    local code
    code=$(echo "$resp" | python3 -c "import json,sys; print(json.load(sys.stdin).get('code',0))" 2>/dev/null)
    if [ "$code" = "200" ]; then
        echo -e "${GREEN}✅ 任务已标记阻塞: $id${NC}"
    else
        echo -e "${RED}❌ 标记失败: $resp${NC}"
    fi
}

list_tasks() {
    local status="${1:-}"
    local url="$API"
    if [ -n "$status" ]; then
        url="$url?status=$status"
    fi
    curl -s --noproxy '*' "$url" | python3 -c "
import json,sys
resp = json.load(sys.stdin)
data = resp.get('data', resp) if isinstance(resp, dict) else resp
tasks = data if isinstance(data, list) else []
if not tasks:
    print('  (无任务)')
else:
    for t in tasks:
        print(f\"  {t.get('status','?'):8s} | {t.get('id','')[:20]:20s} | {t.get('assignee',''):15s} | {t.get('title','')[:40]}\")
" 2>/dev/null
}

show_help() {
    cat << 'EOF'
CEO Dashboard 任务管理

用法：
  dashboard-task.sh create <id> <title> <assignee> [description]
  dashboard-task.sh start <id>
  dashboard-task.sh done <id> [summary]
  dashboard-task.sh progress <id> <progress-text> [percent]
  dashboard-task.sh block <id> <reason>
  dashboard-task.sh list [status]

示例：
  dashboard-task.sh create T005 "前端优化" frontend-dev "修复Header问题"
  dashboard-task.sh start T005
  dashboard-task.sh progress T005 "修复Header完成" 80
  dashboard-task.sh done T005 "全部修复完成"
  dashboard-task.sh list DOING
EOF
}

case "$1" in
    create)   shift; create "$@" ;;
    start)    shift; start "$@" ;;
    done)     shift; done_task "$@" ;;
    progress) shift; progress "$@" ;;
    block)    shift; block "$@" ;;
    list)     shift; list_tasks "$@" ;;
    help|--help|-h) show_help ;;
    *)
        echo -e "${RED}未知命令：$1${NC}"
        show_help
        exit 1
        ;;
esac
