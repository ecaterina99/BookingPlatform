package com.server.shared.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public record AuthRequest(
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password", example = "password123")
        @NotBlank(message = "Password is required")
        String password
) {}
