package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.post.controller.dto.response.TimePickerResponse;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import java.time.LocalDate;
import java.util.Comparator;

public record PlaceReviewRequestPartResponse(
    String placeProfileUrl,
    String placeName,
    Integer rating,
    LocalDate studyDate,
    TimePickerResponse startedAt,
    TimePickerResponse endedAt,
    String content,
    ReviewEnvironmentResponse environments) {

  public static PlaceReviewRequestPartResponse from(PlaceReview placeReview) {
    return new PlaceReviewRequestPartResponse(
        firstPostImageUrl(placeReview),
        placeReview.getPost().getPlace().getName(),
        placeReview.getRating(),
        placeReview.getPost().getStudyDate(),
        TimePickerResponse.from(placeReview.getPost().getStartedAt()),
        TimePickerResponse.from(placeReview.getPost().getEndedAt()),
        placeReview.getContent(),
        ReviewEnvironmentResponse.from(placeReview));
  }

  private static String firstPostImageUrl(PlaceReview placeReview) {
    return placeReview.getPost().getImages().stream()
        .min(Comparator.comparing(PostImage::getId, Comparator.nullsLast(Long::compareTo)))
        .map(PostImage::getImageUrl)
        .orElse(null);
  }
}
