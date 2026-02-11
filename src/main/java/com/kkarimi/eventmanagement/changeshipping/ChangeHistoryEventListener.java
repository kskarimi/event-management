package com.kkarimi.eventmanagement.changeshipping;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ChangeHistoryEventListener {

    private final ChangeHistoryRepository repository;

    @Async
    @EventListener
    public void onDataChanged(DataChangedEvent event) {
        repository.save(ChangeHistoryDocument.builder()
                .module(event.module())
                .action(event.action())
                .entity(event.entity())
                .occurredAt(event.occurredAt())
                .payload(event.payload())
                .result(event.result())
                .build());
    }
}
