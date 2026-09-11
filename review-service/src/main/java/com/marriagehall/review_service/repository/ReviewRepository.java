package com.marriagehall.review_service.repository;

import com.marriagehall.review_service.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByHallIdOrderByCreatedAtDesc(UUID hallId);
    List<Review> findByHallId(UUID hallId);

    @Query(
            """
         SELECT AVG(r.rating)
         FROM Review r
         WHERE r.hallId = :hallId
""")
    Double getAverageRating(UUID hallId);
}
