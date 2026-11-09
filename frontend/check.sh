#!/bin/bash

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

if [ ! -f "package.json" ]; then
    print_error "package.json not found. Run this script from the frontend directory."
    exit 1
fi

LOG="$(mktemp)"
trap 'rm -f "$LOG"' EXIT

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

print_header "LEXIS FRONTEND CHECKS"

if [ "${CHECK_SKIP_INSTALL:-0}" = "1" ] && [ -d node_modules ]; then
    print_step "Skipping install (CHECK_SKIP_INSTALL=1)"
else
    run_step "Install" "npm ci" npm ci --no-audit --no-fund --prefer-offline
fi

AUDIT_SUMMARY="${GREEN}✓ Security audit${NC}"
print_step "Security audit..."
if npm audit --audit-level=high >/dev/null 2>&1; then
    print_success "No high or critical advisories"
else
    print_warning "Advisories found. Run: npm audit"
    AUDIT_SUMMARY="${YELLOW}⚠ Security audit (advisories)${NC}"
fi

run_step "Lint" "npm run lint:fix" npm run lint
run_step "Formatting" "npm run format" npm run format:check
run_step "Type check" "npm run type-check" npm run type-check
run_step "Unit tests" "npm test" npm test

print_header "ALL CHECKS PASSED ✓"
echo -e "$AUDIT_SUMMARY"
echo -e "${GREEN}✓ Lint + format${NC}"
echo -e "${GREEN}✓ Type check${NC}"
echo -e "${GREEN}✓ Unit tests${NC}"
echo ""
