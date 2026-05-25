package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;

public record PlaceReviewPageResponse(
    PlaceReviewSummaryResponse summary, PlaceReviewPageItemsResponse reviews) {

  public static PlaceReviewPageResponse from(PlaceReviewSummary summary) {
    return new PlaceReviewPageResponse(
        PlaceReviewSummaryResponse.from(summary), PlaceReviewPageItemsResponse.empty());
  }
}
