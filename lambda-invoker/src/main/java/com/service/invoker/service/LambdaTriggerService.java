package com.lambda.invoker.service;

import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.invoker.model.KafkaEventBuilder;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LambdaTriggerService {

    private static final Logger log = LoggerFactory.getLogger(LambdaTriggerService.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${LAMBDA_URL:http://notification-service:8080/2015-03-31/functions/function/invocations}")
    private String lambdaUrl;

    @Value("${KAFKA_TOPIC:user-created-events}")
    private String topic;

    public void invokeLambdaWithMessage(String payload) {
        try {
            KafkaEvent event = KafkaEventBuilder.build(topic, payload);
            String json = mapper.writeValueAsString(event);

            log.info("Invoking local Lambda at {}", lambdaUrl);

            Request.post(lambdaUrl)
                    .bodyString(json, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnContent();

            log.info("Lambda invocation successful for payload: {}", payload);

        } catch (Exception e) {
            log.error("Error invoking lambda", e);
        }
    }
}
