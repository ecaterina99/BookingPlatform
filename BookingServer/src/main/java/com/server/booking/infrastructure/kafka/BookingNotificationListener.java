package com.server.booking.infrastructure.kafka;

//kafka consumer

import com.server.booking.events.BookingCreatedEvent;
import com.server.shared.app.BookingEmailNotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingNotificationListener {
    private final BookingEmailNotificationService emailNotificationService;

    public BookingNotificationListener(BookingEmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }


    @KafkaListener(
            topics = "booking.created",
            groupId = "notification-group"
    )

    public void handleBookingCreated(BookingCreatedEvent event) {
        emailNotificationService.notifyBookingCreated(event);
    }
}
