# Event Management - Architecture

## Goal
This project demonstrates a production-oriented modular monolith design for interview use:
- clear module boundaries
- async inter-module contract
- resilient external call boundary
- observable runtime behavior

## Base Package
`com.kkarimi.eventmanagement`

## Modules
1. `events`
- Responsibility: create events, query events, reserve seats.
- API: `EventCatalog`, `Event`, `NewEventCommand`.

2. `attendees`
- Responsibility: register attendees and query them.
- API: `AttendeeDirectory`, `Attendee`, `NewAttendeeCommand`.

3. `registration`
- Responsibility: register attendee to event.
- Dependencies: `events`, `attendees`, `notifications`, `datashipper`.
- API: `RegistrationApplication`, `Registration`, `RegistrationCommand`.

4. `notifications`
- Responsibility: notification boundary (external call boundary).
- API: `NotificationGateway`.
- Resilience: Resilience4j circuit breaker (`notificationService`).

5. `datashipper`
- Responsibility: async change contract and historical change storage.
- Contract mechanism: Spring Application Events.
- AOP capture annotation: `@TrackDataChange`.
- Event: `DataChangedEvent`.
- Storage: MongoDB collection `change_history`.

6. `metrics`
- Responsibility: centralized metric instrumentation via AOP.
- AOP annotation: `@MeasuredOperation`.

7. `web`
- Responsibility: REST controllers + request filtering.
- Includes simple customer API rate limiting filter.

## Data and Infrastructure
- MariaDB: system of record for `events`, `attendees`, `registrations`.
- Liquibase: schema migration source of truth.
- Redis: Spring Cache backend.
- MongoDB: change history for `datashipper` module.

Runtime configuration:
- `src/main/resources/application.yml`

Container runtime:
- `docker-compose.yml`

## Async Contract Flow (Data Shipper)
```mermaid
sequenceDiagram
    participant Service as "Domain Service (events/attendees/registration)"
    participant Aspect as "ChangeTrackingAspect"
    participant AppEvent as "ApplicationEventPublisher"
    participant Listener as "ChangeHistoryEventListener (@Async)"
    participant Mongo as "MongoDB change_history"

    Service->>Aspect: Method with @TrackDataChange
    Aspect->>Service: proceed()
    Service-->>Aspect: result
    Aspect->>AppEvent: publish DataChangedEvent
    AppEvent-->>Listener: dispatch event
    Listener->>Mongo: save ChangeHistoryDocument
```

## Registration Flow (Business)
```mermaid
sequenceDiagram
    participant Client
    participant Web as "RegistrationController"
    participant Reg as "Registration Module"
    participant Events as "Events Module"
    participant Attendees as "Attendees Module"
    participant Notif as "Notifications Module"

    Client->>Web: POST /api/registrations
    Web->>Reg: register(command)
    Reg->>Events: findById + reserveSeat
    Reg->>Attendees: findById
    Reg->>Notif: sendRegistrationConfirmation
    Reg-->>Web: Registration
    Web-->>Client: 201 Created
```

## Key Cross-Cutting Patterns
- AOP Metrics: `@MeasuredOperation`
- AOP Change Capture: `@TrackDataChange`
- Async internal contract: Spring application events
- Resilience: circuit breaker for external notification call
- Protection: fixed-window per-IP rate limiting for customer APIs

## Build and Run
```bash
mvn clean test
mvn spring-boot:run
```

## Java 25 Note
`ModularityTest` is conditionally skipped on Java 25 because current ArchUnit support for class-file version 69 is not yet available.
