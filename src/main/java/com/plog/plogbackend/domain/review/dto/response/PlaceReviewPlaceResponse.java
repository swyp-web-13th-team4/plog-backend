package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.place.entity.Place;

public record PlaceReviewPlaceResponse(Long placeId, String placeName) {

  public static PlaceReviewPlaceResponse from(Place place) {
    return new PlaceReviewPlaceResponse(place.getId(), place.getName());
  }
}
