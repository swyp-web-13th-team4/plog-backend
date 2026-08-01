package com.plog.plogbackend.domain.place.dto.response;

import com.plog.plogbackend.domain.place.entity.Place;

public record PlaceInfoResponse(
    Long placeId, String name, String address, Double latitude, Double longitude) {

  public static PlaceInfoResponse from(Place place) {
    return new PlaceInfoResponse(
        place.getId(),
        place.getName(),
        place.getAddress(),
        place.getLatitude(),
        place.getLongitude());
  }
}
