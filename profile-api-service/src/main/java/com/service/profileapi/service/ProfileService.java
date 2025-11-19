
package com.service.profileapi.service;

import com.service.profileapi.api.CreateProfileRequest;
import com.service.profileapi.model.UserProfile;
import com.service.profileapi.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserProfileRepository repository;

    public ProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public UserProfile createProfile(CreateProfileRequest request) {
        log.info("Creating profile for userId={} username={}", request.getUserId(), request.getUsername());

        UserProfile profile = new UserProfile();
        profile.setUserId(request.getUserId());
        profile.setUsername(request.getUsername());
        profile.setEmail(request.getEmail());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setCreatedAt(Instant.now());

        UserProfile saved = repository.save(profile);
        log.info("Profile created for userId={}", saved.getUserId());
        return saved;
    }

    public Optional<UserProfile> getProfileById(String userId) {
        log.debug("Fetching profile for userId={}", userId);
        // Cleaned up redundant Optional wrapping
        return repository.findByUserId(userId);
    }
}
