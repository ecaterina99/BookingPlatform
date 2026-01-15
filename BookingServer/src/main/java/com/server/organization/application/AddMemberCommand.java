package com.server.organization.application;

import com.server.organization.domain.enums.Role;

public record AddMemberCommand(
        Long organizationId,
        Long userId,
        Role role
) {}
