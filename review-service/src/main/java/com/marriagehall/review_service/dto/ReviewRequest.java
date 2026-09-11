package com.marriagehall.review_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewRequest {

    @NotNull(message = "hallId is required")
    private UUID hallId;

    private String reviewerName;
    private String reviewerAvatar;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 2000, message = "comment cannot exceed 2000 characters")
    private String comment;
}