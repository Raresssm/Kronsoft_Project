# AgoraCampus

Spring Boot API with PostgreSQL, Keycloak JWT auth, Flyway migrations, and a Next.js frontend.

## Requirements

- Java 21
- Maven
- Docker Desktop
- Node.js and npm

On Windows PowerShell, use `npm.cmd` instead of `npm` if script execution policy blocks `npm.ps1`.

## Run The App Locally

Run the backend dependencies, backend API, and frontend in separate terminals.

### 1. Start Postgres And Keycloak

From the repository root:

```powershell
cd C:\path\to\Kronsoft_Project
docker-compose up -d
```

Wait until both containers are healthy:

```powershell
docker-compose ps
```

Default ports:

- PostgreSQL: `localhost:5432`
- Keycloak: `http://localhost:8090`

If you already have a local PostgreSQL running on `5432`, create a local `.env` file at the repository root:

```env
POSTGRES_PORT=5433
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=agora_campus

KEYCLOAK_PORT=8090
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin

DB_URL=jdbc:postgresql://127.0.0.1:5433/agora_campus
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

Then restart Docker Compose:

```powershell
docker-compose down
docker-compose up -d
```

`.env` is local-only and must not be committed.

### 2. Start The Backend API

From a new terminal:

```powershell
cd C:\path\to\Kronsoft_Project\AgoraCampus
mvn spring-boot:run
```

If you used `POSTGRES_PORT=5433`, run:

```powershell
cd C:\path\to\Kronsoft_Project\AgoraCampus
$env:DB_URL='jdbc:postgresql://127.0.0.1:5433/agora_campus'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='postgres'
mvn spring-boot:run
```

Backend URLs:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- API root `http://localhost:8080` may return `401 Unauthorized`; this is expected.

### Use Swagger With Keycloak

Logging into Keycloak in a separate browser tab does not authenticate Swagger API calls. Swagger must obtain its own access token and send it as a Bearer token.

In Swagger UI:

1. Open `http://localhost:8080/swagger-ui.html`.
2. Click **Authorize**.
3. Choose the `oauth2` authorization option.
4. Use client `agora-swagger-ui`; no client secret is needed.
5. Select scopes `openid` and `profile`, then authorize.
6. Log in with `admin` / `admin` or `demo` / `demo`.
7. After Swagger returns to the API page, call `GET /api/users/me`.

If `GET /api/users/me` returns `401`, check that Swagger shows the endpoint as authorized and that the generated request includes an `Authorization: Bearer ...` header.

If `GET /api/users/me` returns `404`, authentication worked, but the logged-in Keycloak account does not yet have an app user row in the `app_users` table. Create it with `POST /api/users` while still authorized in Swagger.

For the `admin` Keycloak user, use:

```json
{
  "email": "admin@agora.local",
  "username": "admin"
}
```

For the `demo` Keycloak user, use:

```json
{
  "email": "demo@agora.local",
  "username": "demo"
}
```

After `POST /api/users` returns `201 Created`, call `GET /api/users/me` again. It should return the current app user:

```json
{
  "id": 1,
  "keycloakId": "...",
  "email": "admin@agora.local",
  "username": "admin",
  "createdAt": "..."
}
```

Use the returned `id` value as `actingUserId` on endpoints that ask which app user is performing the action.

### 3. Start The Frontend

From a new terminal:

```powershell
cd C:\path\to\Kronsoft_Project\AgoraCampusFrontEnd
npm.cmd install
npm.cmd run dev
```

Frontend URL:

- `http://localhost:3000`

Current state: the frontend runs separately, but login/API calls are not wired to Keycloak/backend yet.

## Keycloak Development Data

- Keycloak: `http://localhost:8090`
- Admin console: `http://localhost:8090/admin`
- Realm: `agora-campus`
- Account page: `http://localhost:8090/realms/agora-campus/account`

Dev users:

- `admin` / `admin`
- `demo` / `demo`

If `demo` / `demo` does not work, try `demo@agora.local` / `demo`. If it still fails, your local Keycloak database was probably created before the `demo` user existed in the realm import file. Keycloak does not overwrite an existing imported realm every time Docker starts.

To reset only the local `demo` password, run this after Keycloak is up:

```powershell
docker-compose exec -T keycloak /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
docker-compose exec -T keycloak /opt/keycloak/bin/kcadm.sh get users -r agora-campus -q username=demo --fields id,username --format csv --noquotes
docker-compose exec -T keycloak /opt/keycloak/bin/kcadm.sh set-password -r agora-campus --userid <demo-user-id> --new-password demo
```

Replace `<demo-user-id>` with the ID printed by the second command.

To force a completely fresh dev import instead, wipe the Docker volumes and start again:

```powershell
docker-compose down -v
docker-compose up -d
```

The full reset deletes the local dev Postgres data, including Keycloak data and app database rows. Use it only when you are fine with resetting local development data.

Clients:

- `agora-swagger-ui`
- `agora-frontend`

All `/api/**` routes require:

```http
Authorization: Bearer <token>
```

Swagger UI can obtain a token through Keycloak.

## Stop The App

Stop the frontend and backend API with `Ctrl+C` in their terminals.

Stop Postgres and Keycloak from the repository root:

```powershell
docker-compose down
```

## Tests

Backend tests:

```powershell
cd C:\path\to\Kronsoft_Project\AgoraCampus
mvn test
```

Frontend build check:

```powershell
cd C:\path\to\Kronsoft_Project\AgoraCampusFrontEnd
npm.cmd install
npm.cmd run build
```
