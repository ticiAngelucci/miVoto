#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/common.sh"

response=$(http_request POST "/seed/default")
if [[ -n $response ]]; then
  printf '%s\n' "$response" | python3 -m json.tool
else
  printf 'Seed endpoint returned no content.\n'
fi

