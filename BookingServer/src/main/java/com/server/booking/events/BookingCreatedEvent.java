package com.server.booking.events;

import com.server.booking.events.TimeSlotEvent;

import java.time.LocalDateTime;

public record BookingCreatedEvent(
        Integer bookingId,
        Integer clientId,
        Integer specialistId,
        Integer serviceId,
        TimeSlotEvent timeSlot,
        LocalDateTime createdAt
) {}