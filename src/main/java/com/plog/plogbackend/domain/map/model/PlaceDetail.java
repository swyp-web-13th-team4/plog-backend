package com.plog.plogbackend.domain.map.model;

import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceDetail {
  private Long placeId;
  private String placeName;
  private String address;
  private Long count;
  private Double avgFocus;
  private Long totalStudyTime;
  private String thumbnailUrl;
  private PlaceCategoryCode placeCategory;
  private PlaceReviewSummary reviewSummary;

  public static PlaceDetail of(
      Long placeId,
      String placeName,
      String address,
      Long count,
      Double avgFocus,
      Long totalStudyTime,
      String thumbnailUrl,
      PlaceCategoryCode placeCategory) {
    return new PlaceDetail(
        placeId,
        placeName,
        address,
        count,
        avgFocus,
        totalStudyTime,
        thumbnailUrl,
        placeCategory,
        PlaceReviewSummary.empty());
  }

  public PlaceDetail withReviewSummary(PlaceReviewSummary reviewSummary) {
    return new PlaceDetail(
        placeId,
        placeName,
        address,
        count,
        avgFocus,
        totalStudyTime,
        thumbnailUrl,
        placeCategory,
        reviewSummary);
  }
}
