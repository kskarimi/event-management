package com.kkarimi.eventmanagement.events.internal;

import com.kkarimi.eventmanagement.events.Event;
import com.kkarimi.eventmanagement.events.EventCatalog;
import com.kkarimi.eventmanagement.events.NewEventCommand;
import com.kkarimi.eventmanagement.metrics.MeasuredOperation;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
class EventCatalogService implements EventCatalog {

    private final EventJpaRepository repository;
    private final EventMapper mapper;

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"eventById", "eventList"}, allEntries = true)
    @MeasuredOperation(
            timer = "event.create.duration",
            successCounter = "event.created.total"
    )
    public Event create(NewEventCommand command) {
        if (command.capacity() <= 0) {
            throw new IllegalArgumentException("Event capacity must be greater than zero");
        }
        UUID id = UUID.randomUUID();
        EventJpaEntity entity = mapper.toEntity(id, command);
        return mapper.toModel(repository.save(entity));
    }

    @Override
    @Cacheable(cacheNames = "eventById", key = "#eventId")
    @MeasuredOperation(timer = "event.lookup.duration")
    public Optional<Event> findById(UUID eventId) {
        return repository.findById(eventId).map(mapper::toModel);
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
        return mapper.toModel(entity);
    }

    @Override
    @Cacheable(cacheNames = "eventList")
    @MeasuredOperation(timer = "event.lookup.duration")
    public List<Event> findAll() {
        return repository.findAll().stream()
                .map(mapper::toModel)
                .sorted(Comparator.comparing(Event::startsAt))
                .toList();
    }
}
