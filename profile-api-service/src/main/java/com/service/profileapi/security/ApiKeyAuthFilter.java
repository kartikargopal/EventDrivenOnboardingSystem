package com.service.profileapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    // 1. Inject the API key from the environment variable (set by Terraform)
    //@Value("${PROFILE_API_KEY}")
    //private String serverApiKey;

    @Value("${security.api-key.enabled:true}")
    private boolean securityEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!securityEnabled) {
            filterChain.doFilter(request, response);
            return; // 🚀 Skip auth in test profile
        }
    }

  /*  @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 2. Get the 'X-API-Key' header from the incoming request
        String requestApiKey = request.getHeader("X-API-Key");

        // 3. Validate the key
        if (serverApiKey.equals(requestApiKey)) {
            // Key is valid, proceed with the request
            filterChain.doFilter(request, response);
        } else {
            // Key is invalid or missing, reject the request
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Error: Invalid or Missing API Key");
            return; // Stop the filter chain
        }
    }*/
}