package com.server.booking.api;

import com.server.booking.application.*;
import com.server.shared.infrastructure.security.SecurityCurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingService bookingService;
    private final SecurityCurrentUserProvider currentUserProvider;

    public BookingController(BookingService bookingService, SecurityCurrentUserProvider currentUserProvider) {
        this.bookingService = bookingService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve booking by id")
    @ApiResponse(responseCode = "200", description = "Booking found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookingDTO.class)))
    @PreAuthorize("@orgAccessEvaluator.canViewBooking(#id)")
    public BookingDTO getBookingById(
            @Parameter(description = "ID of booking to retrieve", example = "1")
            @PathVariable int id) {
        return bookingService.getBooking(id);
    }

    @GetMapping("/specialist/{specialistId}")
    @Operation(summary = "Retrieve bookings by specialist Id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all specialist bookings",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookingDTO.class)))
    @PreAuthorize("@orgAccessEvaluator.isResourceOwner(#specialistId) or hasRole('GLOBAL_ADMIN')")
    public List<BookingDTO> getBookingBySpecialistId(
            @Parameter(description = "ID of specialist", example = "1")
            @PathVariable int specialistId) {
        return bookingService.getBookingsBySpecialistId(specialistId);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Retrieve bookings by client Id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all client bookings",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookingDTO.class)))
    @PreAuthorize("@orgAccessEvaluator.isResourceOwner(#clientId) or hasRole('GLOBAL_ADMIN')")
    public List<BookingDTO> getBookingByClientId(
            @Parameter(description = "ID of client", example = "1")
            @PathVariable int clientId) {
        return bookingService.getBookingsByClientId(clientId);
    }

    @GetMapping("/")
    @Operation(summary = "Retrieve all bookings")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all bookings",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookingDTO.class)))
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public List<BookingDTO> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new booking")
    @ApiResponse(responseCode = "201", description = "Booking created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookingDTO.class)))
    public int createBooking(@Valid @RequestBody CreateBookingCommand command) {
        return bookingService.createBooking(command);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending booking (specialist only)")
    @ApiResponse(responseCode = "200", description = "Booking confirmed successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - not the assigned specialist")
    @ApiResponse(responseCode = "400", description = "Booking is not in PENDING status")
    @PreAuthorize("@orgAccessEvaluator.canManageBooking(#id)")
    public int confirmBooking(
            @Parameter(description = "ID of booking to confirm", example = "1")
            @PathVariable int id) {
        int specialistId = currentUserProvider.getUserId();
        return bookingService.confirmBySpecialist(id, specialistId);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking (specialist cancellation)")
    @ApiResponse(responseCode = "200", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - not the assigned specialist")
    @PreAuthorize("@orgAccessEvaluator.canManageBooking(#id)")
    public void cancelBookingBySpecialist(
            @Parameter(description = "ID of booking to cancel", example = "1")
            @PathVariable int id) {
        int specialistId = currentUserProvider.getUserId();
        bookingService.cancelBookingBySpecialist(id, specialistId);
    }

    @PatchMapping("/{id}/cancel/client")
    @Operation(summary = "Cancel a booking (client cancellation)")
    @ApiResponse(responseCode = "200", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - not the booking client")
    @ApiResponse(responseCode = "400", description = "Cannot cancel less than 2 hours before")
    @PreAuthorize("@orgAccessEvaluator.isBookingClient(#id)")
    public void cancelBookingByClient(
            @Parameter(description = "ID of booking to cancel", example = "1")
            @PathVariable int id) throws AccessDeniedException {
        int clientId = currentUserProvider.getUserId();
        bookingService.cancelBookingByClient(id, clientId);
    }
}
