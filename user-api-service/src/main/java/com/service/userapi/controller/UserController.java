package com.service.userapi.controller;
import com.service.userapi.model.User;
import com.service.userapi.model.UserCreationRequest;
import com.service.userapi.payload.UpdateUserRequest;
import com.service.userapi.security.UserPrincipal; // <-- IMPORT UserPrincipal
import com.service.userapi.service.UserService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable String id, @AuthenticationPrincipal UserPrincipal principal) { // <-- USE UserPrincipal
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // <-- SECURED this endpoint
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Search by Username or Email
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')") // <-- SECURED this endpoint
    public ResponseEntity<?> findUser(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        // The service layer now handles all logic, including the error
        // if no parameter is provided.
        User user = userService.findUser(username, email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable String id,
                                           @Valid @RequestBody UpdateUserRequest updateUserRequest, // <-- Add @Valid
                                           @AuthenticationPrincipal UserPrincipal principal) {

        logger.info("User {} is attempting to update user {}", principal.getId(), id);
        User updatedUser = userService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Only the user themselves or an admin can delete the user
    @DeleteMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserById(@PathVariable String id, @AuthenticationPrincipal UserPrincipal principal) { // <-- USE UserPrincipal
        userService.deleteUserById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/by-username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/by-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserByEmail(@RequestParam String email) {
        userService.deleteUserByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/delete-all")
    @PreAuthorize("hasRole('ADMIN')") // <-- SECURED this endpoint
    public ResponseEntity<?> deleteAllUsers() {
        long deletedCount = userService.deleteAllUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully deleted all users.");
        response.put("usersDeleted", deletedCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "user-api-service");
        return ResponseEntity.ok(response);
    }
}