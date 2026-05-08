package com.plog.plogbackend.domain.map.model;

import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceRecord {
  private Long postId;
  private LocalDate studyDate;
  private Integer studyTime;
  private Integer focus;
  private String thumbnailUrl;
  private PlaceCategoryCode categoryCode;
}
