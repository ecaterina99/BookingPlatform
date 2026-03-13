package com.server.service.api;

import com.server.service.domain.ServiceCategory;

public record CreateServiceRequest(
        String name,
        Integer organizationId,
        String description,
        Integer durationMinutes,
        Integer price,
        ServiceCategory category
) {
}
