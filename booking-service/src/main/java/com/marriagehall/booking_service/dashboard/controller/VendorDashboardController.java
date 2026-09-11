package com.marriagehall.booking_service.dashboard.controller;

import com.marriagehall.booking_service.dashboard.controller.dto.VendorBookingResponse;
import com.marriagehall.booking_service.dashboard.controller.dto.VendorDashboardResponse;
import com.marriagehall.booking_service.dashboard.service.VendorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendor/dashboard")
@RequiredArgsConstructor
public class VendorDashboardController {

    private final VendorDashboardService vendorDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<VendorDashboardResponse> getStats(
            @RequestHeader("X-User-Id") String vendorId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(vendorDashboardService.getDashboardStats(UUID.fromString(vendorId), role));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<VendorBookingResponse>> getBookings(
            @RequestHeader("X-User-Id") String vendorId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(vendorDashboardService.getVendorBookings(UUID.fromString(vendorId), role));
    }
}
