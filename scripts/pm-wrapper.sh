#!/bin/bash
# pm-wrapper.sh - Execute CCPM commands inside Docker container if available
#
# Usage: ./pm-wrapper.sh <command> [args...]
# Example: ./pm-wrapper.sh prd-new cookies-store
#          ./pm-wrapper.sh epic-oneshot cookies-store

set -e

COMMAND="${1:-help}"
shift || true
ARGS="$@"

# CCPM scripts location
CCPM_SCRIPTS=".claude/plugins/ccpm-main/ccpm/scripts/pm"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() { echo -e "${GREEN}ℹ${NC} $1"; }
echo_warn() { echo -e "${YELLOW}⚠${NC} $1"; }
echo_error() { echo -e "${RED}✖${NC} $1"; }

# Find running dev container
find_dev_container() {
    # Look for containers with "-dev" suffix (e.g., cookies-store-dev, ccpm-dev)
    docker ps --filter "name=-dev" --format "{{.Names}}" 2>/dev/null | head -1
}

# Check if gh is available on host
check_host_gh() {
    command -v gh &>/dev/null
}

# Execute command
execute_pm_command() {
    local script="${CCPM_SCRIPTS}/${COMMAND}.sh"
    
    if [[ ! -f "$script" ]]; then
        echo_error "Unknown command: $COMMAND"
        echo "Available commands: init, prd-new, prd-parse, epic-oneshot, issue-start, issue-complete, help"
        exit 1
    fi
    
    bash "$script" $ARGS
}

# Main logic
main() {
    # Strategy 1: Check for running Docker container
    CONTAINER=$(find_dev_container)
    
    if [[ -n "$CONTAINER" ]]; then
        echo_info "Found running container: $CONTAINER"
        echo_info "Executing /pm:${COMMAND} inside container..."
        echo ""
        
        docker exec -it "$CONTAINER" bash -c "cd /workspace && bash ${CCPM_SCRIPTS}/${COMMAND}.sh $ARGS"
        exit $?
    fi
    
    # Strategy 2: Check if gh is available on host
    if check_host_gh; then
        echo_info "Using host environment (gh available)"
        execute_pm_command
        exit $?
    fi
    
    # Strategy 3: Neither available - guide user
    echo_error "Cannot execute CCPM commands:"
    echo ""
    echo "  • Docker dev container is not running"
    echo "  • GitHub CLI (gh) is not installed on host"
    echo ""
    echo "Solutions:"
    echo ""
    echo "  1. Start Docker container:"
    echo "     cd docker && docker-compose up -d && docker-compose exec dev bash"
    echo ""
    echo "  2. Or install gh on host:"
    echo "     sudo dnf install gh  # Fedora"
    echo "     sudo apt install gh  # Ubuntu/Debian"
    echo "     brew install gh      # macOS"
    echo ""
    exit 1
}

main
