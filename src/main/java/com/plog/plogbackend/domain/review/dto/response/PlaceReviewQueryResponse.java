package com.plog.plogbackend.domain.review.dto.response;

public record PlaceReviewQueryResponse(
    PlaceReviewRequestPartResponse review, PlaceReviewImageResponse images) {

  public static PlaceReviewQueryResponse of(
      PlaceReviewRequestPartResponse review, PlaceReviewImageResponse images) {
    return new PlaceReviewQueryResponse(review, images);
  }
}
