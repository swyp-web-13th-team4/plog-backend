package com.plog.plogbackend.domain.post.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최근 장소 저장 응답")
public record RecentPlaceSaveResponse(
    @Schema(description = "현재 보관된 최근 장소 총 개수 (최대 10)", example = "7") int totalCount) {

  public static RecentPlaceSaveResponse of(int totalCount) {
    return new RecentPlaceSaveResponse(totalCount);
  }
}
