package com.server.shared.api;

import com.server.organization.domain.enums.GlobalRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current user information")
public record UserInfoResponse(
        @Schema(description = "User ID")
        int id,

        @Schema(description = "User email")
        String email,

        @Schema(description = "User's full name")
        String fullName,

        @Schema(description = "User's global role")
        GlobalRole globalRole
) {}
