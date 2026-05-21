package com.plog.plogbackend.domain.map.model;

import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceSummary {
  private Long placeId;
  private String placeName;
  private String address;
  private Double latitude;
  private Double longitude;
  private Long count;
  private String thumbnailUrl;
  private PlaceCategoryCode placeCategory;
  private Long totalStudyTime;
  private Double avgFocus;

  public static PlaceSummary of(
      Long placeId,
      String placeName,
      String address,
      Double latitude,
      Double longitude,
      Long count,
      String thumbnailUrl,
      PlaceCategoryCode placeCategory,
      Long totalStudyTime,
      Double avgFocus) {
    return PlaceSummary.builder()
        .placeId(placeId)
        .placeName(placeName)
        .address(address)
        .latitude(latitude)
        .longitude(longitude)
        .count(count)
        .thumbnailUrl(thumbnailUrl)
        .placeCategory(placeCategory)
        .totalStudyTime(totalStudyTime)
        .avgFocus(avgFocus)
        .build();
  }
}
