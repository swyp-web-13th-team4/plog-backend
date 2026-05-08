package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.model.MapPin;
import lombok.Builder;

@Builder
public record MapPinResponse(Long placeId, Double latitude, Double longitude, Long count, String thumbnailUrl) {

  public static MapPinResponse from(MapPin mapPin) {
    return MapPinResponse.builder()
        .placeId(mapPin.getPlaceId())
        .latitude(mapPin.getLatitude())
        .longitude(mapPin.getLongitude())
        .count(mapPin.getCount())
            .thumbnailUrl(mapPin.getThumbnailUrl())
        .build();
  }
}
