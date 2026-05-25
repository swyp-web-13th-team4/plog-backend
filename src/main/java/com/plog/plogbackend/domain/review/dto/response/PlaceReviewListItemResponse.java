package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewListItem;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record PlaceReviewListItemResponse(
    Long reviewId,
    String nickname,
    String profileImageUrl,
    Integer rating,
    LocalDateTime createdAt,
    List<PlaceReviewEnvironmentItemResponse> environments,
    String content,
    List<String> imageUrls) {

  public static PlaceReviewListItemResponse from(PlaceReviewListItem item) {
    return new PlaceReviewListItemResponse(
        item.reviewId(),
        item.nickname(),
        item.profileImageUrl(),
        item.rating(),
        item.createdAt(),
        toEnvironmentResponses(item),
        item.content(),
        item.imageUrls());
  }

  private static List<PlaceReviewEnvironmentItemResponse> toEnvironmentResponses(
      PlaceReviewListItem item) {
    return Arrays.stream(ReviewEnvironmentName.values())
        .filter(environment -> item.environments().containsKey(environment))
        .map(
            environment ->
                PlaceReviewEnvironmentItemResponse.from(
                    environment, item.environments().get(environment)))
        .toList();
  }
}
