package com.marriagehall.hall_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class HallRequest {

    @NotBlank(message = "Hall name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    private String city;
    private String state;
    private String address;
    private String landmark;
    private String pincode;

    private Double latitude;
    private Double longitude;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Double price;

    private Double vegPricePerPlate;
    private Double nonVegPricePerPlate;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Integer floatingCapacity;

    @Size(max = 3000, message = "Description cannot exceed 3000 characters")
    private String description;

    private String coverImageUrl;
    private List<String> images;

    // Amenities
    private Boolean hasAc;
    private Boolean hasParking;
    private Integer parkingCapacity;
    private Integer roomsCount;
    private Boolean outsideCateringAllowed;
    private Boolean djAllowed;
    private Boolean alcoholAllowed;
    private Boolean powerBackup;
}
