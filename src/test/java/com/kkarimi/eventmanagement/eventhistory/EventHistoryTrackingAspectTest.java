package com.kkarimi.eventmanagement.eventhistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventHistoryTrackingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldPublishEventHistoryRecordedEventAfterSuccessfulExecution() throws Throwable {
        EventHistoryTrackingAspect aspect = new EventHistoryTrackingAspect(eventPublisher, new ObjectMapper());
        Method method = TestTarget.class.getDeclaredMethod("trackedMethod");
        TrackEventHistory annotation = method.getAnnotation(TrackEventHistory.class);

        when(joinPoint.proceed()).thenReturn("ok");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"payload"});

        Object result = aspect.captureEventHistory(joinPoint, annotation);

        assertEquals("ok", result);

        ArgumentCaptor<EventHistoryRecordedEvent> captor = ArgumentCaptor.forClass(EventHistoryRecordedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        EventHistoryRecordedEvent event = captor.getValue();
        assertEquals("registration", event.module());
        assertEquals("create", event.action());
        assertEquals("registration", event.entity());
        assertNotNull(event.occurredAt());
        assertNotNull(event.payload());
        assertNotNull(event.result());
    }

    private static class TestTarget {
        @TrackEventHistory(module = "registration", action = "create", entity = "registration")
        void trackedMethod() {
        }
    }
}
