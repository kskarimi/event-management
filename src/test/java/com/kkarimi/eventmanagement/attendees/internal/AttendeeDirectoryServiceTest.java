package com.kkarimi.eventmanagement.attendees.internal;

import com.kkarimi.eventmanagement.attendees.Attendee;
import com.kkarimi.eventmanagement.attendees.NewAttendeeCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendeeDirectoryServiceTest {

    @Mock
    private AttendeeJpaRepository repository;

    @Mock
    private AttendeeMapper mapper;

    @InjectMocks
    private AttendeeDirectoryService service;

    @Test
    void registerShouldPersistAndReturnMappedModel() {
        NewAttendeeCommand command = new NewAttendeeCommand("Karim", "karim@example.com");
        UUID id = UUID.randomUUID();
        AttendeeJpaEntity entity = new AttendeeJpaEntity(id, command.fullName(), command.email());
        Attendee model = new Attendee(id, command.fullName(), command.email());

        when(mapper.toEntity(any(UUID.class), any(NewAttendeeCommand.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toModel(entity)).thenReturn(model);

        Attendee result = service.register(command);

        assertEquals(model, result);
    }

    @Test
    void findByIdShouldReturnMappedValue() {
        UUID id = UUID.randomUUID();
        AttendeeJpaEntity entity = new AttendeeJpaEntity(id, "Karim", "karim@example.com");
        Attendee model = new Attendee(id, "Karim", "karim@example.com");

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toModel(entity)).thenReturn(model);

        Optional<Attendee> result = service.findById(id);

        assertEquals(Optional.of(model), result);
    }

    @Test
    void findAllShouldMapAllAttendees() {
        AttendeeJpaEntity e1 = new AttendeeJpaEntity(UUID.randomUUID(), "A", "a@example.com");
        AttendeeJpaEntity e2 = new AttendeeJpaEntity(UUID.randomUUID(), "B", "b@example.com");
        Attendee m1 = new Attendee(e1.getId(), "A", "a@example.com");
        Attendee m2 = new Attendee(e2.getId(), "B", "b@example.com");

        when(repository.findAll()).thenReturn(List.of(e1, e2));
        when(mapper.toModel(e1)).thenReturn(m1);
        when(mapper.toModel(e2)).thenReturn(m2);

        List<Attendee> result = service.findAll();

        assertEquals(List.of(m1, m2), result);
    }
}
