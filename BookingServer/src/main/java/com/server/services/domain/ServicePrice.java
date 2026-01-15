package com.server.services.domain;

public record ServicePrice(int price) {
    public ServicePrice {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}
