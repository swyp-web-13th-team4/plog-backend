package com.plog.plogbackend.domain.review.repository.dto;

import java.time.LocalDateTime;

public record PlaceReviewBaseItem(
    Long reviewId,
    Long authorId,
    String nickname,
    String profileImage,
    Integer rating,
    LocalDateTime createdAt,
    String content) {}
