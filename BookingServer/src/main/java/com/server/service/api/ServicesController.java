package com.server.service.api;

import com.server.service.application.AddServiceCommand;
import com.server.service.application.ServiceOfferingService;
import com.server.service.application.UpdateServiceCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServicesController {

    private final ServiceOfferingService serviceOfferingService;

    public ServicesController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    @GetMapping
    @Operation(summary = "Retrieve all services (public)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all services",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ServiceDTO.class))))
    public List<ServiceDTO> getAllServices() {
        return serviceOfferingService.getAllServices();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve service by ID (public)")
    @ApiResponse(responseCode = "200", description = "Service is found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ServiceDTO.class)))
    public ServiceDTO getServiceById(
            @Parameter(description = "Service ID")
            @PathVariable int id) {
        return serviceOfferingService.getServiceById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new service (ORG_ADMIN only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "New service added successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ServiceDTO.class)))
    @PreAuthorize("@orgAccessEvaluator.isOrganizationAdmin(#request.organizationId()) or hasRole('GLOBAL_ADMIN')")
    public int addService(@RequestBody CreateServiceRequest request) {
        return serviceOfferingService.addService(
                new AddServiceCommand(
                        request.name(),
                        request.organizationId(),
                        request.description(),
                        request.durationMinutes(),
                        request.price()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete service (ORG_ADMIN only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Service deleted successfully")
    @PreAuthorize("@orgAccessEvaluator.canManageService(#id) or hasRole('GLOBAL_ADMIN')")
    public void deleteService(@PathVariable int id) {
        serviceOfferingService.deleteServiceById(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update service (ORG_ADMIN only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Service updated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ServiceDTO.class)))
    @PreAuthorize("@orgAccessEvaluator.canManageService(#id) or hasRole('GLOBAL_ADMIN')")
    public void updateService(@PathVariable int id, @Valid @RequestBody UpdateServiceRequest request) {
        serviceOfferingService.updateService(
                new UpdateServiceCommand(id, request.name(), request.organizationId(), request.description(), request.durationMinutes(), request.price())
        );
    }
}
