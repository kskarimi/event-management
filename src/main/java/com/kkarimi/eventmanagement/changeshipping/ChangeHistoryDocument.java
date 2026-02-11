package com.kkarimi.eventmanagement.changeshipping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "change_history")
class ChangeHistoryDocument {

    @Id
    private String id;

    private String module;

    private String action;

    private String entity;

    private Instant occurredAt;

    private String payload;

    private String result;
}
