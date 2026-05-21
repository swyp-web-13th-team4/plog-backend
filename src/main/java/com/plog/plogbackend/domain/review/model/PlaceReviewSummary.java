package com.plog.plogbackend.domain.review.model;

import java.util.List;

public record PlaceReviewSummary(
        Long reviewCount,
        Double averageRating,
        List<PlaceReviewEnvironmentSummary> environments
) {
    public static PlaceReviewSummary empty() {
        return new PlaceReviewSummary(0L, 0.0, List.of());
    }
}
