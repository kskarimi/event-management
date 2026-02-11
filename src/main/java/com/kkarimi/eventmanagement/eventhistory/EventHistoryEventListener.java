package com.kkarimi.eventmanagement.eventhistory;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class EventHistoryEventListener {

    private final EventHistoryRepository repository;

    @Async
    @EventListener
    public void onEventHistoryRecorded(EventHistoryRecordedEvent event) {
        repository.save(EventHistoryDocument.builder()
                .module(event.module())
                .action(event.action())
                .entity(event.entity())
                .occurredAt(event.occurredAt())
                .payload(event.payload())
                .result(event.result())
                .build());
    }
}
