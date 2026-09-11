package com.marriagehall.hall_service.repository;

import com.marriagehall.hall_service.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface HallRepository extends JpaRepository<Hall, UUID>, JpaSpecificationExecutor<Hall> {
    List<Hall> findByVendorId(UUID vendorId);

    @Query("SELECT DISTINCT h.city FROM Hall h WHERE h.city IS NOT NULL AND h.status = 'ACTIVE' ORDER BY h.city ASC")
    List<String> findDistinctCities();
}
