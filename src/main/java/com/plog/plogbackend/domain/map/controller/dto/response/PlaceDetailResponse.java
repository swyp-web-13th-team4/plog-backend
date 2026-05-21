package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.model.PlaceDetail;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;

public record PlaceDetailResponse(
    Long placeId,
    String placeName,
    String address,
    Long count,
    Double avgFocus,
    Long totalStudyTime,
    String thumbnailUrl,
    PlaceCategoryCode placeCategory,
    PlaceReviewSummaryResponse reviewSummary) {

  public static PlaceDetailResponse from(PlaceDetail detail) {
    return new PlaceDetailResponse(
        detail.getPlaceId(),
        detail.getPlaceName(),
        detail.getAddress(),
        detail.getCount(),
        detail.getAvgFocus(),
        detail.getTotalStudyTime(),
        detail.getThumbnailUrl(),
        detail.getPlaceCategory(),
        PlaceReviewSummaryResponse.from(detail.getReviewSummary()));
  }
}
