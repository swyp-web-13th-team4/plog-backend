package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.review.dto.response.PlaceReviewListItemResponse;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPageResponse;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import com.plog.plogbackend.domain.review.repository.PlaceReviewQueryRepository;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceReviewPageService {

  private final PlaceReviewStatisticsService placeReviewStatisticsService;
  private final PlaceReviewQueryRepository placeReviewQueryRepository;

  public PlaceReviewPageResponse getReviewPage(Long placeId, Cursorable<String> cursorable) {
    PlaceReviewSummary summary = placeReviewStatisticsService.getSummary(placeId);

    Slice<PlaceReviewListItemResponse> reviews =
        placeReviewQueryRepository
            .findReviewPageByPlaceId(placeId, cursorable)
            .map(PlaceReviewListItemResponse::from);

    return PlaceReviewPageResponse.from(summary, reviews);
  }
}
