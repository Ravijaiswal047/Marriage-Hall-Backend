package com.marriagehall.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private UUID bookingId;
    private UUID userId;
    private UUID hallId;
    private LocalDate bookingDate;
    private Double totalAmount;
    private String status;
}
