package com.service.userapi.controller;

import com.service.userapi.model.UserCreationRequest;
import com.service.userapi.model.User; // You can keep this if you return the full User object
import com.service.userapi.payload.AuthResponse;
import com.service.userapi.payload.LoginRequest;
import com.service.userapi.payload.RegisterRequest;
import com.service.userapi.payload.RegisterResponse;
import com.service.userapi.security.JwtTokenProvider;
import com.service.userapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${admin.registration.key}")
    private String adminRegistrationKey;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username,
                        loginRequest.password
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponse(jwt));

      //  return ResponseEntity.ok(new AuthResponse("Logged in successfully"));
    }

    /**
     * Registers a new user.
     * The UserService.createUser method will automatically check the
     * registrationKey and assign roles (USER or ADMIN) accordingly.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User user;
        if (adminRegistrationKey.equals(registerRequest.getRegistrationKey())) {
            // This is an admin registration
            user = userService.createAdminUser(registerRequest); // <-- Pass the DTO
        } else {
            // This is a normal user registration
            user = userService.createUser(registerRequest); // <-- Pass the DTO
        }

        RegisterResponse response = new RegisterResponse(true, "User created successfully", user);

        // Return the response object with a 201 CREATED status
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}