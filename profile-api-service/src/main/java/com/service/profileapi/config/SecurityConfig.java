package com.service.profileapi.config;

import com.service.profileapi.security.ApiKeyAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;

    public SecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authz -> authz
                        // allow actuator endpoints (IMPORTANT)
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/profiles/**").permitAll()

                        // everything else is allowed (your current config)
                        .anyRequest().permitAll()
                )

                // Exclude API key filter from actuator requests
                .addFilterBefore((req, res, chain) -> {
                    if (req instanceof HttpServletRequest request) {
                        String path = request.getRequestURI();

                        if (path.startsWith("/actuator")) {
                            chain.doFilter(req, res);
                            return;
                        } apiKeyAuthFilter.doFilter(request, res, chain);
                    } else {
                        chain.doFilter(req, res);
                    }
                }, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
