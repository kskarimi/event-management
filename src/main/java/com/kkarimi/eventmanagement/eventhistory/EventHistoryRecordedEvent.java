package com.kkarimi.eventmanagement.eventhistory;

import java.time.Instant;

public record EventHistoryRecordedEvent(
        String module,
        String action,
        String entity,
        Instant occurredAt,
        String payload,
        String result
) {
}
