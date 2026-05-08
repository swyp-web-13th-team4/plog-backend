package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.post.entity.RecentPlaceSearch;
import java.time.LocalDateTime;

public record RecentPlaceSearchResponse(
    Long id,
    String placeName,
    String address,
    Double latitude,
    Double longitude,
    LocalDateTime searchedAt) {

  public static RecentPlaceSearchResponse from(RecentPlaceSearch recentPlaceSearch) {
    return new RecentPlaceSearchResponse(
        recentPlaceSearch.getId(),
        recentPlaceSearch.getPlaceName(),
        recentPlaceSearch.getAddress(),
        recentPlaceSearch.getLatitude(),
        recentPlaceSearch.getLongitude(),
        recentPlaceSearch.getSearchedAt());
  }
}
