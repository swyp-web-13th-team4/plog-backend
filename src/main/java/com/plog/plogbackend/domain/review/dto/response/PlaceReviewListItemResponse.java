package com.plog.plogbackend.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceReviewListItemResponse(
    Long reviewId,
    String nickname,
    Integer rating,
    LocalDateTime createdAt,
    List<PlaceReviewEnvironmentItemResponse> environments,
    String content,
    List<String> imageUrls) {}
