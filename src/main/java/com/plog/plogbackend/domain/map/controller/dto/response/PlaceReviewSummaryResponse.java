package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import java.util.List;

public record PlaceReviewSummaryResponse(
    Long reviewCount,
    Double averageRating,
    List<PlaceReviewEnvironmentSummaryResponse> environments) {

  public static PlaceReviewSummaryResponse from(PlaceReviewSummary summary) {
    if (summary.reviewCount() == 0) {
      return null;
    }

    return new PlaceReviewSummaryResponse(
        summary.reviewCount(),
        summary.averageRating(),
        summary.environments().stream().map(PlaceReviewEnvironmentSummaryResponse::from).toList());
  }
}
