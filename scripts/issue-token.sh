#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/common.sh"

token_only=false
for arg in "$@"; do
  case "$arg" in
    --token|-t)
      token_only=true
      ;;
    *)
      printf 'Uso: %s [--token]\n' "$0" >&2
      exit 1
      ;;
  esac
done

wallet=${WALLET_ADDRESS:-}
if [[ -z $wallet ]]; then
  printf 'Definí WALLET_ADDRESS antes de solicitar el token (ej. export WALLET_ADDRESS=0xf39f...).\n' >&2
  exit 1
fi
if [[ ! $wallet =~ ^0x[0-9a-fA-F]{40}$ ]]; then
  printf 'WALLET_ADDRESS inválido: %s\n' "$wallet" >&2
  exit 1
fi

payload=$(python3 - "$wallet" <<'PY'
import json
import sys

wallet = sys.argv[1]
print(json.dumps({"walletAddress": wallet}))
PY
)

response=$(http_request POST "/eligibility/issue/session" "$payload")

if $token_only; then
  printf '%s\n' "$response" | python3 -c 'import json,sys
raw = sys.stdin.read().strip()
if not raw:
    raise SystemExit("empty response from eligibility endpoint")
payload = json.loads(raw)
token = payload.get("eligibilityToken")
if not token:
    raise SystemExit("eligibilityToken missing in response")
print(token)'
else
  printf '%s\n' "$response" | python3 -m json.tool
fi
