package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.repository.dto.PlaceDetail;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record PlaceDetailResponse(
    Long placeId,
    String placeName,
    String address,
    Long count,
    Double avgFocus,
    Long totalStudyTime,
    String thumbnailUrl,
    PlaceCategoryCode placeCategory,
    @Schema(description = "해당 장소에 작성된 전체 리뷰 수입니다.", example = "12", minimum = "0") Long reviewCount,
    @Schema(
            description = "해당 장소 전체 리뷰의 평균 별점입니다. 리뷰가 없으면 0.0을 반환합니다.",
            example = "4.3",
            minimum = "0",
            maximum = "5")
        Double averageRating) {

  public static PlaceDetailResponse from(PlaceDetail detail) {
    return new PlaceDetailResponse(
        detail.getPlaceId(),
        detail.getPlaceName(),
        detail.getAddress(),
        detail.getCount(),
        detail.getAvgFocus(),
        detail.getTotalStudyTime(),
        detail.getThumbnailUrl(),
        detail.getPlaceCategory(),
        detail.getReviewCount(),
        detail.getAverageRating());
  }
}
