package com.service.profileapi.service;

import com.profileapi.api.CreateProfileRequest;
import com.profileapi.model.UserProfile;
import com.profileapi.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.profileapi.service.ProfileService;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProfileServiceTest {

    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private ProfileService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProfile() {
        CreateProfileRequest req = new CreateProfileRequest();
        req.setUserId("user123");
        req.setUsername("john");
        req.setEmail("john@example.com");

        UserProfile saved = new UserProfile("user123", "john", "john@example.com", null, null, Instant.now());

        when(repository.save(any(UserProfile.class))).thenReturn(saved);

        UserProfile result = service.createProfile(req);

        assertEquals("user123", result.getUserId());
        verify(repository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testGetProfileById() {
        UserProfile profile = new UserProfile("user123", "john", "john@example.com", null, null, Instant.now());
        when(repository.findByUserId("user123")).thenReturn(Optional.of(profile));

        Optional<UserProfile> result = service.getProfileById("user123");

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }
}