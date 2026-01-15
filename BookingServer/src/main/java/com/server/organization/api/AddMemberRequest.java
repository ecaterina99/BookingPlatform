package com.server.organization.api;

import com.server.organization.domain.enums.Role;

public record AddMemberRequest(
        int userId,
        Role role
) {}