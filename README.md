# Event Management (Spring Modulith)

Modular monolith for event management with async event history tracking, observability, resilience, and persistence.

## Stack
- Java 25
- Spring Boot 3.5
- Spring Modulith
- Maven
- MariaDB (primary data)
- Redis (cache)
- MongoDB (change history)
- Liquibase (DB migrations)
- Micrometer + Actuator + Prometheus endpoint
- Resilience4j (circuit breaker)
- Lombok + MapStruct

## Main Docs
- Architecture: `docs/ARCHITECTURE.md`
- OpenAPI docs: `docs/openapi.yaml`
- API usage: `docs/API.md`
- Release notes: `docs/RELEASE_NOTES.md`


## Project Coordinates
- `groupId`: `com.kkarimi`
- `artifactId`: `event-management`
- `version`: `0.0.1-SNAPSHOT`

## Run Locally
```bash
mvn clean test
mvn spring-boot:run
```

Base API path: `/api`
OpenAPI JSON: `/v3/api-docs`
Swagger UI: `/swagger-ui.html`

## Docker Compose
```bash
docker compose up --build
```

Services:
- App: `http://localhost:8080`
- MariaDB: `localhost:3306`
- Redis: `localhost:6379`
- MongoDB: `localhost:27017`

## DataSource Defaults
- Hikari pool name: `EventManagementHikariPool`
- Hikari minimum idle: `5`
- Hikari maximum pool size: `20`
- Hikari connection timeout: `30000ms`
- JDBC connect timeout: `5000ms`
- JDBC read/socket timeout: `30000ms`

## Database Auditing
- JPA auditing is enabled for core tables: `events`, `attendees`, `registrations`
- Audit columns:
  - `created_at`
  - `updated_at`
- Audit values are populated automatically on insert/update.

## Redis Cache Defaults
- Cache values use JSON serialization (`GenericJackson2JsonRedisSerializer`)
- Avoids Java `Serializable` requirement for cached domain records

## Observability and Resilience
- Actuator endpoints:
  - `/actuator/health`
  - `/actuator/metrics`
  - `/actuator/prometheus`
  - `/actuator/circuitbreakers`
- Logging:
  - Structured console format with `traceId` and `spanId`
  - Root log level is `ERROR`
  - Spring framework logs are disabled in console (`org.springframework=OFF`)
  - Spring banner/startup-info logs are disabled
  - HTTP request logs include method, path, request body, response status, and duration
  - Request body log size is capped (default `2000` chars, configurable via `APP_LOGGING_HTTP_MAX_BODY_LENGTH`)
  - Startup summary logs include startup duration, runtime, profiles, and key endpoints/configs
  - Response trace headers: `X-Trace-Id`, `X-Span-Id`
- Circuit breaker name for notifications: `notificationService`
- Customer API rate limit:
  - Fixed window, per client IP
  - Default: `60 requests/minute`
  - Applied on `/api/attendees/**` and `/api/registrations/**`
  - Exceed response: HTTP `429`

## CI/Release
Workflow:
- `.github/workflows/maven-ci-cd.yml`

Behavior:
- CI on push/PR:
  - Build job: `mvn clean package -DskipTests`
  - Test/style job: `mvn verify` (tests + Checkstyle)
- Release on version tags (`v*`):
  - `mvn clean deploy -DskipTests`
  - Publish Maven package to GitHub Packages
  - Publish GitHub Release with generated notes + JAR asset

## GitHub Packages (Local Deploy)
`pom.xml` is already configured to deploy to:
- `https://maven.pkg.github.com/kskarimi/event-management`

To authenticate local Maven deploy:
1. Create a GitHub Personal Access Token with:
   - `read:packages`
   - `write:packages`
   - `repo` (if repository is private)
2. Copy project template:
   - `.mvn/settings-example.xml` -> `~/.m2/settings.xml`
3. Replace `YOUR_GITHUB_PAT` with your token.
4. Run:
```bash
mvn -DskipTests deploy
```

## Tests
- Unit tests are available for:
  - core service logic (`events`, `attendees`, `registration`)
  - data change tracking aspect (`eventhistory`)
  - customer API rate-limit filter
  - trace context initialization filter (trace/span propagation)
