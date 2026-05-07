package com.plog.plogbackend.domain.map.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceSearchResult {
  private Long placeId;
  private String placeName;
  private String address;
  private Double latitude;
  private Double longitude;
  private LocalDate lastStudyDate;
}
