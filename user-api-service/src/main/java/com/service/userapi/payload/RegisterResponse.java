package com.service.userapi.payload;

import com.service.userapi.model.User;

/**
 * A new DTO to structure the registration response.
 * Jackson will serialize this into the JSON you want.
 */
public class RegisterResponse {
    public boolean success;
    public String message;
    public User user; // The full user object (password is @JsonIgnored)

    public RegisterResponse(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }
}