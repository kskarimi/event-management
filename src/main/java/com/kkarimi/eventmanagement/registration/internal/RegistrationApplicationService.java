package com.kkarimi.eventmanagement.registration.internal;

import com.kkarimi.eventmanagement.attendees.AttendeeDirectory;
import com.kkarimi.eventmanagement.events.EventCatalog;
import com.kkarimi.eventmanagement.notifications.NotificationGateway;
import com.kkarimi.eventmanagement.registration.Registration;
import com.kkarimi.eventmanagement.registration.RegistrationApplication;
import com.kkarimi.eventmanagement.registration.RegistrationCommand;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
class RegistrationApplicationService implements RegistrationApplication {

    private final EventCatalog eventCatalog;
    private final AttendeeDirectory attendeeDirectory;
    private final NotificationGateway notificationGateway;
    private final RegistrationJpaRepository repository;
    private final Counter registrationCreatedCounter;
    private final Counter registrationFailedCounter;
    private final Timer registrationDurationTimer;

    RegistrationApplicationService(
            EventCatalog eventCatalog,
            AttendeeDirectory attendeeDirectory,
            NotificationGateway notificationGateway,
            RegistrationJpaRepository repository,
            MeterRegistry meterRegistry
    ) {
        this.eventCatalog = eventCatalog;
        this.attendeeDirectory = attendeeDirectory;
        this.notificationGateway = notificationGateway;
        this.repository = repository;
        this.registrationCreatedCounter = Counter.builder("registration.created.total")
                .description("Total number of successful registrations")
                .register(meterRegistry);
        this.registrationFailedCounter = Counter.builder("registration.failed.total")
                .description("Total number of failed registration attempts")
                .register(meterRegistry);
        this.registrationDurationTimer = Timer.builder("registration.process.duration")
                .description("Time taken to process a registration")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public Registration register(RegistrationCommand command) {
        return registrationDurationTimer.record(() -> {
            try {
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
                repository.save(new RegistrationJpaEntity(
                        registration.id(),
                        registration.eventId(),
                        registration.attendeeId(),
                        registration.registeredAt()
                ));
                notificationGateway.sendRegistrationConfirmation(registration);
                registrationCreatedCounter.increment();
                return registration;
            } catch (RuntimeException ex) {
                registrationFailedCounter.increment();
                throw ex;
            }
        });
    }

    @Override
    public List<Registration> findAll() {
        return repository.findAll().stream()
                .map(this::toModel)
                .toList();
    }

    private Registration toModel(RegistrationJpaEntity entity) {
        return new Registration(
                entity.getId(),
                entity.getEventId(),
                entity.getAttendeeId(),
                entity.getRegisteredAt()
        );
    }
}
