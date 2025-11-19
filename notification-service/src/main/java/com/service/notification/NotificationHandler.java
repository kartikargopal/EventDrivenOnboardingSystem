package com.service.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.service.notification.client.ProfileApiClient;
import com.service.notification.model.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationHandler implements RequestHandler<KafkaEvent, Void> {

    private static final Logger log = LoggerFactory.getLogger(NotificationHandler.class);

    private final ObjectMapper mapper;
    private final ProfileApiClient profileApiClient;

    public NotificationHandler() {
        this(defaultObjectMapper(), new ProfileApiClient());
    }

    public NotificationHandler(ObjectMapper mapper, ProfileApiClient profileApiClient) {
        this.mapper = mapper;
        this.profileApiClient = profileApiClient;
    }

    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Override
    public Void handleRequest(KafkaEvent event, Context context) {
        var lambdaLogger = context.getLogger();
        lambdaLogger.log("[notification] Received KafkaEvent with " + event.getRecords().size() + " partition(s)\n");
        log.info("Lambda invoked with {} partitions", event.getRecords().size());

        event.getRecords().forEach((topicPartition, records) -> {
            log.info("Processing topicPartition={} records={}", topicPartition, records.size());
            for (KafkaEvent.KafkaEventRecord record : records) {
                try {
                    processSingleRecord(record.getValue(), context);
                } catch (Exception ex) {
                    lambdaLogger.log("[notification] ERROR processing record on " + topicPartition + ": " + ex.getMessage() + "\n");
                    log.error("Error processing record on {}: {}", topicPartition, ex.getMessage(), ex);
                    // In a real system, you'd send this to a DLQ instead
                }
            }
        });

        return null;
    }

    private void processSingleRecord(String payload, Context context) throws Exception {
        var lambdaLogger = context.getLogger();
        lambdaLogger.log("[notification] raw payload: " + payload + "\n");
        log.debug("Processing payload: {}", payload);

        UserCreatedEvent event = mapper.readValue(payload, UserCreatedEvent.class);

        lambdaLogger.log("[notification] processing userId=" + event.getUserId() + "\n");
        log.info("Processing userId={} username={}", event.getUserId(), event.getUsername());

        profileApiClient.createProfile(event);

        String msg = String.format(
                "[notification] Welcome %s %s (@%s) - an email simulated.\n",
                event.getFirstName(), event.getLastName(), event.getUsername()
        );
        lambdaLogger.log(msg);
        log.info("Notification simulated for userId={}", event.getUserId());
    }
}
