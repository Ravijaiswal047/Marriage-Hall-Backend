package com.marriagehall.booking_service.controller;

import com.marriagehall.booking_service.dto.BookingDetailResponse;
import com.marriagehall.booking_service.dto.BookingRequest;
import com.marriagehall.booking_service.dto.BookingSummaryResponse;
import com.marriagehall.booking_service.dto.SlotAvailabilityResponse;
import com.marriagehall.booking_service.entity.Booking;
import com.marriagehall.booking_service.enums.BookingStatus;
import com.marriagehall.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<Booking> create(
            @Valid @RequestBody BookingRequest bookingRequest,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role
    ) {
        return ResponseEntity.ok(bookingService.createBooking(bookingRequest, UUID.fromString(userId), role));
    }

    /**
     * Check real-time slot availability for a specific hall on a single date
     * Publicly callable so guest users can see slot status before logging in
     */
    @GetMapping("/availability/{hallId}")
    public ResponseEntity<SlotAvailabilityResponse> checkAvailability(
            @PathVariable UUID hallId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(bookingService.checkAvailability(hallId, date));
    }

    /**
     * Get all booked slots for an entire date range (e.g. current month)
     * Used by the frontend interactive calendar to color-code dates (Green/Yellow/Red)
     */
    @GetMapping("/booked-dates/{hallId}")
    public ResponseEntity<List<SlotAvailabilityResponse>> getBookedDates(
            @PathVariable UUID hallId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(bookingService.getBookedDates(hallId, startDate, endDate));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDetailResponse> getBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBookingDetails(bookingId));
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<Booking> updateStatus(
            @PathVariable UUID bookingId,
            @RequestParam BookingStatus status
    ) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, status));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getMyBookings(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(UUID.fromString(userId)));
    }

    @GetMapping("/{bookingId}/summary")
    public ResponseEntity<BookingSummaryResponse> getBookingSummary(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBookingSummary(bookingId));
    }
}
