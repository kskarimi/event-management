package com.kkarimi.eventmanagement.events.internal;

import com.kkarimi.eventmanagement.events.Event;
import com.kkarimi.eventmanagement.events.NewEventCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventCatalogServiceTest {

    @Mock
    private EventJpaRepository repository;

    @Mock
    private EventMapper mapper;

    @InjectMocks
    private EventCatalogService service;

    @Test
    void createShouldPersistAndReturnMappedModel() {
        NewEventCommand command = new NewEventCommand("Event", LocalDateTime.now().plusDays(1), 10);
        EventJpaEntity entity = new EventJpaEntity(UUID.randomUUID(), "Event", command.startsAt(), 10, 0, null);
        Event model = new Event(entity.getId(), "Event", command.startsAt(), 10, 0);

        when(mapper.toEntity(any(UUID.class), any(NewEventCommand.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toModel(entity)).thenReturn(model);

        Event result = service.create(command);

        assertEquals(model, result);
    }

    @Test
    void createShouldRejectNonPositiveCapacity() {
        NewEventCommand command = new NewEventCommand("Event", LocalDateTime.now().plusDays(1), 0);

        assertThrows(IllegalArgumentException.class, () -> service.create(command));
    }

    @Test
    void findByIdShouldReturnMappedEvent() {
        UUID id = UUID.randomUUID();
        EventJpaEntity entity = new EventJpaEntity(id, "Event", LocalDateTime.now().plusDays(1), 10, 0, null);
        Event model = new Event(id, "Event", entity.getStartsAt(), 10, 0);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toModel(entity)).thenReturn(model);

        Optional<Event> result = service.findById(id);

        assertEquals(Optional.of(model), result);
    }

    @Test
    void reserveSeatShouldIncrementReservedSeats() {
        UUID id = UUID.randomUUID();
        EventJpaEntity entity = new EventJpaEntity(id, "Event", LocalDateTime.now().plusDays(1), 10, 2, null);
        Event model = new Event(id, "Event", entity.getStartsAt(), 10, 3);

        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(entity));
        when(mapper.toModel(entity)).thenReturn(model);

        Event result = service.reserveSeat(id);

        assertEquals(3, entity.getReservedSeats());
        assertEquals(model, result);
    }

    @Test
    void reserveSeatShouldFailWhenCapacityReached() {
        UUID id = UUID.randomUUID();
        EventJpaEntity entity = new EventJpaEntity(id, "Event", LocalDateTime.now().plusDays(1), 2, 2, null);

        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class, () -> service.reserveSeat(id));
    }

    @Test
    void findAllShouldReturnSortedByStartDate() {
        LocalDateTime late = LocalDateTime.now().plusDays(2);
        LocalDateTime early = LocalDateTime.now().plusDays(1);
        EventJpaEntity e1 = new EventJpaEntity(UUID.randomUUID(), "Late", late, 10, 0, null);
        EventJpaEntity e2 = new EventJpaEntity(UUID.randomUUID(), "Early", early, 10, 0, null);
        Event m1 = new Event(e1.getId(), "Late", late, 10, 0);
        Event m2 = new Event(e2.getId(), "Early", early, 10, 0);

        when(repository.findAll()).thenReturn(List.of(e1, e2));
        when(mapper.toModel(e1)).thenReturn(m1);
        when(mapper.toModel(e2)).thenReturn(m2);

        List<Event> result = service.findAll();

        assertEquals(List.of(m2, m1), result);
    }
}
