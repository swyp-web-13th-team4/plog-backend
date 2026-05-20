package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record PlaceReviewResponse(
    Long reviewId,
    Long postId,
    Long placeId,
    String placeName,
    Integer rating,
    LocalDate visitedDate,
    LocalTime visitStartTime,
    LocalTime visitEndTime,
    Map<ReviewEnvironmentName, Integer> environments,
    String content,
    List<String> imageUrls) {

  public static PlaceReviewResponse from(PlaceReview placeReview, List<ImageUrlResponse> images) {
    return PlaceReviewResponse.builder()
        .reviewId(placeReview.getId())
        .postId(placeReview.getPost().getId())
        .placeId(placeReview.getPost().getPlace().getId())
        .placeName(placeReview.getPost().getPlace().getName())
        .rating(placeReview.getRating())
        .visitedDate(placeReview.getPost().getStudyDate())
        .visitStartTime(placeReview.getPost().getStartedAt().toLocalTime())
        .visitEndTime(placeReview.getPost().getEndedAt().toLocalTime())
        .environments(placeReview.getEnvironments())
        .content(placeReview.getContent())
        .imageUrls(toImageUrls(images))
        .build();
  }

  private static List<String> toImageUrls(List<ImageUrlResponse> images) {
    if (images == null || images.isEmpty()) {
      return List.of();
    }

    return images.stream().map(ImageUrlResponse::imageUrl).toList();
  }
}
