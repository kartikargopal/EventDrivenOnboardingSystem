package com.service.profileapi.dto;

import jakarta.validation.constraints.NotBlank;

// This class represents the expected JSON body for creating a profile.
public class ProfileCreationRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    // --- Getter and Setter ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}