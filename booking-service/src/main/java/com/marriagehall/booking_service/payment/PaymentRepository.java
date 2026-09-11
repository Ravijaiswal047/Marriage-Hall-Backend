package com.marriagehall.booking_service.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    List<PaymentEntity> findByBookingId(UUID bookingId);

    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
}
