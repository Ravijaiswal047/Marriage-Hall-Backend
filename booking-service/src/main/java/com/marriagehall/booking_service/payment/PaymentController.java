package com.marriagehall.booking_service.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/advance")
    public ResponseEntity<PaymentEntity> payAdvance(
            @Valid @RequestBody PaymentRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(paymentService.payAdvance(request, idempotencyKey));
    }

    @PostMapping("/final")
    public ResponseEntity<PaymentEntity> payFinal(
            @Valid @RequestBody PaymentRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(paymentService.payFinal(request, idempotencyKey));
    }
}
