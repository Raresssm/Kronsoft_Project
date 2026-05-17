#!/usr/bin/env bash
# Enables direct access grants on agora-frontend (required for custom login UI).
# Realm JSON import only runs on first volume create; this patches a running Keycloak.
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
KEYCLOAK_CLIENT_ID="${KEYCLOAK_CLIENT_ID:-agora-frontend}"
KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"

echo "Patching Keycloak client '${KEYCLOAK_CLIENT_ID}' at ${KEYCLOAK_URL}..."

admin_token="$(
  curl -sf -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" \
    -d "username=${KEYCLOAK_ADMIN}" \
    -d "password=${KEYCLOAK_ADMIN_PASSWORD}" \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['access_token'])"
)"

internal_id="$(
  curl -sf "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/clients?clientId=${KEYCLOAK_CLIENT_ID}" \
    -H "Authorization: Bearer ${admin_token}" \
  | python3 -c "import sys, json; print(json.load(sys.stdin)[0]['id'])"
)"

client_json="$(mktemp)"
curl -sf "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/clients/${internal_id}" \
  -H "Authorization: Bearer ${admin_token}" > "${client_json}"

python3 - "${client_json}" <<'PY'
import json
import sys

path = sys.argv[1]
with open(path, encoding="utf-8") as f:
    client = json.load(f)

client["directAccessGrantsEnabled"] = True
with open(path, "w", encoding="utf-8") as f:
    json.dump(client, f)
PY

curl -sf -X PUT "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/clients/${internal_id}" \
  -H "Authorization: Bearer ${admin_token}" \
  -H "Content-Type: application/json" \
  -d @"${client_json}"

rm -f "${client_json}"
echo "directAccessGrantsEnabled=true on ${KEYCLOAK_CLIENT_ID}"
