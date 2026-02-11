package com.kkarimi.eventmanagement.eventhistory;

import org.springframework.data.mongodb.repository.MongoRepository;

interface EventHistoryRepository extends MongoRepository<EventHistoryDocument, String> {
}
