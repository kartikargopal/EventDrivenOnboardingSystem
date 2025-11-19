package com.lambda.invoker.consumer;

import com.lambda.invoker.service.LambdaTriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LambdaKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(LambdaKafkaConsumer.class);

    private final LambdaTriggerService lambdaTriggerService;

    @Value("${KAFKA_TOPIC:user-created-events}")
    private String topic;

    public LambdaKafkaConsumer(LambdaTriggerService lambdaTriggerService) {
        this.lambdaTriggerService = lambdaTriggerService;
    }

    @KafkaListener(topics = "${KAFKA_TOPIC:user-created-events}", groupId = "${KAFKA_GROUP_ID:lambda-invoker-group}")
    public void onMessage(String message) {
        log.info("lambda-invoker received Kafka message: {}", message);

        lambdaTriggerService.invokeLambdaWithMessage(message);
    }
}
