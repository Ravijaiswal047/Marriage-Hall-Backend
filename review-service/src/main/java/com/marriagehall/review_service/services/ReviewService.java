package com.marriagehall.review_service.services;

import com.marriagehall.review_service.dto.ReviewRequest;
import com.marriagehall.review_service.model.Review;
import com.marriagehall.review_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public Review createReview(ReviewRequest req, UUID userId, String fallbackName) {
        String name = (req.getReviewerName() != null && !req.getReviewerName().isBlank())
                ? req.getReviewerName()
                : (fallbackName != null && !fallbackName.isBlank() ? fallbackName : "Verified Guest");

        Review review = Review.builder()
                .userId(userId)
                .hallId(req.getHallId())
                .reviewerName(name)
                .reviewerAvatar(req.getReviewerAvatar())
                .rating(req.getRating())
                .comment(req.getComment())
                .isVerifiedBooking(true)
                .build();

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getHallReviews(UUID hallId) {
        return reviewRepository.findByHallIdOrderByCreatedAtDesc(hallId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(UUID hallId) {
        Double avg = reviewRepository.getAverageRating(hallId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }
}
