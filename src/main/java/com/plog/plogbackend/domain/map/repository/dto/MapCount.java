package com.plog.plogbackend.domain.map.repository.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MapCount {
  private Long recordCount;
  private Long bookmarkCount;

  public static MapCount of(Long recordCount, Long bookmarkCount) {
    return MapCount.builder().recordCount(recordCount).bookmarkCount(bookmarkCount).build();
  }
}
