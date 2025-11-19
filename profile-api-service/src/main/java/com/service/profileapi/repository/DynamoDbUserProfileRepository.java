package com.service.profileapi.repository;

import com.service.profileapi.model.UserProfile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.util.Optional;

@Repository
public class DynamoDbUserProfileRepository implements UserProfileRepository {

    private final DynamoDbTable<UserProfile> table;

    public DynamoDbUserProfileRepository(DynamoDbTable<UserProfile> table) {
        this.table = table;
    }

    @Override
    public UserProfile save(UserProfile profile) {
        table.putItem(profile);
        return profile;
    }

    @Override
    public Optional<UserProfile> findByUserId(String userId) {
        UserProfile result = table.getItem(r -> r.key(k -> k.partitionValue(userId)));
        return Optional.ofNullable(result);
    }
}
