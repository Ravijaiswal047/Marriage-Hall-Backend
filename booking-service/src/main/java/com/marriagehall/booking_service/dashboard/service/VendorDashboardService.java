package com.marriagehall.booking_service.dashboard.service;

import com.marriagehall.booking_service.client.HallServiceClient;
import com.marriagehall.booking_service.dashboard.controller.dto.VendorBookingResponse;
import com.marriagehall.booking_service.dashboard.controller.dto.VendorDashboardResponse;
import com.marriagehall.booking_service.dto.HallResponse;
import com.marriagehall.booking_service.entity.Booking;
import com.marriagehall.booking_service.enums.BookingStatus;
import com.marriagehall.booking_service.exception.BusinessRuleException;
import com.marriagehall.booking_service.payment.PaymentEntity;
import com.marriagehall.booking_service.payment.PaymentRepository;
import com.marriagehall.booking_service.payment.PaymentStatus;
import com.marriagehall.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorDashboardService {

    private final HallServiceClient hallServiceClient;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public VendorDashboardResponse getDashboardStats(UUID vendorId, String role) {
        if (!"VENDOR".equalsIgnoreCase(role)) {
            throw new BusinessRuleException("Only VENDOR can access the vendor dashboard");
        }

        List<HallResponse> halls = hallServiceClient.getVendorHalls(vendorId);
        if (halls == null || halls.isEmpty()) {
            return new VendorDashboardResponse(0L, 0L, 0L, 0L, 0L, 0.0, 0.0, 0.0);
        }

        List<UUID> hallIds = halls.stream().map(HallResponse::getId).filter(Objects::nonNull).toList();
        List<Booking> bookings = bookingRepository.findByHallIdIn(hallIds);

        long totalHalls = halls.size();
        long totalBookings = bookings.size();
        long pendingBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long confirmedBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long cancelledBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        double totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0)
                .sum();

        double receivedAmount = 0.0;
        for (Booking b : bookings) {
            if (b.getStatus() != BookingStatus.CANCELLED) {
                List<PaymentEntity> payments = paymentRepository.findByBookingId(b.getId());
                receivedAmount += payments.stream()
                        .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                        .mapToDouble(PaymentEntity::getAmount)
                        .sum();
            }
        }

        double dueAmount = Math.max(0.0, totalRevenue - receivedAmount);

        return new VendorDashboardResponse(
                totalHalls, totalBookings, pendingBookings, confirmedBookings,
                cancelledBookings, totalRevenue, receivedAmount, dueAmount);
    }

    @Transactional(readOnly = true)
    public List<VendorBookingResponse> getVendorBookings(UUID vendorId, String role) {
        if (!"VENDOR".equalsIgnoreCase(role)) {
            throw new BusinessRuleException("Only VENDOR can access vendor bookings");
        }

        List<HallResponse> halls = hallServiceClient.getVendorHalls(vendorId);
        if (halls == null || halls.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, String> hallMap = halls.stream()
                .collect(Collectors.toMap(HallResponse::getId, HallResponse::getName, (a, b) -> a));

        List<UUID> hallIds = new ArrayList<>(hallMap.keySet());
        List<Booking> bookings = bookingRepository.findByHallIdIn(hallIds);

        List<VendorBookingResponse> responses = new ArrayList<>();
        for (Booking b : bookings) {
            List<PaymentEntity> payments = paymentRepository.findByBookingId(b.getId());
            double paid = payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                    .mapToDouble(PaymentEntity::getAmount)
                    .sum();
            double total = b.getTotalAmount() != null ? b.getTotalAmount() : 0.0;
            double due = Math.max(0.0, total - paid);

            String custName = (b.getCustomerName() != null && !b.getCustomerName().isBlank())
                    ? b.getCustomerName()
                    : "Customer-" + b.getUserId().toString().substring(0, 8);

            responses.add(VendorBookingResponse.builder()
                    .bookingId(b.getId())
                    .customerName(custName)
                    .customerPhone(b.getCustomerPhone())
                    .hallName(hallMap.getOrDefault(b.getHallId(), "Unknown Hall"))
                    .bookingDate(b.getBookingDate())
                    .slot(b.getSlot() != null ? b.getSlot().name() : "FULL_DAY")
                    .eventType(b.getEventType() != null ? b.getEventType().name() : "WEDDING")
                    .guestCount(b.getGuestCount())
                    .totalAmount(total)
                    .paidAmount(paid)
                    .dueAmount(due)
                    .status(b.getStatus().name())
                    .build()
            );
        }

        return responses;
    }
}
