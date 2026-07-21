package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.repository.dto.PlaceDetail;
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
    Long reviewCount,
    Double averageRating) {

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
        detail.getReviewCount(),
        detail.getAverageRating());
  }
}
