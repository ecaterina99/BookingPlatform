package com.server.service.api;

import com.server.service.domain.ServiceCategory;

public record UpdateServiceRequest(
        String name,
        Integer organizationId,
        String description,
        Integer durationMinutes,
        Integer price,
        ServiceCategory category
) {
}

