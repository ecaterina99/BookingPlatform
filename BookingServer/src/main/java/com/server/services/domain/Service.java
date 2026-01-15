package com.server.services.domain;

import lombok.Getter;
import org.springframework.util.Assert;

@Getter
public class Service {
    private int id;
    private ServiceName name;
    private int organizationId;
    private String description;
    private ServiceDuration durationMinutes;
    private ServicePrice price;


    public Service(int id, ServiceName name, int organizationId, String description, ServiceDuration durationMinutes, ServicePrice price) {
        if (name == null) throw new IllegalArgumentException("name is required");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("description is required");
        this.id = id;
        this.name = name;
        this.organizationId = organizationId;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
    }

    public void changeName(ServiceName name) {
        this.name = name;
    }

    public void changeDuration(ServiceDuration durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void changePrice(ServicePrice price) {
        this.price = price;
    }

    public void changeDescription(String description) {
        Assert.notNull(description, "Description is required");
        this.description = description;
    }

}
