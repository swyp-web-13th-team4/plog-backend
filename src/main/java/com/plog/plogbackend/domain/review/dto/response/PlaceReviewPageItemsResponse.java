package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.global.support.paging.Slice;
import java.util.List;

public record PlaceReviewPageItemsResponse(
    List<PlaceReviewListItemResponse> content, boolean hasNext, String nextCursor) {

  public static PlaceReviewPageItemsResponse empty() {
    return new PlaceReviewPageItemsResponse(List.of(), false, null);
  }

  public static PlaceReviewPageItemsResponse from(Slice<PlaceReviewListItemResponse> slice) {
    return new PlaceReviewPageItemsResponse(
        slice.getContent(), slice.isHasNext(), slice.getNextCursor());
  }
}
