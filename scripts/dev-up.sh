#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

if [ ! -f .env ] && [ -f .env.example ]; then
  cp .env.example .env
fi

docker compose up -d

echo ""
echo "Waiting for Keycloak to become ready..."
until curl -sf "http://localhost:${KEYCLOAK_PORT:-8090}/realms/agora-campus" >/dev/null 2>&1; do
  sleep 2
done

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
