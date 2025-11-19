package com.service.notification.local;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.service.notification.NotificationHandler;
import com.service.notification.model.UserCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;

public class LocalNotificationRunner {

    public static void main(String[] args) throws Exception {
        NotificationHandler handler = new NotificationHandler();

        KafkaEvent event = new KafkaEvent();
        KafkaEvent.KafkaEventRecord record = new KafkaEvent.KafkaEventRecord();

        UserCreatedEvent user = new UserCreatedEvent(
                "u-local-1", "Local", "User", "localuser", "local@example.com");

        String payloadJson = new ObjectMapper().writeValueAsString(user);

// KafkaEventRecord wants STRING, not bytes
        record.setValue(payloadJson);

        event.setRecords(Collections.singletonMap(
                "user-created-topic-0",
                Collections.singletonList(record)
        ));

        handler.handleRequest(event, new LocalContext());
    }

    static class LocalContext implements Context {

        private final LambdaLogger logger = new LambdaLogger() {
            @Override
            public void log(String message) {
                System.out.println("[LOCAL LAMBDA] " + message);
            }

            @Override
            public void log(byte[] message) {
                System.out.println("[LOCAL LAMBDA] " + new String(message));
            }
        };

        @Override
        public String getAwsRequestId() { return "local-request-id"; }

        @Override
        public String getLogGroupName() { return "local-log-group"; }

        @Override
        public String getLogStreamName() { return "local-log-stream"; }

        @Override
        public String getFunctionName() { return "local-notification-handler"; }

        @Override
        public String getFunctionVersion() { return "1.0"; }

        @Override
        public String getInvokedFunctionArn() { return "arn:aws:lambda:local"; }

        @Override
        public com.amazonaws.services.lambda.runtime.CognitoIdentity getIdentity() {
            return null;
        }

        @Override
        public ClientContext getClientContext() { return null; }

        @Override
        public int getRemainingTimeInMillis() { return 300000; }

        @Override
        public int getMemoryLimitInMB() { return 512; }

        @Override
        public LambdaLogger getLogger() { return logger; }
    }
}
