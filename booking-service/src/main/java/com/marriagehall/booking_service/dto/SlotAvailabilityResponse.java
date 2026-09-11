package com.marriagehall.booking_service.dto;

import com.marriagehall.booking_service.enums.BookingSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlotAvailabilityResponse {
    private UUID hallId;
    private LocalDate date;
    private boolean available;
    private List<BookingSlot> bookedSlots;
    private List<BookingSlot> availableSlots;
}
