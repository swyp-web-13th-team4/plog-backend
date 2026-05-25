package com.plog.plogbackend.domain.review.dto.response;

import java.util.List;

public record PlaceReviewPageItemsResponse(
    List<PlaceReviewListItemResponse> content, boolean hasNext, String nextCursor) {

  public static PlaceReviewPageItemsResponse empty() {
    return new PlaceReviewPageItemsResponse(List.of(), false, null);
  }
}
