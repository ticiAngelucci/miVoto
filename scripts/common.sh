#!/usr/bin/env bash
set -euo pipefail

# Shared helpers for backend automation scripts.

: "${BASE_URL:=http://localhost:8080}"
: "${OAUTH_AUTHORIZE_URL:=http://localhost:9999/oauth/authorize}"
: "${COOKIE_JAR:=.tmp/cookies/session.jar}"
: "${CURL_BIN:=curl}"

mkdir -p "$(dirname "$COOKIE_JAR")"

http_request() {
  local method=$1
  local path=$2
  local data=${3-}

  local url
  if [[ $path == http://* || $path == https://* ]]; then
    url=$path
  else
    url="${BASE_URL}${path}"
  fi

  local tmp
  tmp=$(mktemp)
  local status
  local -a args=(
    "--silent"
    "--show-error"
    "--cookie" "$COOKIE_JAR"
    "--cookie-jar" "$COOKIE_JAR"
    "--header" "Accept: application/json"
    "--request" "$method"
    "--write-out" "%{http_code}"
    "--output" "$tmp"
  )
  if [[ -n $data ]]; then
    args+=("--header" "Content-Type: application/json" "--data" "$data")
  fi
  args+=("$url")

  status=$("$CURL_BIN" "${args[@]}")
  if [[ ${status} -ge 200 && ${status} -lt 300 ]]; then
    cat "$tmp"
  else
    printf 'Request failed (%s) for %s %s\n' "$status" "$method" "$url" >&2
    cat "$tmp" >&2
    rm -f "$tmp"
    exit 1
  fi
  rm -f "$tmp"
}

