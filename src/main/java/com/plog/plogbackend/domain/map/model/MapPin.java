package com.plog.plogbackend.domain.map.model;

import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import java.time.LocalDate;
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
  private PlaceCategoryCode categoryCode;
  private LocalDate lastStudyDate;
}
