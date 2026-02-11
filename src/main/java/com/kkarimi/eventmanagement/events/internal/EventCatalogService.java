package com.kkarimi.eventmanagement.events.internal;

import com.kkarimi.eventmanagement.events.Event;
import com.kkarimi.eventmanagement.events.EventCatalog;
import com.kkarimi.eventmanagement.events.NewEventCommand;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
class EventCatalogService implements EventCatalog {

    private final EventJpaRepository repository;
    private final Counter eventCreatedCounter;
    private final Timer eventCreateTimer;
    private final Timer eventLookupTimer;

    EventCatalogService(EventJpaRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.eventCreatedCounter = Counter.builder("event.created.total")
                .description("Total number of created events")
                .register(meterRegistry);
        this.eventCreateTimer = Timer.builder("event.create.duration")
                .description("Time taken to create an event")
                .register(meterRegistry);
        this.eventLookupTimer = Timer.builder("event.lookup.duration")
                .description("Time taken to read events")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"eventById", "eventList"}, allEntries = true)
    public Event create(NewEventCommand command) {
        return eventCreateTimer.record(() -> {
            if (command.capacity() <= 0) {
                throw new IllegalArgumentException("Event capacity must be greater than zero");
            }
            UUID id = UUID.randomUUID();
            EventJpaEntity entity = new EventJpaEntity(
                    id,
                    command.title(),
                    command.startsAt(),
                    command.capacity(),
                    0
            );
            Event created = toModel(repository.save(entity));
            eventCreatedCounter.increment();
            return created;
        });
    }

    @Override
    @Cacheable(cacheNames = "eventById", key = "#eventId")
    public Optional<Event> findById(UUID eventId) {
        return eventLookupTimer.record(() -> repository.findById(eventId).map(this::toModel));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"eventById", "eventList"}, allEntries = true)
    public Event reserveSeat(UUID eventId) {
        EventJpaEntity entity = repository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        if (entity.getReservedSeats() >= entity.getCapacity()) {
            throw new IllegalStateException("No seat available for event: " + eventId);
        }

        entity.setReservedSeats(entity.getReservedSeats() + 1);
        return toModel(entity);
    }

    @Override
    @Cacheable(cacheNames = "eventList")
    public List<Event> findAll() {
        return eventLookupTimer.record(() -> repository.findAll().stream()
                .map(this::toModel)
                .sorted(Comparator.comparing(Event::startsAt))
                .toList());
    }

    private Event toModel(EventJpaEntity entity) {
        return new Event(
                entity.getId(),
                entity.getTitle(),
                entity.getStartsAt(),
                entity.getCapacity(),
                entity.getReservedSeats()
        );
    }
}
