package com.kkarimi.eventmanagement.registration.internal;

import com.kkarimi.eventmanagement.attendees.Attendee;
import com.kkarimi.eventmanagement.attendees.AttendeeDirectory;
import com.kkarimi.eventmanagement.events.Event;
import com.kkarimi.eventmanagement.events.EventCatalog;
import com.kkarimi.eventmanagement.notifications.NotificationGateway;
import com.kkarimi.eventmanagement.registration.Registration;
import com.kkarimi.eventmanagement.registration.RegistrationCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationApplicationServiceTest {

    @Mock
    private EventCatalog eventCatalog;

    @Mock
    private AttendeeDirectory attendeeDirectory;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private RegistrationJpaRepository repository;

    @Mock
    private RegistrationMapper mapper;

    @InjectMocks
    private RegistrationApplicationService service;

    @Test
    void registerShouldPersistAndNotify() {
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();
        RegistrationCommand command = new RegistrationCommand(eventId, attendeeId);

        when(eventCatalog.findById(eventId)).thenReturn(Optional.of(new Event(eventId, "E", LocalDateTime.now().plusDays(1), 10, 1)));
        when(attendeeDirectory.findById(attendeeId)).thenReturn(Optional.of(new Attendee(attendeeId, "Karim", "k@example.com")));
        when(mapper.toEntity(any(Registration.class))).thenAnswer(inv -> {
            Registration r = inv.getArgument(0);
            return new RegistrationJpaEntity(r.id(), r.eventId(), r.attendeeId(), r.registeredAt());
        });

        Registration result = service.register(command);

        assertEquals(eventId, result.eventId());
        assertEquals(attendeeId, result.attendeeId());

        ArgumentCaptor<RegistrationJpaEntity> captor = ArgumentCaptor.forClass(RegistrationJpaEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(eventId, captor.getValue().getEventId());
        assertEquals(attendeeId, captor.getValue().getAttendeeId());
        verify(notificationGateway).sendRegistrationConfirmation(result);
        verify(eventCatalog).reserveSeat(eventId);
    }

    @Test
    void registerShouldFailWhenEventMissing() {
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();

        when(eventCatalog.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.register(new RegistrationCommand(eventId, attendeeId)));

        verify(attendeeDirectory, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void registerShouldFailWhenAttendeeMissing() {
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();

        when(eventCatalog.findById(eventId)).thenReturn(Optional.of(new Event(eventId, "E", LocalDateTime.now().plusDays(1), 10, 0)));
        when(attendeeDirectory.findById(attendeeId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.register(new RegistrationCommand(eventId, attendeeId)));

        verify(repository, never()).save(any());
        verify(notificationGateway, never()).sendRegistrationConfirmation(any());
    }

    @Test
    void findAllShouldMapEntities() {
        RegistrationJpaEntity e1 = new RegistrationJpaEntity(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        RegistrationJpaEntity e2 = new RegistrationJpaEntity(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        Registration m1 = new Registration(e1.getId(), e1.getEventId(), e1.getAttendeeId(), e1.getRegisteredAt());
        Registration m2 = new Registration(e2.getId(), e2.getEventId(), e2.getAttendeeId(), e2.getRegisteredAt());

        when(repository.findAll()).thenReturn(List.of(e1, e2));
        when(mapper.toModel(e1)).thenReturn(m1);
        when(mapper.toModel(e2)).thenReturn(m2);

        List<Registration> result = service.findAll();

        assertEquals(List.of(m1, m2), result);
    }
}
