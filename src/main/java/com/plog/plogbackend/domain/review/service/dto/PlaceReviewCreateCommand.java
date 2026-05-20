package com.plog.plogbackend.domain.review.service.dto;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import java.util.Map;
import java.util.UUID;

public record PlaceReviewCreateCommand(
    Long postId,
    UUID memberKey,
    Integer rating,
    String content,
    Map<ReviewEnvironmentName, Integer> environments) {}
