package com.server.services.api;

public record ServiceDTO(
        int id,
        String name,
        int organizationId,
        String description,
        int durationMinutes,
        int price
) {
}
