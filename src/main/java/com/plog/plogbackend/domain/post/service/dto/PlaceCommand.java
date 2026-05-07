package com.plog.plogbackend.domain.post.service.dto;

import com.plog.plogbackend.domain.post.controller.dto.request.post.PlaceRequest;

public record PlaceCommand(String name, String address, Double latitude, Double longitude) {
  public static PlaceCommand from(PlaceRequest req) {
    return new PlaceCommand(req.name(), req.address(), req.latitude(), req.longitude());
  }
}
