# AgoraCampus

Spring Boot API with PostgreSQL, Keycloak JWT auth, Flyway migrations, and a Next.js frontend.

## Quick Start

If you just want to run the finished app locally, use Docker from the repository root:

```powershell
.\scripts\start-app.ps1
```

Open the frontend at `http://localhost:3000` and use `admin` / `admin`, `demo` / `demo`, or create a new account from the app.

The script builds and starts Postgres, Keycloak, the Spring Boot API, and the Next.js frontend. It also waits until the services are actually reachable, which avoids login errors caused by opening the frontend while Keycloak is still starting.

If PowerShell blocks the script, run this once in the same terminal:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\scripts\start-app.ps1
```

Without the helper script, the equivalent command is:

```powershell
docker compose up --build -d
```

If your Docker Desktop uses the older Compose command, replace `docker compose` with `docker-compose`.

Then wait until these URLs load:

- Frontend: `http://localhost:3000`
- Backend Swagger: `http://localhost:8080/swagger-ui.html`
- Keycloak: `http://localhost:8090/realms/agora-campus`

## Requirements

- Docker Desktop

Java, Maven, Node.js, and npm are only required if you want to run the backend or frontend manually for development.

## Run The App With Docker

This is the recommended path for teammates, demos, and mentors.

### Start Everything

From the repository root:

```powershell
cd C:\path\to\Kronsoft_Project
.\scripts\start-app.ps1
```

Docker Compose starts these services:

- PostgreSQL: `localhost:5432`
- Keycloak: `http://localhost:8090`
- Backend API: `http://localhost:8080`
- Frontend: `http://localhost:3000`

The first run can take several minutes because Docker has to download base images and build the backend/frontend images.

To inspect the services:

```powershell
docker compose ps
```

`docker compose up -d` may print `2/2`, `3/3`, or more depending on whether Docker had to create the network during that run. That number is not a health check. Use `docker compose ps` or the helper script output to confirm readiness.

### Fresh Reset

If a teammate has an old Keycloak/Postgres volume from a previous version, reset once:

```powershell
docker compose down -v
.\scripts\start-app.ps1
```

This deletes only local Docker development data. It does not affect the repository.

### Port Changes

If a port is already used, copy `.env.example` to `.env` and change the needed port:

```powershell
Copy-Item .env.example .env
```

For the simplest demo setup, leave the defaults unless your machine already uses `3000`, `5432`, `8080`, or `8090`.

## Run The App Manually For Development

Use this only if you want live backend/frontend development outside Docker.

### 1. Start Postgres And Keycloak

```powershell
docker compose up -d postgres keycloak
```

Wait until Keycloak loads:

```powershell
Invoke-WebRequest http://localhost:8090/realms/agora-campus
```

### 2. Start The Backend API

```powershell
.\scripts\dev-backend.ps1
```

Backend URLs:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- API root `http://localhost:8080` may return `401 Unauthorized`; this is expected.

### 3. Start The Frontend

```powershell
cd AgoraCampusFrontEnd
npm.cmd install
npm.cmd run dev
```

Frontend URL:

- `http://localhost:3000`

## Login And Account Creation

Login flow:

1. Open `http://localhost:3000/login`.
2. Enter `admin@agora.local` / `admin`, `admin` / `admin`, `demo@agora.local` / `demo`, or `demo` / `demo`.
3. The frontend exchanges those credentials with Keycloak and stores the token for the current browser tab.
4. The frontend calls `GET /api/users/me`.
5. If the app user does not exist yet, the frontend creates it with `POST /api/users` using the email and username from the Keycloak token.
6. App pages use the returned app user `id` as the current `actingUserId` when backend calls need it.

Use **Log out** in the app header before switching accounts. The frontend login does not reuse old Keycloak browser cookies, so a new login attempt should use the credentials typed into the form.

Create account flow:

1. Open `http://localhost:3000`.
2. Enter email, password, and account type.
3. The frontend calls `POST /api/auth/register`.
4. The backend creates the Keycloak account, then the frontend logs in with the same credentials and creates the matching app user row if needed.

If account creation returns `401` or says registration is not enabled on the running backend, restart the backend container with `docker compose restart backend`. That means the frontend is still talking to an older backend process that does not include the public `POST /api/auth/register` endpoint.

