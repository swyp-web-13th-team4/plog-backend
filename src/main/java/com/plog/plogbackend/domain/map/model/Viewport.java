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

  public static Viewport of(Double swLat, Double swLng, Double neLat, Double neLng) {
    return Viewport.builder().swLat(swLat).swLng(swLng).neLat(neLat).neLng(neLng).build();
  }
}
