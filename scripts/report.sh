#!/bin/bash
# CEO 数据上报脚本
# 用法：./report.sh <command> [options]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AGENT_REPORT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/agent-report.sh"
AGENT_ID="ceo"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 任务开始
task-start() {
    "$AGENT_REPORT" task-start "$AGENT_ID" "$1"
}

# 进展上报
progress() {
    "$AGENT_REPORT" progress "$AGENT_ID" "$1" "$2" "$3"
}

# 日志上报
log() {
    "$AGENT_REPORT" log "$AGENT_ID" "$1" "$2"
}

# 交互上报
interaction() {
    "$AGENT_REPORT" interaction "$AGENT_ID" "$1" "$2"
}

# 阻塞上报
block() {
    "$AGENT_REPORT" block "$AGENT_ID" "$1" "$2"
}

# 任务完成
complete() {
    "$AGENT_REPORT" complete "$AGENT_ID" "$1" "$2" "$3"
}

# 显示帮助
show-help() {
    cat << EOF
CEO 数据上报脚本

用法：$0 <command> [options]

命令:
  task-start "任务名"                    任务开始
  progress "当前工作" "计划" 进度         进展上报
  log LEVEL "消息"                       日志上报
  interaction USER "内容"                交互上报
  block "原因" "解决方案"                阻塞上报
  complete "任务名" "输出" "摘要"         任务完成

示例:
  $0 task-start "TaskBoard 项目"
  $0 progress "产品原型已验收" "规划✅|执行🔄|验收" 50
  $0 log INFO "确认使用 Spring Boot + Vue3"
  $0 complete "TaskBoard 项目" "/项目总结.md" "完成"
EOF
}

# 主程序
case "$1" in
    task-start|progress|log|interaction|block|complete)
        "$@"
        ;;
    help|--help|-h)
        show-help
        ;;
    *)
        echo -e "${RED}未知命令：$1${NC}"
        show-help
        exit 1
        ;;
esac
