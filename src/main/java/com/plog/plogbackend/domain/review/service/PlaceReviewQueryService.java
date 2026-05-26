package com.plog.plogbackend.domain.review.service;

import static com.plog.plogbackend.global.common.Enum.EntityStatus.ACTIVE;

import com.plog.plogbackend.domain.review.dto.response.PlaceReviewImageResponse;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewQueryResponse;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewRequestPartResponse;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import com.plog.plogbackend.domain.review.repository.PlaceReviewImageRepository;
import com.plog.plogbackend.domain.review.repository.PlaceReviewRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceReviewQueryService {

  private final PlaceReviewRepository placeReviewRepository;
  private final PlaceReviewImageRepository placeReviewImageRepository;

  public PlaceReviewQueryResponse getPlaceReview(Long reviewId, UUID memberKey) {

    PlaceReview placeReview = findPlaceReview(reviewId);

    if (!placeReview.getMember().getMemberKey().equals(memberKey)) {
      throw new AppException(ErrorType.POST_FORBIDDEN);
    }

    List<PlaceReviewImage> images =
        placeReviewImageRepository.findAllByPlaceReviewIdOrderByIdAsc(reviewId);

    return PlaceReviewQueryResponse.of(
        PlaceReviewRequestPartResponse.from(placeReview), PlaceReviewImageResponse.from(images));
  }

  private PlaceReview findPlaceReview(Long placeId) {
    return placeReviewRepository
        .findByIdAndStatus(placeId, ACTIVE)
        .orElseThrow(() -> new AppException(ErrorType.PLACE_REVIEW_NOT_FOUND));
  }
}
