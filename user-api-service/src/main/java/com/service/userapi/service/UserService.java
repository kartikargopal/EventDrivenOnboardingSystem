package com.service.userapi.service;

import com.service.userapi.exception.BadRequestException;
import com.service.userapi.exception.UserAlreadyExistsException;
import com.service.userapi.exception.UserNotFoundException;
import com.service.userapi.model.RoleName;
import com.service.userapi.model.User;
import com.service.userapi.model.UserCreatedEvent;
import com.service.userapi.model.UserDeletedEvent;
import com.service.userapi.model.UserUpdatedEvent;
import com.service.userapi.payload.RegisterRequest;
import com.service.userapi.payload.UpdateUserRequest;
import com.service.userapi.repository.UserRepository;
import com.service.userapi.outbox.OutboxEventRepository;
import com.service.userapi.outbox.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException; // Import for exception handling
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // Import for bulk outbox save
import java.util.List;

import java.util.Set;
import java.util.UUID;


@Service
public class UserService  {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    // Injects the value from "kafka.topic.user-created" in your .yml
    @Value("${kafka.topic.user-created}")
    private String userCreatedTopic;

    // Injects the value from "kafka.topic.user-deleted" in your .yml
    @Value("${kafka.topic.user-deleted}")
    private String userDeletedTopic;

    // Injects the value from "kafka.topic.user-updated" in your .yml
    @Value("${kafka.topic.user-updated}")
    private String userUpdatedTopic;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       OutboxEventRepository outboxEventRepository,
                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new user from a RegisterRequest DTO.
     */
    @Transactional
    public User createUser(RegisterRequest registerRequest) throws UserAlreadyExistsException {
        logger.info("Creating user with username: {}", registerRequest.getUsername());

        // Check if username or email already exists
        userRepository.findByUsername(registerRequest.getUsername()).ifPresent(u -> {
            throw new UserAlreadyExistsException("Username already exists: " + registerRequest.getUsername());
        });
        userRepository.findByEmail(registerRequest.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistsException("Email already exists: " + registerRequest.getEmail());
        });

