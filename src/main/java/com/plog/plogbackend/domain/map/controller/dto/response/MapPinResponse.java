package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.model.MapPin;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record MapPinResponse(
    Long placeId,
    String placeName,
    String address,
    Double latitude,
    Double longitude,
    Long count,
    Integer totalStudyTime,
    Double avgFocus,
    String thumbnailUrl,
    PlaceCategoryCode categoryCode,
    LocalDate lastStudyDate) {

  public static MapPinResponse from(MapPin mapPin) {
    return MapPinResponse.builder()
        .placeId(mapPin.getPlaceId())
        .placeName(mapPin.getPlaceName())
        .address(mapPin.getAddress())
        .latitude(mapPin.getLatitude())
        .longitude(mapPin.getLongitude())
        .count(mapPin.getCount())
        .totalStudyTime(mapPin.getTotalStudyTime())
        .avgFocus(mapPin.getAvgFocus())
        .thumbnailUrl(mapPin.getThumbnailUrl())
        .categoryCode(mapPin.getCategoryCode())
        .lastStudyDate(mapPin.getLastStudyDate())
        .build();
  }
}
