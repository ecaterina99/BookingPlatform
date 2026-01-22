package com.server.organization.api;

import com.server.organization.application.*;
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
@RequestMapping("/api/organizations")
@Tag(name = "Organizations", description = "Operations related to organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    @Operation(summary = "Retrieve all organizations (public)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all organizations",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = OrganizationDTO.class))))
    public List<OrganizationDTO> getAllOrganizations() {
        return organizationService.getAllOrganizations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve organization by id (public)")
    @ApiResponse(responseCode = "200", description = "Organization found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrganizationDTO.class)))
    public OrganizationDTO getOrganizationById(
            @Parameter(description = "ID of organization to retrieve", example = "1")
            @PathVariable int id) {
        return organizationService.getOrganizationById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new organization (GLOBAL_ADMIN only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Organization created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrganizationDTO.class)))
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public int registerOrganization(@Valid @RequestBody CreateOrganizationCommand command) {
        return organizationService.createOrganization(command);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organization (GLOBAL_ADMIN only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Organization deleted successfully")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public void deleteOrganization(@PathVariable int id) {
        organizationService.deleteOrganizationById(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update organization (ORG_ADMIN only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Organization updated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrganizationDTO.class)))
    @PreAuthorize("@orgAccessEvaluator.isOrganizationAdmin(#id) or hasRole('GLOBAL_ADMIN')")
    public void updateOrganization(@PathVariable int id, @Valid @RequestBody UpdateOrganizationRequest request) {
        organizationService.updateOrganization(
                new UpdateOrganizationCommand(id, request.name(), request.city(), request.address(), request.phone(), request.email())
        );
    }
}
