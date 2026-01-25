package com.server.booking.events;

import java.time.LocalDateTime;

public record TimeSlotEvent
        (LocalDateTime start,
         LocalDateTime end
        ) {
}
