#!/usr/bin/env bash
# Applies Gmail (or custom) SMTP to the agora-campus realm via Keycloak Admin API.
# Set KEYCLOAK_SMTP_* in repo root .env (see .env.example).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:${KEYCLOAK_PORT:-8090}}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-agora-campus}"
KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"

KEYCLOAK_SMTP_HOST="${KEYCLOAK_SMTP_HOST:-smtp.gmail.com}"
KEYCLOAK_SMTP_PORT="${KEYCLOAK_SMTP_PORT:-587}"
KEYCLOAK_SMTP_FROM="${KEYCLOAK_SMTP_FROM:-${KEYCLOAK_SMTP_USER:-}}"
KEYCLOAK_SMTP_USER="${KEYCLOAK_SMTP_USER:-}"
KEYCLOAK_SMTP_PASSWORD="${KEYCLOAK_SMTP_PASSWORD:-}"
KEYCLOAK_SMTP_FROM_DISPLAY_NAME="${KEYCLOAK_SMTP_FROM_DISPLAY_NAME:-Agora Campus}"

if [ -z "${KEYCLOAK_SMTP_USER}" ] || [ -z "${KEYCLOAK_SMTP_PASSWORD}" ]; then
  echo "SMTP: skipped (set KEYCLOAK_SMTP_USER and KEYCLOAK_SMTP_PASSWORD in .env for Gmail reset emails)."
  exit 0
fi

if [ -z "${KEYCLOAK_SMTP_FROM}" ]; then
  KEYCLOAK_SMTP_FROM="${KEYCLOAK_SMTP_USER}"
fi

echo "SMTP: configuring realm '${KEYCLOAK_REALM}' → ${KEYCLOAK_SMTP_HOST}:${KEYCLOAK_SMTP_PORT} as ${KEYCLOAK_SMTP_FROM}"

admin_token="$(
  curl -sf -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" \
    -d "username=${KEYCLOAK_ADMIN}" \
    -d "password=${KEYCLOAK_ADMIN_PASSWORD}" \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['access_token'])"
)"

realm_json="$(mktemp)"
curl -sf "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}" \
  -H "Authorization: Bearer ${admin_token}" > "${realm_json}"

export KEYCLOAK_SMTP_HOST KEYCLOAK_SMTP_PORT KEYCLOAK_SMTP_FROM KEYCLOAK_SMTP_FROM_DISPLAY_NAME KEYCLOAK_SMTP_USER KEYCLOAK_SMTP_PASSWORD
python3 - "${realm_json}" <<'PY'
import json
import os
import sys

path = sys.argv[1]
with open(path, encoding="utf-8") as f:
    realm = json.load(f)

realm["smtpServer"] = {
    "host": os.environ["KEYCLOAK_SMTP_HOST"],
    "port": os.environ["KEYCLOAK_SMTP_PORT"],
    "from": os.environ["KEYCLOAK_SMTP_FROM"],
    "fromDisplayName": os.environ["KEYCLOAK_SMTP_FROM_DISPLAY_NAME"],
    "replyTo": os.environ["KEYCLOAK_SMTP_FROM"],
    "replyToDisplayName": os.environ["KEYCLOAK_SMTP_FROM_DISPLAY_NAME"],
    "envelopeFrom": os.environ["KEYCLOAK_SMTP_FROM"],
    "ssl": "false",
    "starttls": "true",
    "auth": "true",
    "user": os.environ["KEYCLOAK_SMTP_USER"],
    "password": os.environ["KEYCLOAK_SMTP_PASSWORD"],
}

with open(path, "w", encoding="utf-8") as f:
    json.dump(realm, f)
PY

curl -sf -X PUT "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}" \
  -H "Authorization: Bearer ${admin_token}" \
  -H "Content-Type: application/json" \
  -d @"${realm_json}"

rm -f "${realm_json}"

echo "SMTP: realm email settings saved."
echo "SMTP: test in Admin UI → Realm settings → Email → Save → Test connection"
