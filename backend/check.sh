#!/bin/bash

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PORT="${CHECK_PORT:-8000}"
BRUNO_DIR="bruno" # folder that contains bruno.json

print_header() {
    echo ""
    echo -e "${BLUE}=========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}=========================================${NC}"
}

print_success() { echo -e "${GREEN}✓ $1${NC}"; }
print_error() { echo -e "${RED}✗ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠ $1${NC}"; }
print_step() { echo -e "${BLUE}→ $1${NC}"; }

if [ ! -f "pyproject.toml" ]; then
    print_error "pyproject.toml not found. Run this script from the backend directory."
    exit 1
fi

LOG="$(mktemp)"
SERVER_LOG="$(mktemp)"
DB_DIR="$(mktemp -d)"
SERVER_PID=""

cleanup() {
    if [ -n "$SERVER_PID" ]; then
        kill "$SERVER_PID" 2>/dev/null || true
    fi
    rm -f "$LOG" "$SERVER_LOG"
    rm -rf "$DB_DIR"
}
trap cleanup EXIT

run_step() {
    local label="$1" hint="$2"
    shift 2
    print_step "$label..."
    if "$@" >"$LOG" 2>&1; then
        print_success "$label passed"
    else
        print_error "$label failed. Fix with: $hint"
        echo ""
        cat "$LOG"
        exit 1
    fi
}

start_server() {
    print_step "Starting API (dummy predictor, throwaway database) on port $PORT..."
    DATABASE_URL="sqlite:///$DB_DIR/check.db" ML_PREDICTOR=dummy \
        uv run uvicorn app.main:app --app-dir src --port "$PORT" >"$SERVER_LOG" 2>&1 &
    SERVER_PID=$!

    for _ in $(seq 1 30); do
        if curl -sf "http://127.0.0.1:$PORT/docs" >/dev/null; then
            print_success "API is up"
            return 0
        fi
        if ! kill -0 "$SERVER_PID" 2>/dev/null; then
            break
        fi
        sleep 1
    done

    print_error "API did not start. Check: uv run uvicorn app.main:app --app-dir src"
    echo ""
    cat "$SERVER_LOG"
    exit 1
}

print_header "TEAM4 BACKEND CHECKS"

if [ "${CHECK_SKIP_INSTALL:-0}" = "1" ] && [ -d .venv ]; then
    print_step "Skipping install (CHECK_SKIP_INSTALL=1)"
else
    run_step "Install" "uv lock, then uv sync" uv sync --locked --inexact
fi

if ! command -v npx >/dev/null 2>&1; then
    print_error "npx not found. Install Node.js to run the Bruno API tests."
    exit 1
fi

run_step "Lint" "uv run ruff check --fix" uv run ruff check
run_step "Formatting" "uv run ruff format" uv run ruff format --check
run_step "Type check" "uv run mypy" uv run mypy
run_step "Unit tests" "uv run pytest" uv run pytest

start_server
run_step "API tests (Bruno)" "cd $BRUNO_DIR && npx @usebruno/cli run --env local" \
    bash -c "cd '$BRUNO_DIR' && npx --yes @usebruno/cli run --env local"

print_header "ALL CHECKS PASSED ✓"
echo -e "${GREEN}✓ Lint + format (ruff)${NC}"
echo -e "${GREEN}✓ Type check (mypy)${NC}"
echo -e "${GREEN}✓ Unit tests (pytest)${NC}"
echo -e "${GREEN}✓ API tests (Bruno)${NC}"
echo ""