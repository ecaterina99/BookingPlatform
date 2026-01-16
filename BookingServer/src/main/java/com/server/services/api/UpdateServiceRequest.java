package com.server.services.api;

public record UpdateServiceRequest(
        String name,
        Integer organizationId,
        String description,
        Integer durationMinutes,
        Integer price
) {
}

