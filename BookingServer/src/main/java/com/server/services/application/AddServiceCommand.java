package com.server.services.application;

public record AddServiceCommand(
        String name,
        int organizationId,
        String description,
        int durationMinutes,
        int price
) {

}
