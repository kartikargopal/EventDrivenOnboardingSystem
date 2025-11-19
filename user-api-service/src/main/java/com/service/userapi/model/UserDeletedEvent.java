package com.service.userapi.model;

import java.time.LocalDateTime;

public class UserDeletedEvent {
    private String userId;
    private LocalDateTime timestamp;

    public UserDeletedEvent() {}

    public UserDeletedEvent(String userId) {
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}