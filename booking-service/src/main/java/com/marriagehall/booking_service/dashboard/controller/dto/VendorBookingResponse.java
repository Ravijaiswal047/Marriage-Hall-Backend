package com.marriagehall.booking_service.dashboard.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorBookingResponse {

    private UUID bookingId;
    private String customerName;
    private String customerPhone;
    private String hallName;
    private LocalDate bookingDate;
    private String slot;
    private String eventType;
    private Integer guestCount;
    private Double totalAmount;
    private Double paidAmount;
    private Double dueAmount;
    private String status;
}
