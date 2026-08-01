package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.model.PlaceReviewEnvironmentSummary;

public record PlaceReviewEnvironmentSummaryResponse(
    String environmentName, String iconName, Integer score, Long count) {

  public static PlaceReviewEnvironmentSummaryResponse from(PlaceReviewEnvironmentSummary summary) {
    return new PlaceReviewEnvironmentSummaryResponse(
        summary.name().getValue(), summary.name().getIconName(), summary.score(), summary.count());
  }
}
