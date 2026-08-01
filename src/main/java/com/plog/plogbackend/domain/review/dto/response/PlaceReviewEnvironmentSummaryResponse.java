package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.model.PlaceReviewEnvironmentSummary;

public record PlaceReviewEnvironmentSummaryResponse(
    String environmentName, Integer score, Long count) {

  public static PlaceReviewEnvironmentSummaryResponse from(PlaceReviewEnvironmentSummary summary) {
    return new PlaceReviewEnvironmentSummaryResponse(
        summary.name().getValue(), summary.score(), summary.count());
  }
}
