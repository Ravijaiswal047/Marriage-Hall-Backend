package com.marriagehall.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingSummaryResponse {

    private UUID bookingId;
    private Double totalAmount;
    private Double paidAmount;
    private Double dueAmount;
    private Boolean fullyPaid;
    private String status;

}
