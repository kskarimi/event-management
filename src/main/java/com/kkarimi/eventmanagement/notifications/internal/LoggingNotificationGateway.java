package com.kkarimi.eventmanagement.notifications.internal;

import com.kkarimi.eventmanagement.notifications.NotificationGateway;
import com.kkarimi.eventmanagement.registration.Registration;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class LoggingNotificationGateway implements NotificationGateway {

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendRegistrationConfirmation")
    public void sendRegistrationConfirmation(Registration registration) {
        // External-call boundary placeholder: replace this with HTTP/email provider call.
        log.info("Confirmation sent for registration {}", registration.id());
    }

    void fallbackSendRegistrationConfirmation(Registration registration, Throwable throwable) {
        log.warn("Notification fallback for registration {} due to: {}", registration.id(), throwable.getMessage());
    }
}