The `agora-frontend` Keycloak client must have **Direct Access Grants** enabled for the frontend login form. The realm import file already sets this for new local environments. If an existing local Keycloak volume was created before this setting changed, either enable it in the Keycloak admin console or reset local volumes with `docker compose down -v`.

## Use Swagger With Keycloak

You do not need Swagger authorization to use the frontend. The frontend gets its own token from Keycloak.

Swagger authorization is only needed when calling protected API endpoints directly from `http://localhost:8080/swagger-ui.html`.

In Swagger UI:

1. Open `http://localhost:8080/swagger-ui.html`.
2. Click **Authorize**.
3. Choose the `oauth2` authorization option.
4. Use client `agora-swagger-ui`; no client secret is needed.
5. Select scopes `openid` and `profile`, then authorize.
6. Log in with `admin` / `admin` or `demo` / `demo`.
7. After Swagger returns to the API page, call `GET /api/users/me`.

## Keycloak Development Data

- Keycloak: `http://localhost:8090`
- Admin console: `http://localhost:8090/admin`
- Realm: `agora-campus`
- Account page: `http://localhost:8090/realms/agora-campus/account`

Dev users:

- `admin` / `admin`
- `demo` / `demo`
- `alice@agora.com` / `password`
- `bogdan@agora.com` / `password`
- `carmen@agora.com` / `password`
- `david@agora.com` / `password`
- `emma@agora.com` / `password`

These users are imported into Keycloak when the local Docker volume is created. The matching app profile is created automatically the first time each user logs into the frontend.

If you already have an existing local Docker volume, new seed users are not imported into that old volume. Run `docker compose down -v` once, then `.\scripts\start-app.ps1`, to recreate the local dev database from the seed file.

If `demo` / `demo` does not work, try `demo@agora.local` / `demo`. If it still fails, your local Keycloak database was probably created before the `demo` user existed in the realm import file. Keycloak does not overwrite an existing imported realm every time Docker starts.

To reset only the local `demo` password, run this after Keycloak is up:

```powershell
docker compose exec -T keycloak /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
docker compose exec -T keycloak /opt/keycloak/bin/kcadm.sh get users -r agora-campus -q username=demo --fields id,username --format csv --noquotes
docker compose exec -T keycloak /opt/keycloak/bin/kcadm.sh set-password -r agora-campus --userid <demo-user-id> --new-password demo
```

Replace `<demo-user-id>` with the ID printed by the second command.

To force a completely fresh dev import instead, wipe the Docker volumes and start again:

