package com.service.userapi.payload;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Username or email is required")
    public String username;

    @NotBlank(message = "Password is required")
    public String password;
}