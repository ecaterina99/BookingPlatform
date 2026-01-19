package com.server.booking.domain;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository {

    Optional<Booking> findById(int id);

    public List<Booking> getAllBookings();

    public List<Booking> getBookingsBySpecialistId(int specialistId);

    public List<Booking> getBookingsByClientId(int clientId);

    public Booking add(Booking booking);

    public void delete(int bookingId);

}
