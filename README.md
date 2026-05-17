# AgoraCampus Backend

Spring Boot API with PostgreSQL, Keycloak JWT auth, and Flyway migrations.

## Requirements

- Java 21, Maven, Docker

## Quick start (full stack)

```bash
./scripts/dev/dev-up.sh                        # Postgres + Keycloak
cd AgoraCampus && mvn spring-boot:run            # API on :8080
cp AgoraCampusFrontEnd/.env.local.example AgoraCampusFrontEnd/.env.local
cd AgoraCampusFrontEnd && npm install && npm run dev   # UI on :3000
```

Backend only:

```bash
./scripts/dev/dev-up.sh
cd AgoraCampus && mvn spring-boot:run
```

See **`scripts/README.md`** for what each script does.

### Two `.env` files (why)

| File | Used by | Contains |
|------|---------|----------|
| **`.env`** (repo root) | Docker Compose, `scripts/keycloak/*`, Spring on your machine | Postgres/Keycloak ports, Gmail SMTP, `DB_URL`, `KEYCLOAK_ISSUER_URI` |
| **`AgoraCampusFrontEnd/.env.local`** | Next.js only | `NEXT_PUBLIC_*` URLs (browser-safe; baked in at dev/build time) |

They are separate because the **frontend** and **infra/backend** run in different processes and Next.js only exposes variables prefixed with `NEXT_PUBLIC_` to the browser. Copy **`.env.example`** → **`.env`** and **`AgoraCampusFrontEnd/.env.local.example`** → **`AgoraCampusFrontEnd/.env.local`**.

Configuration: root **`.env`** is gitignored; never commit secrets (Gmail App Password, etc.).

After editing `docker/keycloak/realms/agora-campus-realm.json`:

```bash
./scripts/dev/dev-reset.sh       # wipes volumes and re-imports realm
```

**Keycloak won’t start (exit 127)?** An old container may still reference a removed `import-and-start.sh` entrypoint. Recreate it:

```bash
docker compose up -d --force-recreate keycloak
```

## URLs (defaults)

| Service | URL |
|---------|-----|
| API / Swagger | http://localhost:8080 · http://localhost:8080/swagger-ui.html |
| Keycloak | http://localhost:8090 · realm `agora-campus` |
| Account / login | http://localhost:8090/realms/agora-campus/account |
| JWT issuer | http://localhost:8090/realms/agora-campus |

**Dev users:** `admin`/`admin`, `demo`/`demo`  
**Clients:** `agora-swagger-ui` (Swagger PKCE), `agora-frontend` (SPA PKCE)  
**Roles:** `admin` → `ROLE_ADMIN`, `user` → `ROLE_USER`

All `/api/**` routes need `Authorization: Bearer <token>`. Swagger UI and OpenAPI docs are public.

### Frontend (Next.js + `keycloak-js`)

```bash
cp AgoraCampusFrontEnd/.env.local.example AgoraCampusFrontEnd/.env.local
cd AgoraCampusFrontEnd && npm install && npm run dev
```

Open http://localhost:3000 — sign in redirects to Keycloak, then back to the app.

```env
NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8090
NEXT_PUBLIC_KEYCLOAK_REALM=agora-campus
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=agora-frontend
NEXT_PUBLIC_API_URL=http://localhost:8080
```

After login: `GET /api/users/me` → `POST /api/users` if 404 → create profile → other APIs with `actingUserId` and the same Bearer token.

Integration checklist: see **`INTEGRATION_TASKS.md`** in the repo root.

### Custom login UI & forgot password

Login, register, and forgot-password use **your Next.js pages**, not Keycloak’s hosted screens. The browser calls `/api/auth/*`; those routes talk to Keycloak on the server (password grant + admin API).

**Forgot password (Gmail, no custom domain):** Keycloak sends reset mail via Gmail SMTP. Credentials live in repo root **`.env`** (not committed):

1. Copy `.env.example` → `.env`
2. Create a [Google App Password](https://myaccount.google.com/apppasswords) (requires 2‑Step Verification on your Google account)
3. Set in `.env`:
   ```env
   KEYCLOAK_SMTP_USER=you@gmail.com
   KEYCLOAK_SMTP_PASSWORD=xxxx xxxx xxxx xxxx
   KEYCLOAK_SMTP_FROM=you@gmail.com
   ```
4. Run `./scripts/dev/dev-up.sh` (applies SMTP to realm **agora-campus** automatically), or only:
   ```bash
   ./scripts/keycloak/configure-smtp.sh
   ```
5. Optional: Admin UI → **Realm settings** → **Email** → **Test connection**
6. In the app: **Forgotten password** → enter the account email

Gmail defaults: `smtp.gmail.com:587`, StartTLS (configured by the script). Do not use your normal Gmail password — only an App Password.

Without `KEYCLOAK_SMTP_*` in `.env`, forgot-password emails are skipped / may fail.

If login returns **“Client not allowed for direct access grants”**, the running Keycloak client was created before that flag was enabled. Run:

```bash
./scripts/keycloak/patch-frontend-client.sh
```

(`scripts/dev/dev-up.sh` runs this automatically after Keycloak is ready.)

**“Account is not fully set up” on register:** Keycloak requires **first and last name** on the user profile. The register API sets them automatically (from the form or derived from your email). Accounts created before this fix may need to be deleted in the Keycloak admin console, or register with a new email.

## Database

- App DB: `agora_campus` on `localhost:5432` (`postgres`/`postgres`)
- Schema: Flyway in `AgoraCampus/src/main/resources/db/migration/`, Hibernate `ddl-auto: validate`

Override JDBC or Keycloak settings via `.env` or environment variables (`DB_URL`, `KEYCLOAK_ISSUER_URI`, `CORS_ALLOWED_ORIGINS`, etc.).

## Tests

```bash
cd AgoraCampus && mvn test
```

Uses H2 and a permit-all security profile — Postgres and Keycloak are not required.
