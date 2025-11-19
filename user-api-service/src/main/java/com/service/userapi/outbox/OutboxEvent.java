package com.service.userapi.outbox;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * Represents an event intended for publication, stored atomically
 * with the business transaction in the 'outbox_events' collection.
 */
@Document(collection = "outbox_events")
public class OutboxEvent {

    @Id
    private String id;

    /**
     * The destination topic for this event (e.g., "user-created-topic").
     */
    private String topic;

    /**
     * The Kafka message key (e.g., the user's ID for partitioning).
     */
    private String key;

    /**
     * The full event payload, typically serialized as a JSON string.
     */
    private String payload;

    /**
     * The timestamp of when the event was created.
     */
    private Instant timestamp;

    /**
     * The status of the event (e.g., PENDING, PUBLISHED).
     * This is optional but can be useful for tracking.
     */
    // private EventStatus status;

    // Default constructor (required by JPA/Jackson)
    public OutboxEvent() {
    }

    /**
     * Primary constructor to create a new pending outbox event.
     *
     * @param topic   The destination Kafka topic.
     * @param key     The Kafka message key.
     * @param payload The serialized event payload (JSON).
     */
    public OutboxEvent(String topic, String key, String payload) {
        this.topic = topic;
        this.key = key;
        this.payload = payload;
        this.timestamp = Instant.now();
        // this.status = EventStatus.PENDING; // If using status
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    // Optional: Enum for status
     public enum EventStatus {
       PENDING,
      PUBLISHED
     }
}