package com.server.booking.api;

import com.server.booking.application.BookingDTO;
import com.server.booking.application.BookingService;
import com.server.booking.application.CreateBookingCommand;
import com.server.organization.api.OrganizationDTO;
import com.server.organization.application.CreateOrganizationCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve booking by id")
    @ApiResponse(responseCode = "200", description = "Booking found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookingDTO.class)))
    public BookingDTO getBookingById(
            @Parameter(description = "ID of booking to retrieve", example = "1")
            @PathVariable int id) {
        return bookingService.getBooking(id);
    }


    @PostMapping
    @Operation(summary = "Create a new booking")
    @ApiResponse(responseCode = "201", description = "Booking created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookingDTO.class)))
    public int createBooking(@Valid @RequestBody CreateBookingCommand command) {
        return bookingService.createBooking(command);
    }
}
