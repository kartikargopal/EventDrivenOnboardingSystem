package com.service.notification.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.notification.model.UserCreatedEvent;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

public class ProfileApiClient {

    private static final Logger log = LoggerFactory.getLogger(ProfileApiClient.class);

    private static final String DEFAULT_PATH = "/profiles";

    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ProfileApiClient() {
        this(resolveBaseUrl(),
                new ObjectMapper(),
                new ApacheHttpClient(Duration.ofSeconds(5)));
    }

    private static String resolveBaseUrl() {
        String explicitUrl = System.getenv("PROFILE_API_URL");   // from docker-compose
        String baseUrl = System.getenv("PROFILE_API_BASE_URL");  // optional older name

        String chosen = explicitUrl != null && !explicitUrl.isBlank()
                ? explicitUrl
                : baseUrl;

        if (chosen == null || chosen.isBlank()) {
            throw new IllegalArgumentException("PROFILE_API_URL or PROFILE_API_BASE_URL must be configured");
        }

        return chosen;
    }

    public ProfileApiClient(String baseUrl, ObjectMapper objectMapper, HttpClient httpClient) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Profile API base URL must be configured");
        }
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public void createProfile(UserCreatedEvent event) {
        try {
            String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            if (!url.endsWith("/profiles")) {
                url = url + DEFAULT_PATH;
            }

            String body = objectMapper.writeValueAsString(event);
            log.info("Calling profile-api-service at {} for userId={}", url, event.getUserId());
            log.debug("Request body: {}", body);

            httpClient.post(url, body);

            log.info("Successfully called profile-api for userId={}", event.getUserId());
        } catch (IOException e) {
            log.error("Failed to call profile-api-service for userId={}", event.getUserId(), e);
            throw new RuntimeException("Failed to call profile-api-service", e);
        }
    }

    public interface HttpClient {
        void post(String url, String jsonBody) throws IOException;
    }

    public static class ApacheHttpClient implements HttpClient {

        private final Duration timeout;

        public ApacheHttpClient(Duration timeout) {
            this.timeout = timeout;
        }

        @Override
        public void post(String url, String jsonBody) throws IOException {
            Request.post(url)
                    .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                    .connectTimeout(Timeout.ofMilliseconds(5000))
                    .responseTimeout(Timeout.ofMilliseconds(5000))
                    .execute()
                    .returnResponse();
        }
    }
}
