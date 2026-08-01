package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewListItemResponse;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPageResponse;
import com.plog.plogbackend.domain.review.enums.PlaceReviewSortType;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import com.plog.plogbackend.domain.review.repository.PlaceReviewQueryRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceReviewPageService {

  private final MemberRepository memberRepository;
  private final PlaceReviewStatisticsService placeReviewStatisticsService;
  private final PlaceReviewQueryRepository placeReviewQueryRepository;

  public PlaceReviewPageResponse getReviewPage(
      UUID memberKey,
      Long placeId,
      Cursorable<String> cursorable,
      boolean imageOnly,
      PlaceReviewSortType sortType) {
    Long currentMemberId = getMemberId(memberKey);
    PlaceReviewSummary summary = placeReviewStatisticsService.getSummary(placeId);

    Slice<PlaceReviewListItemResponse> reviews =
        placeReviewQueryRepository
            .findReviewPageByPlaceId(placeId, cursorable, imageOnly, sortType)
            .map(item -> PlaceReviewListItemResponse.from(item, currentMemberId));

    return PlaceReviewPageResponse.from(summary, reviews);
  }

  private Long getMemberId(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND))
        .getId();
  }
}
