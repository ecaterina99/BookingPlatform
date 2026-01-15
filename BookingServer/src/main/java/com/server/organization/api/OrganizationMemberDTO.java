package com.server.organization.api;

import com.server.organization.domain.enums.Role;

public record OrganizationMemberDTO(
        int id,
        int organizationId,
        int userId,
        Role role
) {
}
