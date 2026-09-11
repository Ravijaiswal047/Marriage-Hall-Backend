package com.marriagehall.booking_service.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "bookingId is required")
    private UUID bookingId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private Double amount;
}
