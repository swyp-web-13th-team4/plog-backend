package com.plog.plogbackend.domain.map.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MapPin {
  private Long placeId;
  private Double latitude;
  private Double longitude;
  private Long count;
  private String thumbnailUrl;

  public static MapPin of(Long placeId, Double latitude, Double longitude, Long count, String thumbnailUrl) {
    return MapPin.builder()
        .placeId(placeId)
        .latitude(latitude)
        .longitude(longitude)
        .count(count)
            .thumbnailUrl(thumbnailUrl)
        .build();
  }
}
