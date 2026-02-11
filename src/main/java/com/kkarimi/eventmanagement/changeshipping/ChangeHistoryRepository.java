package com.kkarimi.eventmanagement.changeshipping;

import org.springframework.data.mongodb.repository.MongoRepository;

interface ChangeHistoryRepository extends MongoRepository<ChangeHistoryDocument, String> {
}
