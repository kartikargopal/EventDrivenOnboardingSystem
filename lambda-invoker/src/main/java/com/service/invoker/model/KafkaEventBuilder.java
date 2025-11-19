package com.lambda.invoker.model;

import com.amazonaws.services.lambda.runtime.events.KafkaEvent;

import java.util.Collections;

public class KafkaEventBuilder {

    public static KafkaEvent build(String topic, String payload) {
        KafkaEvent event = new KafkaEvent();
        KafkaEvent.KafkaEventRecord record = new KafkaEvent.KafkaEventRecord();
        record.setValue(payload);

        event.setRecords(
                Collections.singletonMap(
                        topic,
                        Collections.singletonList(record)
                )
        );
        return event;
    }
}
