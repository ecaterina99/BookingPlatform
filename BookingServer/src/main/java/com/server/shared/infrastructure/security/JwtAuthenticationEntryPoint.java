package com.server.shared.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.server.shared.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        // Let Spring set WWW-Authenticate header
        delegate.commence(request, response, authException);

        // Override body with our custom error format
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = authException.getMessage() != null
                ? authException.getMessage()
                : "Authentication required. Please provide a valid Bearer token.";

        ApiError apiError = ApiError.of(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                message
        );

        objectMapper.writeValue(response.getOutputStream(), apiError);
    }
}
