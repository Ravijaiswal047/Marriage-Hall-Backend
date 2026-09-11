package com.marriagehall.booking_service.client;

import com.marriagehall.booking_service.dto.HallResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "HALL-SERVICE")
public interface HallClient {

    @GetMapping("/api/halls/{hallId}")
    HallResponse getHallById(@PathVariable("hallId") UUID hallId);

    @GetMapping("/api/halls/vendor/{vendorId}")
    List<HallResponse> getVendorHalls(@PathVariable("vendorId") UUID vendorId);
}
