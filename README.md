# Event Management (Spring Modulith)

Interview-ready modular monolith skeleton using Spring Boot + Spring Modulith + Maven.

## Stack
- Java 25
- Spring Boot
- Spring Modulith
- Maven

## Architecture
See: `/Users/karim/Public/event-management/docs/ARCHITECTURE.md`

## API Docs
- cURL examples: `/Users/karim/Public/event-management/docs/API.md`
- Postman collection: `/Users/karim/Public/event-management/docs/postman/event-management.postman_collection.json`

## Quick Start
```bash
mvn clean test
mvn spring-boot:run
```

API base path: `/api`

Actuator endpoints:
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`

## Docker Compose (MariaDB + Redis + App)
```bash
docker compose up --build
```

Services:
- App: `http://localhost:8080`
- MariaDB: `localhost:3306`
- Redis: `localhost:6379`
