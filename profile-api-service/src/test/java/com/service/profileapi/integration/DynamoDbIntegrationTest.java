package com.service.profileapi.integration;

import com.service.profileapi.model.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Testcontainers
public class DynamoDbIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.2.0"))
            .withServices(LocalStackContainer.Service.DYNAMODB);

    @Test
    void testDynamoDbPutAndGet() {
        DynamoDbClient client = DynamoDbClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .region(Region.of(localstack.getRegion()))
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();

        DynamoDbTable<UserProfile> table = enhancedClient.table("UserProfiles", TableSchema.fromBean(UserProfile.class));
        try { table.createTable(); } catch (Exception ignored) {}

        UserProfile profile = new UserProfile("user123", "john", "john@example.com", "John", "Doe", Instant.now());
        table.putItem(profile);

        UserProfile dbProfile = table.getItem(r -> r.key(k -> k.partitionValue("user123")));

        assertNotNull(dbProfile);
        assertEquals("john", dbProfile.getUsername());
    }
}
