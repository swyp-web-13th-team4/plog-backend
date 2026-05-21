package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.repository.dto.PlaceSummary;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;

public record PlaceSummaryResponse(
    Long placeId,
    String placeName,
    String address,
    Double latitude,
    Double longitude,
    Long count,
    String thumbnailUrl,
    PlaceCategoryCode placeCategory,
    Long totalStudyTime,
    Double avgFocus) {

  public static PlaceSummaryResponse from(PlaceSummary summary) {
    return new PlaceSummaryResponse(
        summary.getPlaceId(),
        summary.getPlaceName(),
        summary.getAddress(),
        summary.getLatitude(),
        summary.getLongitude(),
        summary.getCount(),
        summary.getThumbnailUrl(),
        summary.getPlaceCategory(),
        summary.getTotalStudyTime(),
        summary.getAvgFocus());
  }
}
