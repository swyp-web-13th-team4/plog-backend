package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import com.plog.plogbackend.global.support.paging.Slice;

public record PlaceReviewPageResponse(
    PlaceReviewSummaryResponse summary, PlaceReviewPageItemsResponse reviews) {

  public static PlaceReviewPageResponse from(PlaceReviewSummary summary) {
    return new PlaceReviewPageResponse(
        PlaceReviewSummaryResponse.from(summary), PlaceReviewPageItemsResponse.empty());
  }

  public static PlaceReviewPageResponse from(
      PlaceReviewSummary summary, Slice<PlaceReviewListItemResponse> reviews) {
    return new PlaceReviewPageResponse(
        PlaceReviewSummaryResponse.from(summary), PlaceReviewPageItemsResponse.from(reviews));
  }
}