        // --- MAPPING LOGIC ---
        // Map from the safe DTO to the database User entity
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Set.of(RoleName.ROLE_USER.name()));
        // ---------------------

        try {
            // 1. Save user to MongoDB
            User savedUser = userRepository.save(user);
            logger.info("User created successfully with id: {}", savedUser.getId());

            // 2. Create the event payload
            UserCreatedEvent eventPayload = new UserCreatedEvent(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName()
            );

            // 3. Create and save the outbox event
            OutboxEvent outboxEvent = new OutboxEvent(
                    userCreatedTopic,
                    savedUser.getId(),
                    objectMapper.writeValueAsString(eventPayload)
            );
            outboxEventRepository.save(outboxEvent);
            logger.info("Saved outbox event for user creation: {}", savedUser.getId());

            return savedUser;

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize UserCreatedEvent, rolling back transaction", e);
            throw new RuntimeException("User creation failed due to event serialization error", e);
        }
    }

    /**
     * Updates an existing user and creates a corresponding outbox event
     * in a single atomic transaction.
     *
     * @param userId The ID of the user to update.
     * @param request The DTO containing the new values.
     * @return The updated User entity.
     */
    @Transactional
    public User updateUser(String userId, UpdateUserRequest request) {
        logger.info("Attempting to update user with id: {}", userId);

        // 1. Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // 2. Check for conflicts
        // Check if new username is taken by *another* user
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
                throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
            });
            user.setUsername(request.getUsername());
            logger.info("User {} username updated.", userId);
        }

        // Check if new email is taken by *another* user
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
                throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
            });
            user.setEmail(request.getEmail());
            logger.info("User {} email updated.", userId);
        }

        // 3. Apply non-conflicting updates
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            logger.info("User {} firstName updated.", userId);
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
            logger.info("User {} lastName updated.", userId);
        }

        // Note: Password/Role changes should be separate, more secure methods.

        try {
            // 4. Save the updated user
            User updatedUser = userRepository.save(user);
            logger.info("User updated successfully with id: {}", updatedUser.getId());

            // 5. Create the event payload
            UserUpdatedEvent eventPayload = new UserUpdatedEvent(
                    updatedUser.getId(),
                    updatedUser.getUsername(),
                    updatedUser.getEmail(),
                    updatedUser.getFirstName(),
                    updatedUser.getLastName()
            );

            // 6. Create and save the outbox event
            OutboxEvent outboxEvent = new OutboxEvent(
                    userUpdatedTopic,
                    updatedUser.getId(),
                    objectMapper.writeValueAsString(eventPayload)
            );
            outboxEventRepository.save(outboxEvent);
            logger.info("Saved outbox event for user update: {}", updatedUser.getId());

            return updatedUser;

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize UserUpdatedEvent, rolling back transaction", e);
            throw new RuntimeException("User update failed due to event serialization error", e);
        }
    }

    /**
     * Creates a new ADMIN user from a RegisterRequest DTO.
     */
    @Transactional
    public User createAdminUser(RegisterRequest registerRequest) {
        logger.info("Creating ADMIN user with username: {}", registerRequest.getUsername());
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + registerRequest.getUsername());
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + registerRequest.getEmail());
        }

        // --- MAPPING LOGIC ---
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // Assign both USER and ADMIN roles
        user.setRoles(Set.of(RoleName.ROLE_USER.name(), RoleName.ROLE_ADMIN.name()));
        // ---------------------

        try {
            User savedUser = userRepository.save(user);
            logger.info("Admin User created successfully with id: {}", savedUser.getId());

            UserCreatedEvent event = new UserCreatedEvent(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName()
            );

            OutboxEvent outboxEvent = new OutboxEvent(
                    userCreatedTopic,
                    savedUser.getId(),
                    objectMapper.writeValueAsString(event)
            );
            outboxEventRepository.save(outboxEvent);
            logger.info("Saved outbox event for admin user creation: {}", savedUser.getId());

            return savedUser;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize UserCreatedEvent for admin", e);
            throw new RuntimeException("Admin user creation failed", e);
        }
    }

    /**
     * Deletes a user and creates a corresponding outbox event in a single atomic transaction.
     */
    @Transactional
    public void deleteUserById(String userId) {
        logger.info("Attempting to delete user with id: {}", userId);

        // Find user first
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        try {
            // 1. Delete the user from the repository
            userRepository.delete(user);
            logger.info("Successfully deleted user with id: {}", userId);

            // 2. Create the event payload
            UserDeletedEvent event = new UserDeletedEvent(userId);

            // 3. Create and save the outbox event
            OutboxEvent outboxEvent = new OutboxEvent(
                    userDeletedTopic,
                    userId,
                    objectMapper.writeValueAsString(event)
            );
            outboxEventRepository.save(outboxEvent);
            logger.info("Saved outbox event for user deletion: {}", userId);

        } catch (JsonProcessingException e) {
            // If serialization fails, the transaction will roll back.
            logger.error("Failed to serialize UserDeletedEvent, rolling back transaction", e);
            throw new RuntimeException("User deletion failed due to event serialization error", e);
        }
    }

    /**
     * Deletes a user by username. This method is transactional and
     * will call the main deleteUserById logic, inheriting its outbox behavior.
     */
    @Transactional
    public void deleteUserByUsername(String username) {
        logger.info("Attempting to delete user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        // Call the main delete logic (which is already transactional and creates an outbox event)
        deleteUserById(user.getId());
    }

    /**
     * Deletes a user by email. This method is transactional and
     * will call the main deleteUserById logic, inheriting its outbox behavior.
     */
    @Transactional
    public void deleteUserByEmail(String email) {
        logger.info("Attempting to delete user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Call the main delete logic (which is already transactional and creates an outbox event)
        deleteUserById(user.getId());
    }

    /**
     * Deletes all users and creates corresponding outbox events in a single atomic transaction.
     */
    @Transactional
    public long deleteAllUsers() {
        logger.warn("INITIATING BULK DELETION OF ALL USERS.");

        // 1. Get a list of all users *before* deleting
        List<User> allUsers = userRepository.findAll();
        long userCount = allUsers.size();

        if (userCount == 0) {
            logger.info("No users found to delete.");
            return 0;
        }

        try {
            // 2. Delete all users from the database
            userRepository.deleteAll();

            // 3. Create a list of outbox events for every user that was deleted
            List<OutboxEvent> deleteEvents = new ArrayList<>();
            for (User user : allUsers) {
                UserDeletedEvent event = new UserDeletedEvent(user.getId());
                OutboxEvent outboxEvent = new OutboxEvent(
                        userDeletedTopic,
                        user.getId(),
                        objectMapper.writeValueAsString(event)
                );
                deleteEvents.add(outboxEvent);
            }

            // 4. Save all outbox events in a single batch operation
            outboxEventRepository.saveAll(deleteEvents);

            logger.info("Successfully deleted {} users and published {} delete events to outbox.", userCount, userCount);
            return userCount;

        } catch (JsonProcessingException e) {
            // If serialization fails for any event, the entire transaction rolls back.
            logger.error("Failed to serialize one or more UserDeletedEvents, rolling back bulk delete", e);
            throw new RuntimeException("Bulk user deletion failed due to event serialization error", e);
        }
    }

    // =================================================================
    // == SPRING SECURITY AUTHENTICATION METHODS (Required) ==
    // =================================================================

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }


    public User findUser(String username, String email) {
        if (username != null) {
            logger.info("Fetching user by username: {}", username);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        }
        if (email != null) {
            logger.info("Fetching user by email: {}", email);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        }

        // If neither parameter is provided
        throw new BadRequestException("A search parameter (username or email) is required.");
    }
}