#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/common.sh"

: "${CLIENT_ID:=stub-client}"
: "${CLIENT_SCOPE:=openid+profile}"
: "${REDIRECT_URI:=${BASE_URL}/auth/miargentina/callback}"
: "${LOGIN_USER:=ciudadano}"

ENCODED_REDIRECT=$(python3 - "$REDIRECT_URI" <<'PY'
import sys
import urllib.parse
print(urllib.parse.quote(sys.argv[1], safe=''))
PY
)

authorize_url="${OAUTH_AUTHORIZE_URL}?response_type=code&client_id=${CLIENT_ID}&scope=${CLIENT_SCOPE}&redirect_uri=${ENCODED_REDIRECT}&user=${LOGIN_USER}"

tmp=$(mktemp)
status=$("$CURL_BIN" \
  --silent \
  --show-error \
  --location \
  --cookie "$COOKIE_JAR" \
  --cookie-jar "$COOKIE_JAR" \
  --output "$tmp" \
  --write-out "%{http_code}" \
  "$authorize_url")

if [[ $status -ge 400 ]]; then
  printf 'Login flow failed (%s)\n' "$status" >&2
  cat "$tmp" >&2
  rm -f "$tmp"
  exit 1
fi
rm -f "$tmp"

printf 'Session cookie stored at %s\n' "$COOKIE_JAR"

http_request GET "/auth/session"
