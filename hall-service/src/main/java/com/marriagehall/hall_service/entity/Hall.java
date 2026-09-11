package com.marriagehall.hall_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "halls", indexes = {
        @Index(name = "idx_hall_city", columnList = "city"),
        @Index(name = "idx_hall_vendor", columnList = "vendorId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hall {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    private String city;
    private String state;
    private String address;
    private String landmark;
    private String pincode;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Double price;

    private Double vegPricePerPlate;
    private Double nonVegPricePerPlate;

    @Column(nullable = false)
    private Integer capacity;

    private Integer floatingCapacity;

    @Column(length = 3000)
    private String description;

    private String coverImageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hall_images", joinColumns = @JoinColumn(name = "hall_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    // Amenities
    @Builder.Default
    private Boolean hasAc = true;

    @Builder.Default
    private Boolean hasParking = true;

    private Integer parkingCapacity;
    private Integer roomsCount;

    @Builder.Default
    private Boolean outsideCateringAllowed = false;

    @Builder.Default
    private Boolean djAllowed = true;

    @Builder.Default
    private Boolean alcoholAllowed = false;

    @Builder.Default
    private Boolean powerBackup = true;

    @Column(nullable = false)
    private UUID vendorId;

    @Builder.Default
    private String status = "ACTIVE";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (city == null && location != null) {
            city = location.split(",")[0].trim();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
