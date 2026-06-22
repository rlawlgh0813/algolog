#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
RUN_ID="$(date +%s)"
EMAIL="${EMAIL:-smoke-${RUN_ID}@example.com}"
PASSWORD="${PASSWORD:-password1234}"
NICKNAME="${NICKNAME:-smoke-user-${RUN_ID}}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

log() {
  printf '\n[%s] %s\n' "$1" "$2"
}

json_field() {
  python3 -c 'import json,sys; data=json.load(sys.stdin); print(data["'"$1"'"])'
}

request() {
  local method="$1"
  local path="$2"
  local expected_status="$3"
  local body="${4:-}"
  local token="${5:-}"
  local response_file="$TMP_DIR/response.json"
  local status_file="$TMP_DIR/status.txt"

  local args=(-sS -X "$method" "$BASE_URL$path" -H "Content-Type: application/json" -o "$response_file" -w "%{http_code}")

  if [[ -n "$token" ]]; then
    args+=(-H "Authorization: Bearer $token")
  fi

  if [[ -n "$body" ]]; then
    args+=(-d "$body")
  fi

  if ! curl "${args[@]}" > "$status_file"; then
    printf 'Request failed: %s %s\n' "$method" "$path" >&2
    exit 1
  fi

  local status
  status="$(cat "$status_file")"

  if [[ "$status" != "$expected_status" ]]; then
    printf 'Unexpected status for %s %s: expected %s, got %s\n' "$method" "$path" "$expected_status" "$status" >&2
    printf 'Response body:\n' >&2
    cat "$response_file" >&2
    printf '\n' >&2
    exit 1
  fi

  cat "$response_file"
}

log "1/8" "Signup"
SIGNUP_RESPONSE="$(
  request POST "/api/auth/signup" 201 \
    "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"nickname\":\"$NICKNAME\"}"
)"
USER_ID="$(printf '%s' "$SIGNUP_RESPONSE" | json_field id)"
printf 'Created user id: %s\n' "$USER_ID"

log "2/8" "Login"
LOGIN_RESPONSE="$(
  request POST "/api/auth/login" 200 \
    "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}"
)"
ACCESS_TOKEN="$(printf '%s' "$LOGIN_RESPONSE" | json_field accessToken)"
printf 'Received access token\n'

log "3/8" "Create problem"
PROBLEM_RESPONSE="$(
  request POST "/api/problems" 201 \
    "{\"platform\":\"BOJ\",\"problemNumber\":\"SMOKE-$RUN_ID\",\"title\":\"Smoke Test Problem\",\"difficulty\":\"Bronze V\"}" \
    "$ACCESS_TOKEN"
)"
PROBLEM_ID="$(printf '%s' "$PROBLEM_RESPONSE" | json_field id)"
printf 'Created problem id: %s\n' "$PROBLEM_ID"

log "4/8" "Create public solution record"
SOLUTION_RESPONSE="$(
  request POST "/api/solution-records" 201 \
    "{\"problemId\":$PROBLEM_ID,\"title\":\"Smoke Test Solution\",\"solutionMemo\":\"Use simple arithmetic.\",\"mistakeNote\":\"No mistake yet.\",\"solvingStatus\":\"SOLVED\",\"reviewNeeded\":false,\"visibility\":\"PUBLIC\"}" \
    "$ACCESS_TOKEN"
)"
SOLUTION_RECORD_ID="$(printf '%s' "$SOLUTION_RESPONSE" | json_field id)"
printf 'Created solution record id: %s\n' "$SOLUTION_RECORD_ID"

log "5/8" "Create counter example"
COUNTER_EXAMPLE_RESPONSE="$(
  request POST "/api/solution-records/$SOLUTION_RECORD_ID/counter-examples" 201 \
    '{"inputExample":"1 2","expectedBehavior":"3","wrongReason":"Smoke test sample","fixMemo":"Keep parsing input correctly."}' \
    "$ACCESS_TOKEN"
)"
COUNTER_EXAMPLE_ID="$(printf '%s' "$COUNTER_EXAMPLE_RESPONSE" | json_field id)"
printf 'Created counter example id: %s\n' "$COUNTER_EXAMPLE_ID"

log "6/8" "Read public solution records"
request GET "/api/public/solution-records?size=5" 200 > /dev/null
printf 'Public solution records are readable\n'

log "7/8" "Read public solution records by problem"
request GET "/api/problems/$PROBLEM_ID/public-solution-records?size=5" 200 > /dev/null
printf 'Problem public solution records are readable\n'

log "8/8" "Read solution detail and counter examples"
request GET "/api/solution-records/$SOLUTION_RECORD_ID" 200 > /dev/null
request GET "/api/solution-records/$SOLUTION_RECORD_ID/counter-examples" 200 > /dev/null
printf 'Solution detail and counter examples are readable\n'

printf '\nSmoke test completed successfully.\n'
