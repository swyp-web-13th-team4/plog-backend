package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;

public record PlaceReviewEnvironmentItemResponse(
    String environmentName, String title, String iconName, Integer score, String label) {

  public static PlaceReviewEnvironmentItemResponse from(
      ReviewEnvironmentName environment, Integer score) {
    return new PlaceReviewEnvironmentItemResponse(
        environment.getValue(),
        environment.getTitle(),
        environment.getIconName(),
        score,
        environment.getLabel(score));
  }
}
