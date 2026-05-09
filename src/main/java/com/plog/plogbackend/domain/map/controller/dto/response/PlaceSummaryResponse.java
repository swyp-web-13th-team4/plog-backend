package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.model.PlaceSummary;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import java.time.LocalDate;

public record PlaceSummaryResponse(
    Long placeId,
    String placeName,
    String address,
    Double latitude,
    Double longitude,
    Long count,
    String thumbnailUrl,
    PlaceCategoryCode placeCategory,
    LocalDate lastStudyDate,
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
        summary.getLastStudyDate(),
        summary.getTotalStudyTime(),
        summary.getAvgFocus());
  }
}
