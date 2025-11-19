package com.service.userapi.model;

import java.time.Instant;

public class UserUpdatedEvent {

    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Instant timestamp;

    public UserUpdatedEvent(String userId, String username, String email, String firstName, String lastName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.timestamp = Instant.now();
    }

    // Getters

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

    public Instant getTimestamp() {
        return timestamp;
    }
}