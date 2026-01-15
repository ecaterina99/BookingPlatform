package com.server.organization.api;

import com.server.organization.application.AddMemberCommand;
import com.server.organization.application.OrgMembersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Organization members", description = "Operations related to organization members")
public class OrgMembersController {

    private final OrgMembersService orgMembersService;

    public OrgMembersController(OrgMembersService orgMembersService) {
        this.orgMembersService = orgMembersService;
    }

    @PostMapping("/add/{organizationId}")
    @Operation(summary = "Add member to organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added to the organization successfully"
            )})
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

    @Operation(summary = "Delete a member from the organization")
    @ApiResponse(responseCode = "204", description = "Member deleted from the organization successfully")
    @DeleteMapping("/{organizationId}/{userId}")
    public void deleteMember(@PathVariable int organizationId, @PathVariable int userId) {
        orgMembersService.deleteMember(organizationId, userId);
    }

    @GetMapping("/{organizationId}")
    @Operation(summary = "Retrieve all organization members by organization id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all organization's members",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = OrganizationDTO.class))))
    public List<OrganizationMemberDTO> getAllOrganizationMembers(
            @Parameter(description = "ID of organization", example = "1")
            @PathVariable int organizationId) {
        return orgMembersService.getMembers(organizationId);
    }

}