```powershell
docker compose down -v
.\scripts\start-app.ps1
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

## Database Diagram

The current Mermaid source lives in [db_diagram/agora-erd.mmd](db_diagram/agora-erd.mmd). It includes the `Background` entity and the rest of the main app model.

```mermaid
erDiagram
  APP_USERS {
    BIGINT app_user_id PK
    STRING keycloak_id
    STRING email
    STRING username
    TIMESTAMP created_at
  }

  PROFILES {
    BIGINT profile_id PK
    BIGINT app_user_id FK
    STRING headline
    STRING description
    STRING location
    STRING website
    STRING profile_picture
    STRING cover_image
    STRING profile_type
    TIMESTAMP updated_at
  }

  INDIVIDUAL_PROFILES {
    BIGINT individual_profile_id PK
    BIGINT profile_id FK
    STRING first_name
    STRING last_name
    STRING phone
    STRING cv_document
  }

  ORGANIZATION_PROFILES {
    BIGINT organization_profile_id PK
    BIGINT profile_id FK
    STRING organization_name
    STRING phone
    STRING industry
    STRING specialties
  }

  BACKGROUND {
    BIGINT background_id PK
    BIGINT individual_profile_id FK
    STRING type
    STRING title
    STRING description
    DATE start_date
    DATE end_date
    BOOLEAN currently_ongoing
  }

  POSTS {
    BIGINT post_id PK
    BIGINT app_user_id FK
    STRING content
    STRING media_url
    TIMESTAMP created_at
  }

  COMMENTS {
    BIGINT comment_id PK
    BIGINT post_id FK
    BIGINT app_user_id FK
    STRING content
    TIMESTAMP created_at
  }

  REACTIONS {
    BIGINT reaction_id PK
    BIGINT post_id FK
    BIGINT app_user_id FK
    STRING reaction_type
    TIMESTAMP created_at
  }

  MESSAGES {
    BIGINT message_id PK
    BIGINT sender_user_id FK
    BIGINT receiver_user_id FK
    STRING content
    TIMESTAMP sent_at
    BOOLEAN is_read
  }

  CONNECTIONS {
    BIGINT connection_id PK
    BIGINT requester_user_id FK
    BIGINT receiver_user_id FK
    STRING status
    TIMESTAMP created_at
  }

  OPPORTUNITIES {
    BIGINT opportunity_id PK
    BIGINT posted_by_user_id FK
    BIGINT organization_profile_id FK
    BIGINT individual_profile_id FK
    STRING title
    STRING type
    STRING location
    STRING period
    STRING description
    STRING additional_info
    TIMESTAMP created_at
  }

  OPPORTUNITY_APPLICATIONS {
    BIGINT application_id PK
    BIGINT opportunity_id FK
    BIGINT applicant_user_id FK
    STRING status
    TIMESTAMP applied_at
  }

  VOLUNTEERING {
    BIGINT volunteering_id PK
    BIGINT opportunity_id FK
    STRING target_audience
    STRING schedule
    STRING benefits
  }

  COMPETITIONS {
    BIGINT competition_id PK
    BIGINT opportunity_id FK
    STRING rules
    STRING prizes
    DATE deadline
  }

  INTERNSHIPS {
    BIGINT internship_id PK
    BIGINT opportunity_id FK
    STRING duration
    STRING compensation
    STRING requirements
  }

  STUDENT_PROJECTS {
    BIGINT student_project_id PK
    BIGINT opportunity_id FK
    STRING domain
    STRING team_size
    STRING requirements
  }

  APP_USERS ||--|| PROFILES : has
  PROFILES ||--|| INDIVIDUAL_PROFILES : individual_subtype
  PROFILES ||--|| ORGANIZATION_PROFILES : organization_subtype
  INDIVIDUAL_PROFILES ||--o{ BACKGROUND : owns

  APP_USERS ||--o{ POSTS : authors
  APP_USERS ||--o{ COMMENTS : writes
  POSTS ||--o{ COMMENTS : contains
  APP_USERS ||--o{ REACTIONS : reacts
  POSTS ||--o{ REACTIONS : receives
  APP_USERS ||--o{ MESSAGES : sends
  APP_USERS ||--o{ MESSAGES : receives
  APP_USERS ||--o{ CONNECTIONS : requests
  APP_USERS ||--o{ CONNECTIONS : accepts

  APP_USERS ||--o{ OPPORTUNITIES : posts
  ORGANIZATION_PROFILES ||--o{ OPPORTUNITIES : posts_for
  INDIVIDUAL_PROFILES ||--o{ OPPORTUNITIES : posts_for
  OPPORTUNITIES ||--o{ OPPORTUNITY_APPLICATIONS : has
  APP_USERS ||--o{ OPPORTUNITY_APPLICATIONS : applies
  OPPORTUNITIES ||--o| VOLUNTEERING : volunteering_details
  OPPORTUNITIES ||--o| COMPETITIONS : competition_details
  OPPORTUNITIES ||--o| INTERNSHIPS : internship_details
  OPPORTUNITIES ||--o| STUDENT_PROJECTS : student_project_details
```

## Stop The App

From the repository root:

```powershell
.\scripts\stop-app.ps1
```

This stops the containers and preserves the database.

Do not use `down -v` for normal stopping. The `-v` flag deletes the Docker volume, which deletes local accounts, profiles, posts, opportunities, messages, and Keycloak changes.

If you are using the manual development mode, stop the backend and frontend with `Ctrl+C` in their terminals.

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

## Troubleshooting

- **Port already in use:** stop any existing process on `5432`, `8080`, `8090`, or `3000`, or change the mapped port in `.env`.
- **Keycloak token request fails with `ERR_CONNECTION_REFUSED`:** Keycloak is not reachable yet at `http://localhost:8090`. Use `.\scripts\start-app.ps1`, or wait until `http://localhost:8090/realms/agora-campus` loads before logging in.
- **Old Keycloak data:** if seeded users or clients are missing, run `docker compose down -v` and then `.\scripts\start-app.ps1`.
- **Manual frontend auth issues:** make sure the backend is started with `.\scripts\dev-backend.ps1`, not plain `mvn spring-boot:run`, if you rely on `.env` values.


