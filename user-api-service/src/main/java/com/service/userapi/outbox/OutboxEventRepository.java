package com.service.userapi.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {

    // Find a small batch of events to process, oldest first
    List<OutboxEvent> findTop10ByOrderByTimestampAsc();
}