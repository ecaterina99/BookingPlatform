package com.server.booking.application;

import com.server.booking.domain.Booking;
import com.server.booking.domain.BookingAvailabilityService;
import com.server.booking.domain.BookingRepository;
import com.server.booking.domain.TimeSlot;
import com.server.booking.events.BookingCreatedEvent;
import com.server.booking.events.TimeSlotEvent;
import com.server.booking.infrastructure.BookingMapper;
import com.server.booking.infrastructure.kafka.BookingEventProducer;
import com.server.organization.domain.users.UserRepository;
import com.server.schedule.domain.Schedule;
import com.server.schedule.domain.ScheduleRepository;
import com.server.schedule.domain.WorkingHours;
import com.server.service.domain.ServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BookingMapper bookingMapper;
    private final ScheduleRepository scheduleRepository;
    private final BookingAvailabilityService availabilityService;
    private final BookingEventProducer bookingEventProducer;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            BookingMapper bookingMapper,
            ScheduleRepository scheduleRepository,
            BookingEventProducer bookingEventProducer
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.bookingMapper = bookingMapper;
        this.scheduleRepository = scheduleRepository;
        this.bookingEventProducer = bookingEventProducer;
        this.availabilityService = new BookingAvailabilityService();
    }

    @Transactional(readOnly = true)
    public BookingDTO getBooking(int id) {
        Booking booking = findBookingById(id);
        return bookingMapper.toDTO(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsBySpecialistId(int id) {
        List<Booking> bookings = bookingRepository.getBookingsBySpecialistId(id);
        return bookings.stream().map(bookingMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByClientId(int id) {
        List<Booking> bookings = bookingRepository.getBookingsByClientId(id);
        return bookings.stream().map(bookingMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.getAllBookings();
        return bookings.stream().map(bookingMapper::toDTO).toList();
    }

    @Transactional
    public int createBooking(CreateBookingCommand command) {
        validateEntitiesExist(command);

        com.server.service.domain.Service service = serviceRepository.findById(command.serviceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        LocalDateTime endTime = command.start()
                .plusMinutes(service.getDurationMinutes().minutes());

        TimeSlot requestedSlot = new TimeSlot(command.start(), endTime);

        Schedule schedule = scheduleRepository.findBySpecialistId(command.specialistId())
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        WorkingHours requestedHours = new WorkingHours(command.start(), endTime);
        if (!schedule.isAvailable(requestedHours)) {
            throw new IllegalStateException("Specialist is not available at this time");
        }
        List<Booking> existingBookings = bookingRepository.getBookingsBySpecialistId(command.specialistId());
        if (!availabilityService.hasNoConflicts(requestedSlot, existingBookings)) {
            throw new IllegalStateException("Time slot already booked");
        }


        Booking booking = new Booking(
                0,
                command.clientId(),
                command.specialistId(),
                command.serviceId(),
                requestedSlot,
                LocalDateTime.now()
        );
        bookingRepository.save(booking);

        bookingEventProducer.sendBookingCreated(
                new BookingCreatedEvent(
                        booking.getId(),
                        booking.getClientId(),
                        booking.getSpecialistId(),
                        booking.getServiceId(),
                        new TimeSlotEvent(
                                booking.getTimeSlot().start(),
                                booking.getTimeSlot().end()
                        ),
                        booking.getCreatedAt()
                )
        );

        return booking.getId();
    }

    @Transactional
    public void cancelBookingByClient(int bookingId, int clientId) throws AccessDeniedException {
        Booking booking = findBookingById(bookingId);

        if (booking.getClientId() != clientId) {
            throw new AccessDeniedException("Client does not own this booking");
        }

        booking.cancelByClient(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBookingBySpecialist(int bookingId, int specialistId) {
        Booking booking = findBookingById(bookingId);

        if (booking.getSpecialistId() != specialistId) {
            throw new SecurityException("Specialist does not own this booking");
        }

        booking.cancelBySpecialist();
        bookingRepository.save(booking);
    }

    @Transactional
    public int confirmBySpecialist(int bookingId, int specialistId) {
        Booking booking = findBookingById(bookingId);
        if (booking.getSpecialistId() != specialistId) {
            throw new SecurityException("Specialist does not own this booking");
        }
        booking.confirmBySpecialist();
        bookingRepository.save(booking);
        return booking.getId();
    }

    private void validateEntitiesExist(CreateBookingCommand command) {
        userRepository.findById(command.clientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        userRepository.findById(command.specialistId())
                .orElseThrow(() -> new EntityNotFoundException("Specialist not found"));
        serviceRepository.findById(command.serviceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
    }

    private Booking findBookingById(int id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking with id: " + id + " is not found"));
    }
}
