# Release Notes

## Current Release

### Highlights
- Built a modular event-management backend with Spring Modulith.
- Added persistent storage with MariaDB and Liquibase-managed schema migrations.
- Added Redis caching for event read operations.
- Added MongoDB-backed `datashipper` module for async historical change tracking.
- Added AOP-based metrics collection with Micrometer and Actuator endpoints.
- Added Resilience4j circuit breaker on notification external-call boundary.
- Added per-IP customer API rate limiting for attendee/registration endpoints.
- Added Lombok + MapStruct to reduce boilerplate and centralize mappings.
- Added Docker Compose support for app, MariaDB, Redis, and MongoDB.
- Added GitHub Actions CI and tag-based release workflow with GitHub Packages publish.
- Added unit tests for core services, rate-limiting filter, and change-tracking aspect.

### Runtime Endpoints
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`
- `/actuator/circuitbreakers`

### Compatibility Notes
- Java target is 25.
- Modulith structure test is conditionally skipped on Java 25 due to current ArchUnit bytecode support gap.
