#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/common.sh"

ballot_id=${1:-${BALLOT_ID:-1}}

response=$(http_request GET "/ballots/${ballot_id}/tally")
if [[ -n $response ]]; then
  printf '%s\n' "$response" | python3 -m json.tool
else
  printf 'Tally endpoint returned no content.\n'
fi

