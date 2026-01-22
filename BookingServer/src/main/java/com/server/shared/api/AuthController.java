package com.server.shared.api;

import com.server.organization.application.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authenticationService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "Registration successful",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "409", description = "User already exists",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }

    @GetMapping("/get/current")
    @Operation(summary = "Get current authenticated user info",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User info retrieved",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserInfoResponse.class)))
    @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
    public UserInfoResponse getCurrentUser() {
        return authenticationService.getCurrentUser();
    }
}
