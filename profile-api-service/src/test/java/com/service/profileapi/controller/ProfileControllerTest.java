package com.service.profileapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profileapi.api.CreateProfileRequest;
import com.profileapi.model.UserProfile;
import com.profileapi.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.profileapi.controller.ProfileController;
import com.profileapi.exception.GlobalExceptionHandler;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileControllerTest {

    private MockMvc mockMvc;
    private ProfileService profileService;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        profileService = Mockito.mock(ProfileService.class);
        mapper = new ObjectMapper();

        ProfileController controller = new ProfileController(profileService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void whenCreateProfile_thenReturns201() throws Exception {

        UserProfile mockProfile = new UserProfile(
                "u1", "john", "john@example.com", "John", "Doe", Instant.now()
        );

        when(profileService.createProfile(any(CreateProfileRequest.class)))
                .thenReturn(mockProfile);

        CreateProfileRequest request =
                new CreateProfileRequest("u1", "john", "john@example.com", "John", "Doe");

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("u1"));
    }

    @Test
    void whenGetProfile_withValidId_thenReturns200() throws Exception {
        UserProfile mockProfile = new UserProfile(
                "u123", "jdoe", "john@example.com", "John", "Doe", Instant.now()
        );

        when(profileService.getProfileById("u123"))
                .thenReturn(Optional.of(mockProfile));

        mockMvc.perform(get("/profiles/u123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jdoe"));
    }

    @Test
    void whenGetProfile_notFound_then404() throws Exception {
        when(profileService.getProfileById("unknown"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/profiles/unknown"))
                .andExpect(status().isNotFound());
    }
}
