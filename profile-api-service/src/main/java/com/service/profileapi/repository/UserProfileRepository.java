package com.service.profileapi.repository;

import com.service.profileapi.model.UserProfile;

import java.util.Optional;

public interface UserProfileRepository {

    UserProfile save(UserProfile profile);

    Optional<UserProfile> findByUserId(String userId);
}
