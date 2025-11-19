package com.service.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.notification.client.ProfileApiClient;
import com.service.notification.model.UserCreatedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class NotificationHandlerTest {

    @Test
    void testHandlerLogsAndCallsProfileApi() throws Exception {

        // Arrange -------------------------------------------------------------

        // Mock logger
        LambdaLogger mockLogger = mock(LambdaLogger.class);

        // Mock context
        Context mockContext = mock(Context.class);
        when(mockContext.getLogger()).thenReturn(mockLogger);

        // Mock ProfileApiClient
        ProfileApiClient mockClient = mock(ProfileApiClient.class);

        // Handler with injected mocks
        NotificationHandler handler =
                new NotificationHandler(new ObjectMapper(), mockClient);

        // Create the event payload
        UserCreatedEvent user = new UserCreatedEvent(
                "u123",     // must match your assertions
                "John",
                "Doe",
                "jdoe",
                "john@example.com"
        );

        String payloadJson = new ObjectMapper().writeValueAsString(user);

        // Prepare KafkaEvent
        KafkaEvent event = new KafkaEvent();

        KafkaEvent.KafkaEventRecord record = new KafkaEvent.KafkaEventRecord();
        record.setValue(payloadJson); // <-- String is correct for your AWS libs

        Map<String, List<KafkaEvent.KafkaEventRecord>> map = new HashMap<>();
        map.put("user-created-topic-0", List.of(record));

        event.setRecords(map);

        // Act ----------------------------------------------------------------
        handler.handleRequest(event, mockContext);

        // Assert --------------------------------------------------------------

        // Logger was used at least once
        verify(mockLogger, atLeastOnce()).log(anyString());

        // ProfileApiClient.createProfile() was called
        ArgumentCaptor<UserCreatedEvent> captor =
                ArgumentCaptor.forClass(UserCreatedEvent.class);

        verify(mockClient, times(1)).createProfile(captor.capture());

        UserCreatedEvent captured = captor.getValue();
        assert captured.getUserId().equals("u123");
        assert captured.getUsername().equals("jdoe");
    }
}
