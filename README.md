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
