package com.plog.plogbackend.domain.post.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "최근 장소 검색 목록 응답")
public record RecentPlaceSearchListResponse(
    @Schema(description = "최근 장소 검색 리스트 (최신순, 최대 10개)") List<RecentPlaceSearchResponse> places,
    @Schema(description = "조회된 항목 개수", example = "10") int totalCount) {

  public static RecentPlaceSearchListResponse of(List<RecentPlaceSearchResponse> places) {
    return new RecentPlaceSearchListResponse(places, places.size());
  }
}
