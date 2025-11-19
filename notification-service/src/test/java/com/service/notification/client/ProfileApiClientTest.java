package com.service.notification.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.notification.model.UserCreatedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileApiClientTest {

    @Test
    void createProfile_callsHttpClientWithCorrectUrlAndBody() throws Exception {
        // Arrange
        String baseUrl = "http://dummy-host"; // no dependency on real port
        ObjectMapper mapper = new ObjectMapper();

        class FakeHttpClient implements ProfileApiClient.HttpClient {
            String capturedUrl;
            String capturedBody;

            @Override
            public void post(String url, String jsonBody) {
                this.capturedUrl = url;
                this.capturedBody = jsonBody;
            }
        }

        FakeHttpClient fakeHttpClient = new FakeHttpClient();
        ProfileApiClient client = new ProfileApiClient(baseUrl, mapper, fakeHttpClient);

        UserCreatedEvent event = new UserCreatedEvent(
                "u1", "Jane", "Doe", "jane", "jane@example.com"
        );

        // Act
        client.createProfile(event);

        // Assert
        assertEquals(baseUrl + "/profiles", fakeHttpClient.capturedUrl);

        assertNotNull(fakeHttpClient.capturedBody);
        assertTrue(fakeHttpClient.capturedBody.contains("\"userId\":\"u1\""));
        assertTrue(fakeHttpClient.capturedBody.contains("\"username\":\"jane\""));
        assertTrue(fakeHttpClient.capturedBody.contains("\"email\":\"jane@example.com\""));
    }

    @Test
    void constructor_throwsWhenBaseUrlMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProfileApiClient(null, new ObjectMapper(), (u, b) -> {}));
    }
}
