# AgoraCampus Backend

Spring Boot API with PostgreSQL, Keycloak JWT auth, and Flyway migrations.

## Requirements

- Java 21, Maven, Docker

## Quick start

```bash
./scripts/dev-up.sh          # Postgres + Keycloak (realm auto-imported)
cd AgoraCampus && mvn spring-boot:run
```

Configuration: copy **`.env.example`** to **`.env`** at the repo root (used by Docker Compose and optional Spring overrides; `.env` is not committed).

After editing `docker/keycloak/realms/agora-campus-realm.json`:

```bash
./scripts/dev-reset.sh       # wipes volumes and re-imports realm
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

### Frontend (`keycloak-js`)

```env
VITE_KEYCLOAK_URL=http://localhost:8090
VITE_KEYCLOAK_REALM=agora-campus
VITE_KEYCLOAK_CLIENT_ID=agora-frontend
VITE_API_URL=http://localhost:8080
```

After login: `GET /api/users/me` → `POST /api/users` if 404 → create profile → other APIs with `actingUserId` and the same Bearer token.

## Database

- App DB: `agora_campus` on `localhost:5432` (`postgres`/`postgres`)
- Schema: Flyway in `AgoraCampus/src/main/resources/db/migration/`, Hibernate `ddl-auto: validate`

Override JDBC or Keycloak settings via `.env` or environment variables (`DB_URL`, `KEYCLOAK_ISSUER_URI`, `CORS_ALLOWED_ORIGINS`, etc.).

## Tests

```bash
cd AgoraCampus && mvn test
```

Uses H2 and a permit-all security profile — Postgres and Keycloak are not required.
