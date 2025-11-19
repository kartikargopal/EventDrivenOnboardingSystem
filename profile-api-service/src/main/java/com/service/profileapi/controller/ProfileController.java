package com.service.profileapi.controller;

import com.service.profileapi.api.ResourceNotFoundException;
import com.service.profileapi.model.UserProfile;
import com.service.profileapi.api.CreateProfileRequest;
import com.service.profileapi.api.UserProfileResponse;
import com.service.profileapi.service.ProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        log.info("Received createProfile request for userId={}", request.getUserId());

        UserProfile profile = service.createProfile(request);

        UserProfileResponse response = new UserProfileResponse(
                profile.getUserId(),
                profile.getUsername(),
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable String userId) {
        log.info("Received getProfile request for userId={}", userId);

        return service.getProfileById(userId)
                .map(profile -> ResponseEntity.ok(
                        new UserProfileResponse(
                                profile.getUserId(),
                                profile.getUsername(),
                                profile.getEmail(),
                                profile.getFirstName(),
                                profile.getLastName(),
                                profile.getCreatedAt()
                        )
                ))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profile not found for userId=" + userId)
                );
    }
}
