package com.plog.plogbackend.domain.review.dto.response;

public record PlaceReviewEnvironmentItemResponse(
        String environmentName,
        String title,
        String iconName,
        Integer score,
        String label) {}
