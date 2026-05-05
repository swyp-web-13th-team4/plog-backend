package com.plog.plogbackend.domain.map.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MapPin {
  private Long placeId;
  private String placeName;
  private String address;
  private Double latitude;
  private Double longitude;
  private Long count;
  private Integer totalStudyTime;
  private Double avgFocus;
  private String thumbnailUrl;
}
