package com.marriagehall.booking_service.client;

import com.marriagehall.booking_service.dto.HallResponse;
import com.marriagehall.booking_service.exception.BusinessRuleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallServiceClient {

    private final HallClient hallClient;

    @CircuitBreaker(name = "hallService", fallbackMethod = "getHallFallback")
    @Retry(name = "hallService")
    @RateLimiter(name = "hallService")
    public HallResponse getHallDetails(UUID hallId) {
        return hallClient.getHallById(hallId);
    }

    public HallResponse getHallFallback(UUID hallId, Throwable t) {
        log.error("Hall service call failed for hallId {}: {}", hallId, t.getMessage());
        throw new BusinessRuleException("Hall service is temporarily unavailable. Please try again shortly.");
    }

    @CircuitBreaker(name = "hallService", fallbackMethod = "getVendorHallsFallback")
    @Retry(name = "hallService")
    @RateLimiter(name = "hallService")
    public List<HallResponse> getVendorHalls(UUID vendorId) {
        return hallClient.getVendorHalls(vendorId);
    }

    public List<HallResponse> getVendorHallsFallback(UUID vendorId, Throwable t) {
        log.error("Hall service call failed for vendorId {}: {}", vendorId, t.getMessage());
        return Collections.emptyList();
    }
}
