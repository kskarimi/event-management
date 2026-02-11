package com.kkarimi.eventmanagement.changeshipping;

import java.time.Instant;

public record DataChangedEvent(
        String module,
        String action,
        String entity,
        Instant occurredAt,
        String payload,
        String result
) {
}
