package com.marriagehall.hall_service.controller;

import com.marriagehall.hall_service.dto.HallRequest;
import com.marriagehall.hall_service.entity.Hall;
import com.marriagehall.hall_service.service.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    /**
     * Public Marketplace Catalog & Search Endpoint
     * Matches the UX of The Knot / WeddingWire / Airbnb with pagination, faceted filtering, and sorting.
     */
    @GetMapping
    public ResponseEntity<Page<Hall>> getAllHalls(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Boolean hasAc,
            @RequestParam(required = false) Boolean hasParking,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(hallService.searchHalls(city, minPrice, maxPrice, minCapacity, hasAc, hasParking, pageable));
    }

    /**
     * Distinct cities for frontend search dropdown / autocomplete
     */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(hallService.getDistinctCities());
    }

    @PostMapping("/create-hall")
    public ResponseEntity<Hall> createHall(
            @Valid @RequestBody HallRequest hallRequest,
            @RequestHeader("X-User-Id") String vendorId,
            @RequestHeader("X-Role") String role
    ) {
        log.debug("Creating hall for vendorId={}, role={}", vendorId, role);
        return ResponseEntity.ok(hallService.createHall(hallRequest, vendorId, role));
    }

    @GetMapping("/{hallId}")
    public ResponseEntity<Hall> getHallById(@PathVariable UUID hallId) {
        return ResponseEntity.ok(hallService.getHallById(hallId));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Hall>> getVendorHalls(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(hallService.getVendorHalls(vendorId));
    }

    @PutMapping("/{hallId}")
    public ResponseEntity<Hall> updateHall(
            @PathVariable UUID hallId,
            @Valid @RequestBody HallRequest hallRequest,
            @RequestHeader("X-User-Id") String vendorId,
            @RequestHeader("X-Role") String role
    ) {
        return ResponseEntity.ok(hallService.updateHall(hallId, hallRequest, vendorId, role));
    }

    @DeleteMapping("/{hallId}")
    public ResponseEntity<Void> deleteHall(
            @PathVariable UUID hallId,
            @RequestHeader("X-User-Id") String vendorId,
            @RequestHeader("X-Role") String role
    ) {
        hallService.deleteHall(hallId, vendorId, role);
        return ResponseEntity.noContent().build();
    }
}
