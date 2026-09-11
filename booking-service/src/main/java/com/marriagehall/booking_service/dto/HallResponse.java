package com.marriagehall.booking_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class HallResponse {

    private UUID id;
    private String name;
    private String location;
    private Double price;
    private Integer capacity;
    private String description;

}
