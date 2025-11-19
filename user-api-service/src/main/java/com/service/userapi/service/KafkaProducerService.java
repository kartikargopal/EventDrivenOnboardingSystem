package com.service.userapi.service;
import com.service.userapi.model.UserCreatedEvent;
import com.service.userapi.model.UserDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.user-created}")
    private String userCreatedTopic;

    @Value("${kafka.topic.user-deleted}") // <-- ADD THIS
    private String userDeletedTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;

    }

    /**
     * Publishes a generic event to a specified Kafka topic.
     * This is used by the OutboxEventPoller.
     *
     * @param topic   The destination topic.
     * @param key     The message key.
     * @param payload The message payload (already serialized as a JSON string).
     */
    public void publish(String topic, String key, String payload) {
        logger.info("Publishing generic event to topic {}: key={}", topic, key);
        try {
            // The payload is already a JSON string from the outbox,
            // so we send it directly.
            kafkaTemplate.send(topic, key, payload);
        } catch (Exception e) {
            logger.error("Failed to publish generic event to topic {}: {}", topic, e.getMessage());
            // Re-throw the exception so the OutboxEventPoller's transaction
            // will roll back, and the event will not be deleted.
            throw new RuntimeException("Kafka publishing failed", e);
        }
    }


    public void publishUserCreatedEvent(UserCreatedEvent event) {
        logger.info("Publishing UserCreatedEvent for userId: {}", event.getUserId());
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(userCreatedTopic, event.getUserId(), event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Successfully published event for userId: {} to topic: {}",
                        event.getUserId(), userCreatedTopic);
            } else {
                logger.error("Failed to publish event for userId: {}",
                        event.getUserId(), ex);
            }
        });
    }

    public void publishUserDeletedEvent(UserDeletedEvent event) {
        logger.info("Publishing UserDeletedEvent for userId: {}", event.getUserId());
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(userDeletedTopic, event.getUserId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Successfully published UserDeletedEvent for userId: {}", event.getUserId());
            } else {
                logger.error("Failed to publish UserDeletedEvent for userId: {}", event.getUserId(), ex);
            }
        });
    }


}
