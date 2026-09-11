package com.marriagehall.review_service.controller;

import com.marriagehall.review_service.dto.ReviewRequest;
import com.marriagehall.review_service.model.Review;
import com.marriagehall.review_service.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<Review> createReview(
            @Valid @RequestBody ReviewRequest reviewRequest,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader(value = "X-Email", required = false) String email) {
        return ResponseEntity.ok(reviewService.createReview(reviewRequest, userId, email));
    }

    // FIX: path var name now matches the method parameter
    @GetMapping("/{hallId}")
    public ResponseEntity<?> getReview(@PathVariable("hallId") UUID hallId) {
        return ResponseEntity.ok(reviewService.getHallReviews(hallId));
    }

    // FIX: was missing @GetMapping entirely -> now a real endpoint on a distinct path
    @GetMapping("/{hallId}/average")
    public ResponseEntity<?> averageRating(@PathVariable("hallId") UUID hallId) {
        return ResponseEntity.ok(reviewService.getAverageRating(hallId));
    }
}
