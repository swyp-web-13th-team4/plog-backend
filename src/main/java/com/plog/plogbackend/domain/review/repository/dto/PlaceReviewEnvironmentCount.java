package com.plog.plogbackend.domain.review.repository.dto;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;

public record PlaceReviewEnvironmentCount(
        ReviewEnvironmentName name,
        Integer score,
        Long count
) {}
