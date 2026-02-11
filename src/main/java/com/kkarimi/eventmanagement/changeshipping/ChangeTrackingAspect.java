package com.kkarimi.eventmanagement.changeshipping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
class ChangeTrackingAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Around("@annotation(trackDataChange)")
    public Object captureChange(ProceedingJoinPoint joinPoint, TrackDataChange trackDataChange) throws Throwable {
        Object result = joinPoint.proceed();

        DataChangedEvent event = new DataChangedEvent(
                trackDataChange.module(),
                trackDataChange.action(),
                trackDataChange.entity(),
                Instant.now(),
                toJson(joinPoint.getArgs()),
                toJson(result)
        );
        eventPublisher.publishEvent(event);
        return result;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"serialization_failed\"}";
        }
    }
}
