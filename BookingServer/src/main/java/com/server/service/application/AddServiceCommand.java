package com.server.service.application;

import com.server.service.domain.ServiceCategory;

public record AddServiceCommand(
        String name,
        int organizationId,
        String description,
        int durationMinutes,
        int price,
        ServiceCategory category
) {
}
