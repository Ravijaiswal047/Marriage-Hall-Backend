package com.marriagehall.booking_service.dashboard.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorDashboardResponse {

    private Long totalHalls;
    private Long totalBookings;
    private Long pendingBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private Double totalRevenue;
    private Double receivedAmount;
    private Double dueAmount;

}
