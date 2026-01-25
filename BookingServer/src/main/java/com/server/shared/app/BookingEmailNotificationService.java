package com.server.shared.app;

import com.server.booking.events.BookingCreatedEvent;
import com.server.organization.domain.users.UserRepository;
import com.server.shared.infrastructure.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class BookingEmailNotificationService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private static final DateTimeFormatter EMAIL_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public BookingEmailNotificationService(
            EmailService emailService,
            UserRepository userRepository
    ) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public void notifyBookingCreated(BookingCreatedEvent event) {

        var clientOptional = userRepository.findById(event.clientId());
        var specialistOptional = userRepository.findById(event.specialistId());

        if (clientOptional.isEmpty() || specialistOptional.isEmpty()) {
            log.warn(
                    "Skipping booking notification. Missing user(s). bookingId={}",
                    event.bookingId()
            );
            return;
        }

        var client = clientOptional.get();
        var specialist = specialistOptional.get();

        sendSafely(
                client.getEmail().value(),
                "Booking created",
                buildClientEmail(event),
                "client",
                event.bookingId()
        );

        sendSafely(
                specialist.getEmail().value(),
                "New booking assigned",
                buildSpecialistEmail(event),
                "specialist",
                event.bookingId()
        );
    }

    private void sendSafely(
            String email,
            String subject,
            String body,
            String role,
            Integer bookingId
    ) {
        if (email == null || email.isBlank()) {
            log.warn(
                    "Skipping {} email notification. No email. bookingId={}",
                    role,
                    bookingId
            );
            return;
        }

        try {
            emailService.send(email, subject, body);
        } catch (MailException ex) {
            log.warn(
                    "Failed to send {} booking email to {}. bookingId={}",
                    role,
                    email,
                    bookingId,
                    ex
            );
        }
    }

    private String buildClientEmail(BookingCreatedEvent event) {
        var start = event.timeSlot().start()
                .format(EMAIL_DATE_TIME_FORMAT);

        var end = event.timeSlot().end()
                .format(EMAIL_DATE_TIME_FORMAT);

        return """
                Your booking has been created.
                Time: %s – %s
                """.formatted(start, end);
    }

    private String buildSpecialistEmail(BookingCreatedEvent event) {
        var start = event.timeSlot().start()
                .format(EMAIL_DATE_TIME_FORMAT);

        var end = event.timeSlot().end()
                .format(EMAIL_DATE_TIME_FORMAT);
        return """
                    You have a new booking.
                   Time: %s – %s
                """.formatted(start, end);
    }
}