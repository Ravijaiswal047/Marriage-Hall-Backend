package com.marriagehall.booking_service.dto;

import com.marriagehall.booking_service.enums.BookingSlot;
import com.marriagehall.booking_service.enums.EventType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingRequest {

    @NotNull(message = "hallId is required")
    private UUID hallId;

    @NotNull(message = "bookingDate is required")
    @Future(message = "bookingDate must be in the future")
    private LocalDate bookingDate;

    private BookingSlot slot;
    private EventType eventType;
    private Integer guestCount;
    private String customerName;
    private String customerPhone;
    private String specialRequests;
}
