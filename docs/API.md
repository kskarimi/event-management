# API Documentation

Base URL: `http://localhost:8080`

## Quick Flow
1. Create an event
2. Create an attendee
3. Register attendee in the event

## cURL Examples

### 1. Create Event
```bash
curl -X POST 'http://localhost:8080/api/events' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Spring Modulith Workshop",
    "startsAt": "2026-03-01T10:00:00",
    "capacity": 100
  }'
```

### 2. List Events
```bash
curl 'http://localhost:8080/api/events'
```

### 3. Get Event by ID
```bash
curl 'http://localhost:8080/api/events/{eventId}'
```

### 4. Create Attendee
```bash
curl -X POST 'http://localhost:8080/api/attendees' \
  -H 'Content-Type: application/json' \
  -d '{
    "fullName": "Karim Karimi",
    "email": "karim@example.com"
  }'
```

### 5. List Attendees
```bash
curl 'http://localhost:8080/api/attendees'
```

### 6. Register Attendee in Event
```bash
curl -X POST 'http://localhost:8080/api/registrations' \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": "{eventId}",
    "attendeeId": "{attendeeId}"
  }'
```

### 7. List Registrations
```bash
curl 'http://localhost:8080/api/registrations'
```

## Common HTTP Status Codes
- `201 Created`: Resource created successfully.
- `200 OK`: Request successful.
- `404 Not Found`: Event or attendee not found.
- `409 Conflict`: No seat available for event.

## Monitoring and Metrics
- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

Custom business metrics:
- `event.created.total`
- `event.create.duration`
- `event.lookup.duration`
- `registration.created.total`
- `registration.failed.total`
- `registration.process.duration`
