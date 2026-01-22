package com.server.shared.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token")
public record AuthResponse(
        @Schema(description = "JWT access token")
        String token,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "Token expiration time in milliseconds")
        long expiresIn
) {
    public AuthResponse(String token, long expiresIn) {
        this(token, "Bearer", expiresIn);
    }
}
