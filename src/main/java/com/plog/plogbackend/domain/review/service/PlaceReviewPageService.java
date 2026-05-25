package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewListItemResponse;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPageResponse;
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
  private final PostRepository postRepository;
  private final BookMarkRepository bookMarkRepository;
  private final PlaceReviewStatisticsService placeReviewStatisticsService;
  private final PlaceReviewQueryRepository placeReviewQueryRepository;

  public PlaceReviewPageResponse getRecordReviewPage(
      UUID memberKey, Long placeId, Cursorable<String> cursorable) {
    Long memberId = getMemberId(memberKey);
    validateRecordedPlace(memberId, placeId);

    return getReviewPage(placeId, cursorable);
  }

  public PlaceReviewPageResponse getBookmarkReviewPage(
      UUID memberKey, Long placeId, Cursorable<String> cursorable) {
    Long memberId = getMemberId(memberKey);
    validateBookmarkedPlace(memberId, placeId);

    return getReviewPage(placeId, cursorable);
  }

  public PlaceReviewPageResponse getReviewPage(Long placeId, Cursorable<String> cursorable) {
    PlaceReviewSummary summary = placeReviewStatisticsService.getSummary(placeId);

    Slice<PlaceReviewListItemResponse> reviews =
        placeReviewQueryRepository
            .findReviewPageByPlaceId(placeId, cursorable)
            .map(PlaceReviewListItemResponse::from);

    return PlaceReviewPageResponse.from(summary, reviews);
  }

  private Long getMemberId(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND))
        .getId();
  }

  private void validateRecordedPlace(Long memberId, Long placeId) {
    if (!postRepository.existsByMemberIdAndPlaceId(memberId, placeId)) {
      throw new AppException(ErrorType.PLACE_NOT_FOUND);
    }
  }

  private void validateBookmarkedPlace(Long memberId, Long placeId) {
    if (!bookMarkRepository.existsByMemberIdAndPlaceId(memberId, placeId)) {
      throw new AppException(ErrorType.PLACE_NOT_FOUND);
    }
  }
}
