package com.service.notification.model;

public class ProfileRequest {

    private String userId;
    private String email;

    public ProfileRequest() {
    }

    public ProfileRequest(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    // -------- Getters and Setters ----------

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
