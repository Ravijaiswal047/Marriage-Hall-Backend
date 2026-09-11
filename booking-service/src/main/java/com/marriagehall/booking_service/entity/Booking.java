package com.marriagehall.booking_service.entity;

import com.marriagehall.booking_service.enums.BookingSlot;
import com.marriagehall.booking_service.enums.BookingStatus;
import com.marriagehall.booking_service.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings", uniqueConstraints = @UniqueConstraint(
        name = "uk_booking_hall_date_slot",
        columnNames = {"hallId", "bookingDate", "slot"}
))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @UuidGenerator
    @GeneratedValue
    private UUID id;
    private UUID userId;
    private UUID hallId;

    private LocalDate bookingDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingSlot slot = BookingSlot.FULL_DAY;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventType eventType = EventType.WEDDING;

    private Integer guestCount;
    private String customerName;
    private String customerPhone;

    @Column(length = 1000)
    private String specialRequests;

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slot == null) {
            slot = BookingSlot.FULL_DAY;
        }
        if (eventType == null) {
            eventType = EventType.WEDDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
