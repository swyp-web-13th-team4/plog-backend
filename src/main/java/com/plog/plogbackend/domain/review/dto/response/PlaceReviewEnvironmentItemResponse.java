package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;

public record PlaceReviewEnvironmentItemResponse(String environmentName, Integer score) {

  public static PlaceReviewEnvironmentItemResponse from(
      ReviewEnvironmentName environment, Integer score) {
    return new PlaceReviewEnvironmentItemResponse(environment.getValue(), score);
  }
}
