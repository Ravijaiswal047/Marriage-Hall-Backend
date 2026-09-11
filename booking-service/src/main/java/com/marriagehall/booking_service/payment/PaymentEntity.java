package com.marriagehall.booking_service.payment;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class PaymentEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private UUID bookingId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // Idempotency: unique per payment attempt. Same key -> same payment, never charged twice.
    @Column(unique = true)
    private String idempotencyKey;

    private LocalDateTime paymentDate;

    @PrePersist
    public void prePersist() {
        paymentDate = LocalDateTime.now();
    }
}
