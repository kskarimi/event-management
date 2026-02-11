package com.kkarimi.eventmanagement.registration.internal;

import com.kkarimi.eventmanagement.attendees.AttendeeDirectory;
import com.kkarimi.eventmanagement.events.EventCatalog;
import com.kkarimi.eventmanagement.metrics.MeasuredOperation;
import com.kkarimi.eventmanagement.notifications.NotificationGateway;
import com.kkarimi.eventmanagement.registration.Registration;
import com.kkarimi.eventmanagement.registration.RegistrationApplication;
import com.kkarimi.eventmanagement.registration.RegistrationCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class RegistrationApplicationService implements RegistrationApplication {

    private final EventCatalog eventCatalog;
    private final AttendeeDirectory attendeeDirectory;
    private final NotificationGateway notificationGateway;
    private final RegistrationJpaRepository repository;
    private final RegistrationMapper mapper;

    @Override
    @Transactional
    @MeasuredOperation(
            timer = "registration.process.duration",
            successCounter = "registration.created.total",
            failureCounter = "registration.failed.total"
    )
    public Registration register(RegistrationCommand command) {
        eventCatalog.findById(command.eventId())
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + command.eventId()));

        attendeeDirectory.findById(command.attendeeId())
                .orElseThrow(() -> new NoSuchElementException("Attendee not found: " + command.attendeeId()));

        eventCatalog.reserveSeat(command.eventId());

        Registration registration = new Registration(
                UUID.randomUUID(),
                command.eventId(),
                command.attendeeId(),
                Instant.now()
        );
        repository.save(mapper.toEntity(registration));
        notificationGateway.sendRegistrationConfirmation(registration);
        return registration;
    }

    @Override
    public List<Registration> findAll() {
        return repository.findAll().stream()
                .map(mapper::toModel)
                .toList();
    }
}
