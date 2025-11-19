package com.service.userapi.outbox.deadletter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a failed event that could not be processed
 * and has been moved to the dead-letter collection for manual inspection.
 */
@Document("dead_letter_events")
public class DeadLetterEvent {

    @Id
    private String id;

    // --- Original Event Data ---
    private String aggregateId; // The ID of the entity (e.g., User ID)
    private String topic;         // The intended Kafka topic
    private String eventKey;      // The Kafka message key
    private String payload;       // The JSON payload of the original event

    // --- Error Information ---
    private String errorMessage;  // The exception message that caused the failure
    private Instant failedTimestamp; // When the failure was processed

    /**
     * No-args constructor for Spring Data deserialization.
     */
    public DeadLetterEvent() {
    }

    /**
     * Creates a new DeadLetterEvent from a failed OutboxEvent.
     */
    public DeadLetterEvent(String aggregateId, String topic, String eventKey, String payload, String errorMessage) {
        this.id = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.topic = topic;
        this.eventKey = eventKey;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.failedTimestamp = Instant.now();
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getTopic() {
        return topic;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getPayload() {
        return payload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getFailedTimestamp() {
        return failedTimestamp;
    }
}