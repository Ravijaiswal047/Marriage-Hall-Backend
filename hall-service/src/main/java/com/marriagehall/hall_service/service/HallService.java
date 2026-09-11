package com.marriagehall.hall_service.service;

import com.marriagehall.hall_service.dto.HallRequest;
import com.marriagehall.hall_service.entity.Hall;
import com.marriagehall.hall_service.exception.ForbiddenException;
import com.marriagehall.hall_service.exception.ResourceNotFoundException;
import com.marriagehall.hall_service.repository.HallRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;

    @Transactional
    public Hall createHall(HallRequest req, String vendorId, String role) {
        if (!"VENDOR".equalsIgnoreCase(role)) {
            throw new ForbiddenException("Only vendors can create halls");
        }
        if (vendorId == null || vendorId.isBlank()) {
            throw new ForbiddenException("Missing vendor identifier");
        }

        Hall hall = Hall.builder()
                .name(req.getName())
                .location(req.getLocation())
                .city(req.getCity() != null ? req.getCity() : req.getLocation())
                .state(req.getState())
                .address(req.getAddress())
                .landmark(req.getLandmark())
                .pincode(req.getPincode())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .price(req.getPrice())
                .vegPricePerPlate(req.getVegPricePerPlate())
                .nonVegPricePerPlate(req.getNonVegPricePerPlate())
                .capacity(req.getCapacity())
                .floatingCapacity(req.getFloatingCapacity())
                .description(req.getDescription())
                .coverImageUrl(req.getCoverImageUrl())
                .images(req.getImages() != null ? new ArrayList<>(req.getImages()) : new ArrayList<>())
                .hasAc(req.getHasAc() != null ? req.getHasAc() : true)
                .hasParking(req.getHasParking() != null ? req.getHasParking() : true)
                .parkingCapacity(req.getParkingCapacity())
                .roomsCount(req.getRoomsCount())
                .outsideCateringAllowed(req.getOutsideCateringAllowed() != null ? req.getOutsideCateringAllowed() : false)
                .djAllowed(req.getDjAllowed() != null ? req.getDjAllowed() : true)
                .alcoholAllowed(req.getAlcoholAllowed() != null ? req.getAlcoholAllowed() : false)
                .powerBackup(req.getPowerBackup() != null ? req.getPowerBackup() : true)
                .vendorId(UUID.fromString(vendorId))
                .status("ACTIVE")
                .build();

        return hallRepository.save(hall);
    }

    @Transactional(readOnly = true)
    public Hall getHallById(UUID hallId) {
        return hallRepository.findById(hallId).orElseThrow(
                () -> new ResourceNotFoundException("Hall with id " + hallId + " not found")
        );
    }

    @Transactional(readOnly = true)
    public List<Hall> getVendorHalls(UUID vendorId) {
        return hallRepository.findByVendorId(vendorId);
    }

    @Transactional(readOnly = true)
    public Page<Hall> searchHalls(String city, Double minPrice, Double maxPrice, Integer minCapacity,
                                 Boolean hasAc, Boolean hasParking, Pageable pageable) {
        Specification<Hall> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only show ACTIVE halls to public customers
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));

            if (city != null && !city.isBlank()) {
                String cityPattern = "%" + city.trim().toLowerCase() + "%";
                Predicate cityMatch = cb.like(cb.lower(root.get("city")), cityPattern);
                Predicate locationMatch = cb.like(cb.lower(root.get("location")), cityPattern);
                predicates.add(cb.or(cityMatch, locationMatch));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (minCapacity != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity));
            }
            if (hasAc != null) {
                predicates.add(cb.equal(root.get("hasAc"), hasAc));
            }
            if (hasParking != null) {
                predicates.add(cb.equal(root.get("hasParking"), hasParking));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return hallRepository.findAll(spec, pageable);
    }

    @Transactional
    public Hall updateHall(UUID hallId, HallRequest req, String vendorId, String role) {
        Hall hall = getHallById(hallId);

        if (!"ADMIN".equalsIgnoreCase(role) && !hall.getVendorId().toString().equalsIgnoreCase(vendorId)) {
            throw new ForbiddenException("You are not authorized to update this hall");
        }

        if (req.getName() != null) hall.setName(req.getName());
        if (req.getLocation() != null) hall.setLocation(req.getLocation());
        if (req.getCity() != null) hall.setCity(req.getCity());
        if (req.getState() != null) hall.setState(req.getState());
        if (req.getAddress() != null) hall.setAddress(req.getAddress());
        if (req.getLandmark() != null) hall.setLandmark(req.getLandmark());
        if (req.getPincode() != null) hall.setPincode(req.getPincode());
        if (req.getLatitude() != null) hall.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) hall.setLongitude(req.getLongitude());
        if (req.getPrice() != null) hall.setPrice(req.getPrice());
        if (req.getVegPricePerPlate() != null) hall.setVegPricePerPlate(req.getVegPricePerPlate());
        if (req.getNonVegPricePerPlate() != null) hall.setNonVegPricePerPlate(req.getNonVegPricePerPlate());
        if (req.getCapacity() != null) hall.setCapacity(req.getCapacity());
        if (req.getFloatingCapacity() != null) hall.setFloatingCapacity(req.getFloatingCapacity());
        if (req.getDescription() != null) hall.setDescription(req.getDescription());
        if (req.getCoverImageUrl() != null) hall.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getImages() != null) hall.setImages(new ArrayList<>(req.getImages()));
        if (req.getHasAc() != null) hall.setHasAc(req.getHasAc());
        if (req.getHasParking() != null) hall.setHasParking(req.getHasParking());
        if (req.getParkingCapacity() != null) hall.setParkingCapacity(req.getParkingCapacity());
        if (req.getRoomsCount() != null) hall.setRoomsCount(req.getRoomsCount());
        if (req.getOutsideCateringAllowed() != null) hall.setOutsideCateringAllowed(req.getOutsideCateringAllowed());
        if (req.getDjAllowed() != null) hall.setDjAllowed(req.getDjAllowed());
        if (req.getAlcoholAllowed() != null) hall.setAlcoholAllowed(req.getAlcoholAllowed());
        if (req.getPowerBackup() != null) hall.setPowerBackup(req.getPowerBackup());

        return hallRepository.save(hall);
    }

    @Transactional
    public void deleteHall(UUID hallId, String vendorId, String role) {
        Hall hall = getHallById(hallId);
        if (!"ADMIN".equalsIgnoreCase(role) && !hall.getVendorId().toString().equalsIgnoreCase(vendorId)) {
            throw new ForbiddenException("You are not authorized to delete this hall");
        }
        // Soft delete: sets status to INACTIVE so historical bookings remain intact
        hall.setStatus("INACTIVE");
        hallRepository.save(hall);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCities() {
        return hallRepository.findDistinctCities();
    }
}
