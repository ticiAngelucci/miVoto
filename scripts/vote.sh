#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/common.sh"

ballot_id=${BALLOT_ID:-1}
institution_id=${INSTITUTION_ID:-inst-1}
candidate_csv=${1:-${CANDIDATE_IDS:-cand-1}}

if [[ -z $candidate_csv ]]; then
  printf 'Al menos un candidato es requerido (usa CANDIDATE_IDS o argumento).\n' >&2
  exit 1
fi

token=$("$SCRIPT_DIR/issue-token.sh" --token)
token=$(echo "$token" | tr -d '\r\n')

payload=$( \
  BALLOT_ID=$ballot_id \
  INSTITUTION_ID=$institution_id \
  ELIGIBILITY_TOKEN=$token \
  CANDIDATE_IDS=$candidate_csv \
  python3 - <<'PY'
import json
import os

ballot_id = os.environ["BALLOT_ID"]
institution_id = os.environ["INSTITUTION_ID"]
token = os.environ["ELIGIBILITY_TOKEN"]
candidates = [value for value in os.environ["CANDIDATE_IDS"].split(",") if value]

if not candidates:
  raise SystemExit("Candidate list is empty after parsing.")

payload = {
    "ballotId": ballot_id,
    "eligibilityToken": token,
    "selection": {
        "institutionId": institution_id,
        "candidateIds": candidates,
    },
}

print(json.dumps(payload))
PY
)

response=$(http_request POST "/votes/cast" "$payload")
if [[ -n $response ]]; then
  printf '%s\n' "$response" | python3 -m json.tool
else
  printf 'Vote endpoint returned no content.\n'
fi

