package com.plog.plogbackend.domain.map.controller.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceSummaryResponse {
  private Long placeId;
  private String placeName;
  private String address;
  private long recordCount;
  private Integer totalStudyTime;
  private Double avgFocus;
  private List<String> toTags;
}
