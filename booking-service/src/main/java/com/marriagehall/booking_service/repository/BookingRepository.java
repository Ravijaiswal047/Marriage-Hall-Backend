package com.marriagehall.booking_service.repository;

import com.marriagehall.booking_service.entity.Booking;
import com.marriagehall.booking_service.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    boolean existsByHallIdAndBookingDate(UUID hallId, LocalDate bookingDate);

    List<Booking> findByHallIdAndBookingDate(UUID hallId, LocalDate bookingDate);

    List<Booking> findByHallIdAndBookingDateBetweenAndStatusNot(
            UUID hallId, LocalDate startDate, LocalDate endDate, BookingStatus status
    );

    List<Booking> findByUserId(UUID userId);

    List<Booking> findByHallIdIn(List<UUID> hallIds);
}
