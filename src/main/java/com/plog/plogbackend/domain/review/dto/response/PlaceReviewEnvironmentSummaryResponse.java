package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.model.PlaceReviewEnvironmentSummary;

public record PlaceReviewEnvironmentSummaryResponse(
    String environmentName,
    String title,
    String iconName,
    Integer score,
    String label,
    Long count) {

  public static PlaceReviewEnvironmentSummaryResponse from(PlaceReviewEnvironmentSummary summary) {
    return new PlaceReviewEnvironmentSummaryResponse(
        summary.name().getValue(),
        summary.name().getTitle(),
        summary.name().getIconName(),
        summary.score(),
        summary.name().getLabel(summary.score()),
        summary.count());
  }
}
