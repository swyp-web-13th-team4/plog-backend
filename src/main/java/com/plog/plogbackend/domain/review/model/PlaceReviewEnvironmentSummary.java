package com.plog.plogbackend.domain.review.model;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;

public record PlaceReviewEnvironmentSummary(
    ReviewEnvironmentName name, Integer score, Long count) {}
