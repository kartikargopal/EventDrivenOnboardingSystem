package com.service.userapi.payload;

public class AuthResponse {
    public String token;

    public AuthResponse(String token) {
        this.token = token;
    }
}