package com.plog.plogbackend.domain.post.controller.dto.response;

public record PlaceResponse(String name, String address, Double latitude, Double longitude) {

  public static PlaceResponse from(String name, String address, Double latitude, Double longitude) {
    return new PlaceResponse(name, address, latitude, longitude);
  }
}
