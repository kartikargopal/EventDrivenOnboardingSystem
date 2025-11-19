package com.service.userapi.outbox.deadletter;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for DeadLetterEvent entities.
 */
@Repository
public interface DeadLetterEventRepository extends MongoRepository<DeadLetterEvent, String> {
    // Spring Data will provide all the standard find/save/delete methods.
    // You can add custom finders here if needed, e.g.:
    // List<DeadLetterEvent> findByTopic(String topic);
}