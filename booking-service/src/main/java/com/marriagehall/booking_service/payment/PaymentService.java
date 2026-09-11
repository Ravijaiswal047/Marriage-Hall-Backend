package com.marriagehall.booking_service.payment;

import com.marriagehall.booking_service.entity.Booking;
import com.marriagehall.booking_service.enums.BookingStatus;
import com.marriagehall.booking_service.exception.BusinessRuleException;
import com.marriagehall.booking_service.exception.ResourceNotFoundException;
import com.marriagehall.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public PaymentEntity payAdvance(PaymentRequestDTO request, String idempotencyKey) {
        return processPayment(request, PaymentType.ADVANCE, idempotencyKey);
    }

    @Transactional
    public PaymentEntity payFinal(PaymentRequestDTO request, String idempotencyKey) {
        return processPayment(request, PaymentType.FINAL, idempotencyKey);
    }

    @Transactional
    public PaymentEntity processPayment(PaymentRequestDTO request,
                                         PaymentType type, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessRuleException("Idempotency-Key header is required");
        }

        // 1. Idempotency check: same key already processed -> return the ORIGINAL result (no new charge)
        Optional<PaymentEntity> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. First time for this key -> process the payment
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot process payment for a cancelled booking");
        }

        List<PaymentEntity> existingPayments = paymentRepository.findByBookingId(booking.getId());
        double alreadyPaid = existingPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(PaymentEntity::getAmount)
                .sum();
        double total = booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;

        if (alreadyPaid >= total) {
            throw new BusinessRuleException("Booking is already fully paid");
        }
        if (alreadyPaid + request.getAmount() > total + 0.001) {
            throw new BusinessRuleException(String.format(
                    "Payment amount (%.2f) exceeds remaining due amount (%.2f)",
                    request.getAmount(), (total - alreadyPaid)));
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setBookingId(booking.getId());
        payment.setAmount(request.getAmount());
        payment.setPaymentType(type);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setIdempotencyKey(idempotencyKey);

        PaymentEntity savedPayment;
        try {
            savedPayment = paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            // 3. Race condition: two concurrent requests with the SAME key.
            //    DB unique constraint blocks the second insert -> return the one that won.
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new BusinessRuleException("Duplicate payment detected"));
        }

        // Confirm booking once payment is received
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        return savedPayment;
    }
}
