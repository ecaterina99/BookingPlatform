package com.server.service.application;

import com.server.service.domain.ServiceCategory;

public record UpdateServiceCommand(
        int id,
        String name,
        Integer organizationId,
        String description,
        Integer durationMinutes,
        Integer price,
        ServiceCategory category
) {
}

