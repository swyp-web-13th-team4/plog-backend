package com.plog.plogbackend.domain.review.repository.dto;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record PlaceReviewListItem(
    Long reviewId,
    Long authorId,
    String nickname,
    String profileImageUrl,
    Integer rating,
    LocalDateTime createdAt,
    Map<ReviewEnvironmentName, Integer> environments,
    String content,
    List<String> imageUrls) {}
