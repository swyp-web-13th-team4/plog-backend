package com.plog.plogbackend.domain.review.repository.dto;

import java.time.LocalDateTime;

public record PlaceReviewBaseItem(
    Long reviewId, String nickname, Integer rating, LocalDateTime createdAt, String content) {}
