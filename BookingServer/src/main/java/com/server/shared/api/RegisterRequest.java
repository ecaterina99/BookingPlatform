package com.server.shared.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request")
public record RegisterRequest(
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password (minimum 6 characters)", example = "password123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @Schema(description = "User's full name", example = "John Doe")
        @NotBlank(message = "Full name is required")
        String fullName
) {}
