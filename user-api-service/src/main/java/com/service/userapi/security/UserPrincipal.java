package com.service.userapi.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.service.userapi.model.User; // <-- Import your User model
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final String id;
    private final String username;
    private final String email;

    @JsonIgnore // Don't send the password back in any API responses
    private final String password;

    // This holds the user's "roles" (e.g., "ROLE_USER", "ROLE_ADMIN")
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String username, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username; // <-- ADDED
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Helper method to create a UserPrincipal object from your User entity
     */
    public static UserPrincipal create(User user) {
        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toSet());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(), // <-- PASS USERNAME
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    // --- Getter methods required by the UserDetails interface ---

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        // Use the *actual* username
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // --- Other methods required by UserDetails ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or logic from your User model
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or logic from your User model
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // Or logic from your User model
    }

    // --- Equals and HashCode for comparison ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}