#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

echo "Stopping containers and removing volumes (re-imports realm on next start)..."
docker compose down -v

docker compose up -d

echo ""
echo "Waiting for Keycloak..."
until curl -sf "http://localhost:${KEYCLOAK_PORT:-8090}/realms/agora-campus" >/dev/null 2>&1; do
  sleep 2
done

echo "Done. Realm agora-campus was re-imported from docker/keycloak/realms/agora-campus-realm.json"
