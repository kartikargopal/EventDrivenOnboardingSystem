package com.service.profileapi.e2e;

import com.service.profileapi.ProfileApiApplication;
import com.service.profileapi.model.UserProfile;
import com.service.profileapi.repository.UserProfileRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(
        classes = ProfileApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2EProfileTest {

    // ---------- DynamoDB Local (Testcontainers) ----------
    @Container
    static GenericContainer<?> dynamodb =
            new GenericContainer<>(DockerImageName.parse("amazon/dynamodb-local:latest"))
                    .withExposedPorts(8000);

    @LocalServerPort
    private int port;   // <-- Auto-resolves the correct running port

    @Autowired
    private UserProfileRepository repository;

    private static HttpClient http;

    @BeforeAll
    static void init() {
        http = HttpClient.newHttpClient();
    }

    @Test
    @Order(1)
    void fullEndToEnd() throws Exception {

        // ----- 1️⃣ Inject test profile into DynamoDB -----
        UserProfile profile = new UserProfile(
                "tc-1",
                "tester",
                "tester@example.com",
                "Test",
                "User",
                Instant.now()
        );

        repository.save(profile);

        // ----- 2️⃣ Make HTTP call to Profile API -----
        String url = "http://localhost:" + port + "/profiles/tc-1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                http.send(request, HttpResponse.BodyHandlers.ofString());

        // ----- 3️⃣ Assert Response -----
        assertEquals(200, response.statusCode(), "Profile API should return HTTP 200");
    }
}
