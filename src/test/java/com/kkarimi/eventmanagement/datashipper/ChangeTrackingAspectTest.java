package com.kkarimi.eventmanagement.datashipper;

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
class ChangeTrackingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldPublishDataChangedEventAfterSuccessfulExecution() throws Throwable {
        ChangeTrackingAspect aspect = new ChangeTrackingAspect(eventPublisher, new ObjectMapper());
        Method method = TestTarget.class.getDeclaredMethod("trackedMethod");
        TrackDataChange annotation = method.getAnnotation(TrackDataChange.class);

        when(joinPoint.proceed()).thenReturn("ok");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"payload"});

        Object result = aspect.captureChange(joinPoint, annotation);

        assertEquals("ok", result);

        ArgumentCaptor<DataChangedEvent> captor = ArgumentCaptor.forClass(DataChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        DataChangedEvent event = captor.getValue();
        assertEquals("registration", event.module());
        assertEquals("create", event.action());
        assertEquals("registration", event.entity());
        assertNotNull(event.occurredAt());
        assertNotNull(event.payload());
        assertNotNull(event.result());
    }

    private static class TestTarget {
        @TrackDataChange(module = "registration", action = "create", entity = "registration")
        void trackedMethod() {
        }
    }
}
