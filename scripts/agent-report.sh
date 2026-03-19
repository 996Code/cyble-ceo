#!/bin/bash
# Agent 数据上报 CLI 工具
# 用法：./agent-report.sh <command> [options]

API_URL="${DASHBOARD_API_URL:-http://localhost:8080/api/v1/dashboard/agent/report}"

# 颜色输出 - 如果设置了 NO_COLOR 环境变量则禁用颜色
if [ -z "$NO_COLOR" ]; then
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    NC='\033[0m' # No Color
else
    RED=''
    GREEN=''
    YELLOW=''
    NC=''
fi

# 获取当前时间戳
get_timestamp() {
    date -u +"%Y-%m-%dT%H:%M:%S"
}

# 发送上报请求
send_report() {
    local data="$1"
    local response
    response=$(curl -s -X POST "$API_URL" \
        -H "Content-Type: application/json" \
        -d "$data")
    
    if echo "$response" | grep -q '"success":true\|"code":200'; then
        echo -e "${GREEN}✅ 上报成功${NC}"
        return 0
    else
        echo -e "${RED}❌ 上报失败：$response${NC}" >&2
        return 1
    fi
}

# 任务开始上报
task_start() {
    local agent="$1"
    local task="$2"
    local workspace="${3:-workspace-$agent}"
    
    if [ -z "$agent" ] || [ -z "$task" ]; then
        echo -e "${RED}用法：$0 task-start <agent-id> <task-name> [workspace]${NC}"
        exit 1
    fi
    
    local data="{
        \"type\": \"TASK_START\",
        \"agentId\": \"$agent\",
        \"workspace\": \"$workspace\",
        \"taskName\": \"$task\",
        \"startTime\": \"$(get_timestamp)\"
    }"
    
    echo -e "${YELLOW}📝 任务开始上报：$agent - $task${NC}"
    send_report "$data"
}

# 进展上报
progress() {
    local agent="$1"
    local current="$2"
    local plan="$3"
    local prog="${4:-0}"
    
    if [ -z "$agent" ] || [ -z "$current" ]; then
        echo -e "${RED}用法：$0 progress <agent-id> <current> [plan] [progress]${NC}"
        exit 1
    fi
    
    local data="{
        \"type\": \"PROGRESS\",
        \"agentId\": \"$agent\",
        \"current\": \"$current\",
        \"plan\": \"$plan\",
        \"progress\": $prog,
        \"timestamp\": \"$(get_timestamp)\"
    }"
    
    echo -e "${YELLOW}📊 进展上报：$agent - $current ($prog%)${NC}"
    send_report "$data"
}

# 日志上报
log() {
    local agent="$1"
    local level="${2:-INFO}"
    local message="$3"
    
    if [ -z "$agent" ] || [ -z "$message" ]; then
        echo -e "${RED}用法：$0 log <agent-id> [LEVEL] <message>${NC}"
        exit 1
    fi
    
    local data="{
        \"type\": \"LOG\",
        \"agentId\": \"$agent\",
        \"level\": \"$level\",
        \"message\": \"$message\",
        \"timestamp\": \"$(get_timestamp)\"
    }"
    
    echo -e "${YELLOW}📋 日志上报：$agent [$level] $message${NC}"
    send_report "$data"
}

# 交互记录上报
interaction() {
    local agent="$1"
    local role="${2:-USER}"
    local content="$3"
    
    if [ -z "$agent" ] || [ -z "$content" ]; then
        echo -e "${RED}用法：$0 interaction <agent-id> [ROLE] <content>${NC}"
        exit 1
    fi
    
    local data="{
        \"type\": \"INTERACTION\",
        \"agentId\": \"$agent\",
        \"role\": \"$role\",
        \"content\": \"$content\",
        \"timestamp\": \"$(get_timestamp)\"
    }"
    
    echo -e "${YELLOW}💬 交互上报：$agent [$role] $content${NC}"
    send_report "$data"
}

# 任务完成上报
complete() {
    local agent="$1"
    local task="$2"
    local output="$3"
    local summary="$4"
    
    if [ -z "$agent" ] || [ -z "$task" ]; then
        echo -e "${RED}用法：$0 complete <agent-id> <task-name> [output] [summary]${NC}"
        exit 1
    fi
    
    local data="{
        \"type\": \"TASK_COMPLETE\",
        \"agentId\": \"$agent\",
        \"taskName\": \"$task\",
        \"output\": \"$output\",
        \"summary\": \"$summary\",
        \"endTime\": \"$(get_timestamp)\"
    }"
    
    echo -e "${YELLOW}✅ 任务完成上报：$agent - $task${NC}"
    send_report "$data"
}

# 阻塞上报
block() {
    local agent="$1"
    local reason="$2"
    local expected="$3"
    
    if [ -z "$agent" ] || [ -z "$reason" ]; then
        echo -e "${RED}用法：$0 block <agent-id> <reason> [expected-resolve]${NC}"
        exit 1
    fi
    
    local data="{
        \"type\": \"BLOCK\",
        \"agentId\": \"$agent\",
        \"reason\": \"$reason\",
        \"expectedResolve\": \"$expected\",
        \"timestamp\": \"$(get_timestamp)\"
    }"
    
    echo -e "${RED}⏸️ 阻塞上报：$agent - $reason${NC}"
    send_report "$data"
}

# 心跳上报
heartbeat() {
    local agent="$1"
    local status="${2:-IDLE}"
    
    if [ -z "$agent" ]; then
        echo -e "${RED}用法：$0 heartbeat <agent-id> [status]${NC}"
        exit 1
    fi
    
    local data="{
        \"type\": \"HEARTBEAT\",
        \"agentId\": \"$agent\",
        \"status\": \"$status\",
        \"timestamp\": \"$(get_timestamp)\"
    }"
    
    send_report "$data"
}

# 显示帮助
show_help() {
    cat << EOF
Agent 数据上报 CLI 工具

用法：$0 <command> [options]

命令:
  task-start    <agent> <task> [workspace]     任务开始上报
  progress      <agent> <current> [plan] [progress]  进展上报
  log           <agent> [LEVEL] <message>      日志上报
  interaction   <agent> [ROLE] <content>       交互记录上报
  complete      <agent> <task> [output] [summary]  任务完成上报
  block         <agent> <reason> [expected]    阻塞上报
  heartbeat     <agent> [status]               心跳上报

示例:
  $0 task-start backend-dev "API 开发"
  $0 progress backend-dev "开发中" "需求✅|开发🔄|测试" 60
  $0 log backend-dev INFO "完成接口开发"
  $0 interaction backend-dev USER "用户要求添加验证"
  $0 complete backend-dev "API 开发" "/workspace/api" "完成所有接口"
  $0 block backend-dev "等待前端文档" "需要 frontend-dev 提供 API"
  $0 heartbeat backend-dev BUSY

环境变量:
  DASHBOARD_API_URL  Dashboard API 地址 (默认：http://localhost:8080/api/v1/dashboard/agent/report)

EOF
}

# 主程序
case "$1" in
    task-start)
        task_start "$2" "$3" "$4"
        ;;
    progress)
        progress "$2" "$3" "$4" "$5"
        ;;
    log)
        log "$2" "$3" "$4"
        ;;
    interaction)
        interaction "$2" "$3" "$4"
        ;;
    complete)
        complete "$2" "$3" "$4" "$5"
        ;;
    block)
        block "$2" "$3" "$4"
        ;;
    heartbeat)
        heartbeat "$2" "$3"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo -e "${RED}未知命令：$1${NC}"
        show_help
        exit 1
        ;;
esac
