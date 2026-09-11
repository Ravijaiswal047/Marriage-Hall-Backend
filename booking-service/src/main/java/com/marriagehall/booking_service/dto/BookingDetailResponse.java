package com.marriagehall.booking_service.dto;

import com.marriagehall.booking_service.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingDetailResponse {
    private Booking booking;
    private HallResponse hall;

}
