package com.plog.plogbackend.domain.place.dto.response;

import com.plog.plogbackend.domain.place.entity.Place;

public record PlaceNameResponse(Long placeId, String placeName) {

  public static PlaceNameResponse from(Place place) {
    return new PlaceNameResponse(place.getId(), place.getName());
  }
}
