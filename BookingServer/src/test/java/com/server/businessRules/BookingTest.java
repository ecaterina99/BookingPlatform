package com.server.businessRules;

import com.server.booking.domain.Booking;
import com.server.booking.domain.BookingStatus;
import com.server.booking.domain.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BookingTest {
    @Test
    void cancelByClient_moreThan2HoursBefore_bookingIsCancelled() {
        LocalDateTime start = LocalDateTime.now().plusHours(3);
        TimeSlot timeSlot = new TimeSlot(start, start.plusHours(1));

        Booking booking = new Booking(
                1,
                1,
                2,
                1,
                timeSlot,
                LocalDateTime.now()
        );

        LocalDateTime now = LocalDateTime.now();

        booking.cancelByClient(now);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void cancelByClient_lessThan2HoursBefore_throwsException() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(90);
        TimeSlot timeSlot = new TimeSlot(start, start.plusHours(1));

        Booking booking = new Booking(
                1, 1, 2, 1,
                timeSlot,
                LocalDateTime.now()
        );

        LocalDateTime now = LocalDateTime.now();

        assertThrows(IllegalStateException.class,
                () -> booking.cancelByClient(now)
        );
    }

    @Test
    void cancelByClient_alreadyCancelled_throwsException() {
        LocalDateTime start = LocalDateTime.now().plusHours(5);
        TimeSlot timeSlot = new TimeSlot(start, start.plusHours(1));

        Booking booking = new Booking(
                1, 2, 3, 1,
                timeSlot,
                BookingStatus.CANCELLED,
                LocalDateTime.now()
        );

        LocalDateTime now = LocalDateTime.now();
        assertThrows(IllegalStateException.class,
                () -> booking.cancelByClient(now)
        );
    }

}