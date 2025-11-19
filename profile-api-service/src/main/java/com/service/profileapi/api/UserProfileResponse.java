package com.service.profileapi.api;

import java.time.Instant;

public class UserProfileResponse {

    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Instant createdAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(String userId, String username, String email,
                               String firstName, String lastName, Instant createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
