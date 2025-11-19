package com.service.userapi.outbox;

import com.service.userapi.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.kafka.core.KafkaTemplate;
import com.service.userapi.outbox.deadletter.DeadLetterEvent;
import com.service.userapi.outbox.deadletter.DeadLetterEventRepository;

@Service
public class EventProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(EventProcessingService.class);

    private final KafkaProducerService kafkaProducerService;
    private final OutboxEventRepository outboxRepository;
    private final DeadLetterEventRepository deadLetterRepository;

    public EventProcessingService(KafkaProducerService kafkaProducerService,
                                  OutboxEventRepository outboxRepository,
                                  DeadLetterEventRepository deadLetterRepository) {
        this.kafkaProducerService = kafkaProducerService;
        this.outboxRepository = outboxRepository;
        this.deadLetterRepository = deadLetterRepository;
    }

    /**
     * Handles the "happy path" in a single transaction.
     * If KafkaProducerService.publish() throws an exception, this transaction
     * will roll back, and the event will NOT be deleted from the outbox.
     * The poller will then catch the exception.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void processAndPublishEvent(OutboxEvent event) {
        logger.info("Processing event: {}", event.getId());

        // 1. Publish to Kafka (this is synchronous and will throw on failure)
        kafkaProducerService.publish(
                event.getTopic(),
                event.getKey(),
                event.getPayload()
        );

        // 2. Delete from outbox ONLY if publish was successful
        outboxRepository.delete(event);
        logger.info("Successfully processed and deleted event: {}", event.getId());
    }

    /**
     * Handles the "sad path" in its OWN NEW transaction.
     * This ensures that even if the main processing fails,
     * the dead-lettering is an atomic operation.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void moveToDeadLetterQueue(OutboxEvent event, String errorMessage) {
        // 1. Create a dead-letter record
        DeadLetterEvent dlEvent = new DeadLetterEvent(
                event.getId(), // Assuming you add this field to OutboxEvent
                event.getTopic(),
                event.getKey(),
                event.getPayload(),
                errorMessage
        );
        deadLetterRepository.save(dlEvent);

        // 2. Remove the poison pill from the main outbox
        outboxRepository.delete(event);

        logger.info("Successfully moved poison pill event {} to dead-letter table.", event.getId());
    }
}