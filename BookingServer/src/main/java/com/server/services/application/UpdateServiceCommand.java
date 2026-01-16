package com.server.services.application;

    public record UpdateServiceCommand(
            int id,
            String name,
            int organizationId,
            String description,
            int durationMinutes,
            int price
    ) {
    }

