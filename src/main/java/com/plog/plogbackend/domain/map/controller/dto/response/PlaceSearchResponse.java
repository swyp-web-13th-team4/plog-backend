package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.model.PlaceSearchResult;
import java.time.LocalDate;

public record PlaceSearchResponse(
    Long placeId,
    String placeName,
    String address,
    Double latitude,
    Double longitude,
    LocalDate lastStudyDate) {

  public static PlaceSearchResponse from(PlaceSearchResult result) {
    return new PlaceSearchResponse(
        result.getPlaceId(),
        result.getPlaceName(),
        result.getAddress(),
        result.getLatitude(),
        result.getLongitude(),
        result.getLastStudyDate());
  }
}
