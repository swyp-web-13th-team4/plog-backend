package com.plog.plogbackend.domain.map.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Viewport {
  private final Double swLat;
  private final Double swLng;
  private final Double neLat;
  private final Double neLng;

  public static Viewport of(double swLat, double swLng, double neLat, double neLng) {
    if (swLat > neLat || swLng > neLng) {
      throw new com.plog.plogbackend.global.error.AppException(
          com.plog.plogbackend.global.error.ErrorType.INVALID_VIEWPORT_RANGE);
    }
    return Viewport.builder().swLat(swLat).swLng(swLng).neLat(neLat).neLng(neLng).build();
  }
}
