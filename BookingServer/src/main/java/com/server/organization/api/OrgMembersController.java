package com.server.organization.api;

import com.server.organization.application.AddMemberCommand;
import com.server.organization.application.OrgMembersService;
import com.server.shared.infrastructure.security.SecurityCurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Organization members", description = "Operations related to organization members")
@SecurityRequirement(name = "bearerAuth")
public class OrgMembersController {

    private final OrgMembersService orgMembersService;
    private final SecurityCurrentUserProvider currentUserProvider;


    public OrgMembersController(OrgMembersService orgMembersService, SecurityCurrentUserProvider currentUserProvider) {
        this.orgMembersService = orgMembersService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/add/{organizationId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add member to organization (ORG_ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added to the organization successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - not an organization admin")
    })
    @PreAuthorize("@orgAccessEvaluator.isOrganizationAdmin(#organizationId) or hasRole('GLOBAL_ADMIN')")
    public int addMember(
            @PathVariable int organizationId,
            @RequestBody AddMemberRequest request
    ) {
        AddMemberCommand command = new AddMemberCommand(
                organizationId,
                request.userId(),
                request.role()
        );
        return orgMembersService.addMember(command);
    }

    @DeleteMapping("/{organizationId}/{userId}")
    @Operation(summary = "Delete a member from the organization (ORG_ADMIN only)")
    @ApiResponse(responseCode = "204", description = "Member deleted from the organization successfully")
    @PreAuthorize("@orgAccessEvaluator.isOrganizationAdmin(#organizationId) or hasRole('GLOBAL_ADMIN')")
    public void deleteMember(@PathVariable int organizationId, @PathVariable int userId) {
        orgMembersService.deleteMember(organizationId, userId);
    }

    @GetMapping("/{organizationId}")
    @Operation(summary = "Retrieve all organization members by organization id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all organization's members",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = OrganizationMemberDTO.class))))
    @PreAuthorize("@orgAccessEvaluator.isOrganizationMember(#organizationId) or hasRole('GLOBAL_ADMIN')")
    public List<OrganizationMemberDTO> getAllOrganizationMembers(
            @Parameter(description = "ID of organization", example = "1")
            @PathVariable int organizationId) {
        return orgMembersService.getMembers(organizationId);
    }

    @GetMapping("/{organizationId}/specialists")
    @Operation(summary = "Get all specialists of an organization")
    public List<SpecialistDTO> getSpecialists(@PathVariable int organizationId) {
        return orgMembersService.getSpecialists(organizationId);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's memberships across all organizations")
    public List<OrganizationMemberDTO> getMyMemberships() {
        return orgMembersService.getMyMemberships(currentUserProvider.getUserId());
    }

}
