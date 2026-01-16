package com.server.services.api;

public record CreateServiceRequest(
        String name,
        Integer organizationId,
        String description,
        Integer durationMinutes,
        Integer price
) {
}
