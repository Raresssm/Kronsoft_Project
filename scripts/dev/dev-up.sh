#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

if [ ! -f .env ] && [ -f .env.example ]; then
  cp .env.example .env
fi

docker compose up -d

# Older local setups used a custom entrypoint (import-and-start.sh). If that
# container still exists, Keycloak exits with code 127 until it is recreated.
keycloak_exit="$(docker compose ps -a keycloak --format '{{.ExitCode}}' 2>/dev/null | head -1 || true)"
if [ "${keycloak_exit}" = "127" ]; then
  echo "Recreating Keycloak (stale container with removed entrypoint)..."
  docker compose up -d --force-recreate keycloak
fi

echo ""
echo "Waiting for Keycloak to become ready..."
until curl -sf "http://localhost:${KEYCLOAK_PORT:-8090}/realms/agora-campus" >/dev/null 2>&1; do
  sleep 2
done

"${ROOT_DIR}/scripts/keycloak/patch-frontend-client.sh"
"${ROOT_DIR}/scripts/keycloak/configure-smtp.sh"

echo ""
echo "Stack is up."
echo "  PostgreSQL : localhost:${POSTGRES_PORT:-5432} (db: agora_campus)"
echo "  Keycloak   : http://localhost:${KEYCLOAK_PORT:-8090}"
echo "  Realm      : agora-campus (imported automatically)"
echo ""
echo "Pre-configured realm users (dev only):"
echo "  admin / admin  (roles: admin, user)"
echo "  demo  / demo   (roles: user)"
echo ""
echo "OIDC clients: agora-frontend, agora-swagger-ui"
echo ""
echo "Start API: cd AgoraCampus && mvn spring-boot:run"
