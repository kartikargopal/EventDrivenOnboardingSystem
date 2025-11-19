// File: com.service.userapi.outbox.OutboxEventPoller.java
package com.service.userapi.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OutboxEventPoller {

    private static final Logger logger = LoggerFactory.getLogger(OutboxEventPoller.class);

    private final OutboxEventRepository outboxEventRepository;
    private final EventProcessingService eventProcessingService;

    public OutboxEventPoller(OutboxEventRepository outboxEventRepository,
                             EventProcessingService eventProcessingService) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventProcessingService = eventProcessingService;
    }

    /**
     * Polls the outbox collection and publishes events to Kafka.
     * This method is transactional. If publishing fails, the transaction
     * rolls back, and the event is not deleted, so it will be retried.
     */
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    @Transactional
    public void pollAndPublishEvents() {
        logger.trace("Polling for outbox events...");

        // Fetch a small batch of events to prevent locking the table for too long
        List<OutboxEvent> events = outboxEventRepository.findTop10ByOrderByTimestampAsc();

        if (events.isEmpty()) {
            return; // Nothing to do
        }

        logger.info("Found {} events to publish.", events.size());

        for (OutboxEvent event : events) {
            try {
                // 1. Try to publish to Kafka
                // (You may need a generic publish method on your producer)
                eventProcessingService.processAndPublishEvent(event);

                // 2. If publish is successful, delete the event from the outbox
                outboxEventRepository.delete(event);
                logger.info("Successfully published and deleted event: {}", event.getId());

            } catch (Exception e) {
                // If publish fails, log the error.
                // The @Transactional annotation will roll back to delete.
                // The event will remain in the outbox to be retried next time.
                logger.error("Failed to publish event: {}. It will be retried.", event.getId(), e);

                // 5. Delegate to the dead-letter service
                eventProcessingService.moveToDeadLetterQueue(event, e.getMessage());
            }
        }
    }
}