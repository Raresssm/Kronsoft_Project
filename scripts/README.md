# Scripts

| Folder | Scripts | Purpose |
|--------|---------|---------|
| **`dev/`** | `dev-up.sh` | Start Postgres + Keycloak, wait, run Keycloak patches |
| | `dev-reset.sh` | Wipe Docker volumes, fresh realm import, run Keycloak patches |
| **`keycloak/`** | `patch-frontend-client.sh` | Enable direct access grants on `agora-frontend` (custom login) |
| | `configure-smtp.sh` | Apply Gmail SMTP from repo root `.env` (forgot password) |

All scripts assume the **repo root** as working directory for `docker compose` and `.env`.

```bash
./scripts/dev/dev-up.sh
./scripts/dev/dev-reset.sh
./scripts/keycloak/configure-smtp.sh
```
