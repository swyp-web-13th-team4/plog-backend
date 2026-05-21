package com.plog.plogbackend.domain.review.controller.api;

import com.plog.plogbackend.domain.review.dto.request.PlaceReviewCreateRequest;
import com.plog.plogbackend.domain.review.dto.request.PlaceReviewUpdateRequest;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewCreateCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewDeleteCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewUpdateCommand;
import java.util.UUID;

public class PlaceReviewMapper {

  public static PlaceReviewCreateCommand from(
      Long postId, PlaceReviewCreateRequest request, UUID memberKey) {
    return new PlaceReviewCreateCommand(
        postId, memberKey, request.rating(), request.content(), request.environments().toMap());
  }

  public static PlaceReviewUpdateCommand from(
      Long reviewId, PlaceReviewUpdateRequest request, UUID memberKey) {
    return new PlaceReviewUpdateCommand(
        reviewId,
        memberKey,
        request.rating(),
        request.content(),
        request.environments().toMap(),
        request.keepImageIds());
  }

  public static PlaceReviewDeleteCommand from(Long reviewId, UUID memberKey) {
    return new PlaceReviewDeleteCommand(reviewId, memberKey);
  }
}
