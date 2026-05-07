package com.plog.plogbackend.domain.post.service.dto;

import com.plog.plogbackend.domain.post.controller.dto.request.post.RecentPlaceSaveRequest;

public record RecentPlaceSaveCommand(
    String placeName, String address, Double latitude, Double longitude) {

  public static RecentPlaceSaveCommand from(RecentPlaceSaveRequest saveReq) {
    return new RecentPlaceSaveCommand(
        saveReq.placeName(), saveReq.address(), saveReq.latitude(), saveReq.longitude());
  }
}
