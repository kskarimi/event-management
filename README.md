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
- API + curl: `docs/API.md`
- Postman collection: `docs/postman/event-management.postman_collection.json`
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

## Docker Compose
```bash
docker compose up --build
```

Services:
- App: `http://localhost:8080`
- MariaDB: `localhost:3306`
- Redis: `localhost:6379`
- MongoDB: `localhost:27017`

## Observability and Resilience
- Actuator endpoints:
  - `/actuator/health`
  - `/actuator/metrics`
  - `/actuator/prometheus`
  - `/actuator/circuitbreakers`
- Logging:
  - Structured console format with `traceId` and `spanId`
  - Root log level is `ERROR`
  - Startup summary logs include startup duration, runtime, profiles, and key endpoints/configs
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
