package com.marriagehall.booking_service.service;

import com.marriagehall.booking_service.client.HallServiceClient;
import com.marriagehall.booking_service.config.RabbitMQConfig;
import com.marriagehall.booking_service.dto.*;
import com.marriagehall.booking_service.entity.Booking;
import com.marriagehall.booking_service.enums.BookingSlot;
import com.marriagehall.booking_service.enums.BookingStatus;
import com.marriagehall.booking_service.enums.EventType;
import com.marriagehall.booking_service.exception.BusinessRuleException;
import com.marriagehall.booking_service.exception.ResourceNotFoundException;
import com.marriagehall.booking_service.payment.PaymentEntity;
import com.marriagehall.booking_service.payment.PaymentRepository;
import com.marriagehall.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final HallServiceClient hallServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Booking createBooking(BookingRequest req, UUID userId, String role) {
        if (!"USER".equalsIgnoreCase(role) && !"CUSTOMER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new BusinessRuleException("Only customers can create bookings");
        }

        BookingSlot requestedSlot = req.getSlot() != null ? req.getSlot() : BookingSlot.FULL_DAY;
        EventType requestedEventType = req.getEventType() != null ? req.getEventType() : EventType.WEDDING;

        // Check for slot conflicts on that date
        List<Booking> existingOnDate = bookingRepository.findByHallIdAndBookingDate(req.getHallId(), req.getBookingDate());
        for (Booking existing : existingOnDate) {
            if (existing.getStatus() != BookingStatus.CANCELLED) {
                if (existing.getSlot() == BookingSlot.FULL_DAY
                        || requestedSlot == BookingSlot.FULL_DAY
                        || existing.getSlot() == requestedSlot) {
                    throw new BusinessRuleException(String.format(
                            "Hall is already booked for %s (%s). Please select another date or slot.",
                            req.getBookingDate(), existing.getSlot()));
                }
            }
        }

        HallResponse hall = hallServiceClient.getHallDetails(req.getHallId());

        Booking booking = Booking.builder()
                .userId(userId)
                .hallId(req.getHallId())
                .bookingDate(req.getBookingDate())
                .slot(requestedSlot)
                .eventType(requestedEventType)
                .guestCount(req.getGuestCount())
                .customerName(req.getCustomerName())
                .customerPhone(req.getCustomerPhone())
                .specialRequests(req.getSpecialRequests())
                .totalAmount(hall.getPrice())
                .status(BookingStatus.PENDING)
                .build();

        Booking saved;
        try {
            saved = bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            // Concurrent collision caught by DB constraint
            throw new BusinessRuleException("Hall is already booked for this date and slot");
        }

        // Publish event to RabbitMQ
        try {
            BookingCreatedEvent event = new BookingCreatedEvent(
                    saved.getId(), saved.getUserId(), saved.getHallId(),
                    saved.getBookingDate(), saved.getTotalAmount(), saved.getStatus().name());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        } catch (Exception ex) {
            log.error("Failed to publish booking created event for bookingId {}: {}",
                    saved.getId(), ex.getMessage());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public SlotAvailabilityResponse checkAvailability(UUID hallId, LocalDate date) {
        List<Booking> bookings = bookingRepository.findByHallIdAndBookingDate(hallId, date);
        List<BookingSlot> bookedSlots = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getStatus() != BookingStatus.CANCELLED) {
                bookedSlots.add(b.getSlot());
                if (b.getSlot() == BookingSlot.FULL_DAY) {
                    bookedSlots.add(BookingSlot.MORNING);
                    bookedSlots.add(BookingSlot.EVENING);
                }
            }
        }

        List<BookingSlot> availableSlots = new ArrayList<>();
        if (!bookedSlots.contains(BookingSlot.FULL_DAY)
                && !bookedSlots.contains(BookingSlot.MORNING)
                && !bookedSlots.contains(BookingSlot.EVENING)) {
            availableSlots.add(BookingSlot.FULL_DAY);
        }
        if (!bookedSlots.contains(BookingSlot.MORNING)) {
            availableSlots.add(BookingSlot.MORNING);
        }
        if (!bookedSlots.contains(BookingSlot.EVENING)) {
            availableSlots.add(BookingSlot.EVENING);
        }

        return SlotAvailabilityResponse.builder()
                .hallId(hallId)
                .date(date)
                .available(!availableSlots.isEmpty())
                .bookedSlots(bookedSlots.stream().distinct().toList())
                .availableSlots(availableSlots)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SlotAvailabilityResponse> getBookedDates(UUID hallId, LocalDate startDate, LocalDate endDate) {
        List<Booking> activeBookings = bookingRepository.findByHallIdAndBookingDateBetweenAndStatusNot(
                hallId, startDate, endDate, BookingStatus.CANCELLED
        );
        Map<LocalDate, List<Booking>> grouped = activeBookings.stream()
                .collect(Collectors.groupingBy(Booking::getBookingDate));

        List<SlotAvailabilityResponse> responses = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Booking>> entry : grouped.entrySet()) {
            responses.add(checkAvailability(hallId, entry.getKey()));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingDetails(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return new BookingDetailResponse(booking, hallServiceClient.getHallDetails(booking.getHallId()));
    }

    @Transactional(readOnly = true)
    public BookingSummaryResponse getBookingSummary(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        List<PaymentEntity> payments = paymentRepository.findByBookingId(bookingId);
        double paidAmount = payments.stream().mapToDouble(PaymentEntity::getAmount).sum();
        double total = booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;
        double dueAmount = Math.max(0.0, total - paidAmount);

        return new BookingSummaryResponse(bookingId, total,
                paidAmount, dueAmount, dueAmount <= 0, booking.getStatus().name());
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessRuleException("Booking is already cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBookingStatus(UUID bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUser(UUID userId) {
        return bookingRepository.findByUserId(userId);
    }
}
