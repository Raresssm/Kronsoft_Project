# AgoraCampus Backend

Spring Boot backend for AgoraCampus.

## Requirements

- Java 21
- Maven
- PostgreSQL running locally
- A PostgreSQL database created for the project

## Database Configuration

The database settings are read from `AgoraCampus/src/main/resources/application.yaml`.

Current defaults:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/agora_campus}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

That means the app uses these values unless you override them:

- Database URL: `jdbc:postgresql://localhost:5432/agora_campus`
- Username: `postgres`
- Password: `postgres`

If your local database name, username, or password is different, set these environment variables before running the server.

PowerShell example:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/your_database_name"
$env:DB_USERNAME="your_username"
$env:DB_PASSWORD="your_password"
```

Example using the default PostgreSQL username and a custom database:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/agora_campus"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_postgres_password"
```

## Run The Server

From the project root:

```powershell
cd AgoraCampus
mvn spring-boot:run
```

If the server starts successfully, it runs on:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Run Tests

From `AgoraCampus`:

```powershell
mvn test
```

If your PostgreSQL password is not `postgres`, set `DB_PASSWORD` first:

```powershell
$env:DB_PASSWORD="your_postgres_password"
mvn test
```

## Common Issue: Port 8080 Already In Use

If `mvn spring-boot:run` fails but Swagger still opens in the browser, another copy of the app is probably already running.

Find the process:

```powershell
netstat -ano | Select-String ':8080'
```

Stop it by replacing `<PID>` with the process id from the command above:

```powershell
taskkill /PID <PID> /F
```

Or run the server on another port:

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

Then open:

```text
http://localhost:8081/swagger-ui/index.html
```
