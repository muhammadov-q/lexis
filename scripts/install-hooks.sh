#!/usr/bin/env bash
# Run once after cloning. Points git at .githooks/ (core.hooksPath) so the
# shared pre-commit and pre-push hooks run on your machine, then prints what
# each one does.
# Undo with: git config --unset core.hooksPath

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

chmod +x .githooks/* 2>/dev/null || true
git config core.hooksPath .githooks

for hook in pre-commit pre-push; do
    if [ ! -x ".githooks/$hook" ]; then
        echo "ERROR: .githooks/$hook is not executable, so git will ignore it." >&2
        echo "Run: chmod +x .githooks/$hook" >&2
        exit 1
    fi
done

echo "Hooks enabled (core.hooksPath -> .githooks)."
echo
echo "  pre-commit  ESLint --fix + Prettier on staged frontend files (lint-staged)"
echo "  pre-push    the check.sh of every area you touched (frontend, backend, mobile, docs)"
echo
echo "Bypass once with:     git commit --no-verify / git push --no-verify"
echo "Disable entirely with: git config --unset core.hooksPath"
