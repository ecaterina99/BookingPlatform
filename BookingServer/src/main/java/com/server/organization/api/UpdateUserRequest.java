package com.server.organization.api;

public record UpdateUserRequest(
        String email,
        String fullName
) {
}