package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.repository.dto.MapCount;
import lombok.Builder;

@Builder
public record MapCountResponse(Long recordCount, Long bookmarkCount) {
  public static MapCountResponse of(MapCount mapCount) {
    return MapCountResponse.builder()
        .recordCount(mapCount.getRecordCount())
        .bookmarkCount(mapCount.getBookmarkCount())
        .build();
  }
}
