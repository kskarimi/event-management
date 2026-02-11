package com.kkarimi.eventmanagement.eventhistory;

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
class EventHistoryTrackingAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Around("@annotation(trackEventHistory)")
    public Object captureEventHistory(
            ProceedingJoinPoint joinPoint,
            TrackEventHistory trackEventHistory
    ) throws Throwable {
        Object result = joinPoint.proceed();

        EventHistoryRecordedEvent event = new EventHistoryRecordedEvent(
                trackEventHistory.module(),
                trackEventHistory.action(),
                trackEventHistory.entity(),
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
