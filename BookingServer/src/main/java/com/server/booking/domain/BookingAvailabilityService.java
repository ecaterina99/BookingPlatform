package com.server.booking.domain;

import java.util.List;

public class BookingAvailabilityService {

    public boolean hasNoConflicts(TimeSlot requestedSlot, List<Booking> existingBookings) {
        return existingBookings.stream()
                .noneMatch(booking -> booking.getTimeSlot().overlaps(requestedSlot));
    }
}
