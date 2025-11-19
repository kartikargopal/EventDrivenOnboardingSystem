package com.service.profileapi.e2e;

import com.amazonaws.services.lambda.runtime.events.KafkaEvent;

import java.util.List;
import java.util.Map;

public class TestKafkaEvent {

    public static KafkaEvent fromJson(String json) {
        KafkaEvent event = new KafkaEvent();

        KafkaEvent.KafkaEventRecord record = new KafkaEvent.KafkaEventRecord();
        record.setValue(json); // <-- String, NOT bytes

        event.setRecords(
                Map.of("user-created-topic-0", List.of(record))
        );

        return event;
    }
}
