#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

failures=0

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "FAIL: '$1' not found in PATH"
    failures=$((failures + 1))
    return 1
  fi
  echo "OK: $1 -> $(command -v "$1")"
}

echo "== Toolchain =="
require_cmd java || true
require_cmd mvn || true

if ! command -v node >/dev/null 2>&1 && [[ -s "${NVM_DIR:-$HOME/.nvm}/nvm.sh" ]]; then
  # shellcheck disable=SC1090
  source "${NVM_DIR:-$HOME/.nvm}/nvm.sh"
  nvm use >/dev/null 2>&1 || true
fi

require_cmd node || true
require_cmd npm || true

echo
echo "== Versions =="
java -version 2>&1 | head -1 || true
mvn -version 2>&1 | head -1 || true
node -v 2>/dev/null || true
npm -v 2>/dev/null || true

echo
echo "== Maven local repository =="
echo "Using project repo: $ROOT/.m2/repository"
mkdir -p "$ROOT/.m2/repository"
if [[ ! -w "$ROOT/.m2/repository" ]]; then
  echo "FAIL: .m2/repository is not writable"
  failures=$((failures + 1))
else
  echo "OK: .m2/repository is writable"
fi

if [[ "$failures" -gt 0 ]]; then
  echo
  echo "Dev environment incomplete. Install Node.js in WSL, e.g.:"
  echo "  sudo apt-get update && sudo apt-get install -y nodejs npm"
  echo "Or with nvm:"
  echo "  curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash"
  echo "  nvm install"
  exit 1
fi

echo
echo "Dev environment ready."
