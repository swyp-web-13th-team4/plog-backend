package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;

public record ReviewEnvironmentResponse(
    Integer spaceSize, Integer noiseLevel, Integer congestionLevel, Integer focusLevel) {

  public static ReviewEnvironmentResponse from(PlaceReview placeReview) {
    return new ReviewEnvironmentResponse(
        placeReview.getEnvironments().get(ReviewEnvironmentName.SPACE_SIZE),
        placeReview.getEnvironments().get(ReviewEnvironmentName.NOISE_LEVEL),
        placeReview.getEnvironments().get(ReviewEnvironmentName.CONGESTION_LEVEL),
        placeReview.getEnvironments().get(ReviewEnvironmentName.FOCUS_LEVEL));
  }
}
