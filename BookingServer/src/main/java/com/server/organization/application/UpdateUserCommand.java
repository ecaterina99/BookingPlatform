package com.server.organization.application;

public record UpdateUserCommand(
        String email,
        String fullName,
        int id
) {
}