package com.server.booking.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Booking {

    private int id;
    private Integer clientId;
    private Integer specialistId;
    private Integer serviceId;
    private TimeSlot timeSlot;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public Booking(int id, Integer clientId, Integer specialistId, Integer serviceId,
                   TimeSlot timeSlot, LocalDateTime createdAt) {
        if (specialistId == null || serviceId == null || clientId == null)
            throw new IllegalArgumentException("Ids must not be null");

        this.id = id;
        this.clientId = clientId;
        this.serviceId = serviceId;
        this.specialistId = specialistId;
        this.timeSlot = timeSlot;
        this.status = BookingStatus.PENDING;
        this.createdAt = createdAt;
    }

    public void cancelByClient(LocalDateTime now) {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled");
        }

        if (now.isAfter(timeSlot.start().minusHours(2))) {
            throw new IllegalStateException("Client cannot cancel less than 2h before");
        }
        this.status = BookingStatus.CANCELLED;
    }

    public void cancelBySpecialist() {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled");
        }

        this.status = BookingStatus.CANCELLED;
    }

    public void confirmBySpecialist() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }

        this.status = BookingStatus.CONFIRMED;
    }
}
