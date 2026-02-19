package com.server.organization.api;

import com.server.organization.application.CreateUserCommand;
import com.server.organization.application.UpdateUserCommand;
import com.server.organization.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations related to users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Retrieve all users (GLOBAL_ADMIN only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all users",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve user by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class)))
    @PreAuthorize("@orgAccessEvaluator.isResourceOwner(#id) or hasRole('GLOBAL_ADMIN')")
    public UserDTO getUserById(
            @Parameter(description = "ID of user to retrieve", example = "1")
            @PathVariable int id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user (public)")
    @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class)))
    public int register(@Valid @RequestBody CreateUserCommand command) {
        return userService.createUser(command);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @PreAuthorize("@orgAccessEvaluator.isResourceOwner(#id) or hasRole('GLOBAL_ADMIN')")
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class)))
    @PreAuthorize("@orgAccessEvaluator.isResourceOwner(#id) or hasRole('GLOBAL_ADMIN')")
    public void updateUser(@PathVariable int id, @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(
                new UpdateUserCommand(request.email(), request.fullName(), id)
        );
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change user global role (GLOBAL_ADMIN only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Role updated successfully")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public void changeUserRole(@PathVariable int id, @RequestBody ChangeUserRoleRequest request) {
        userService.changeUserRole(id, request.globalRole());
    }
}
