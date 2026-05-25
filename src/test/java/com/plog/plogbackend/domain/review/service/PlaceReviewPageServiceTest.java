package com.plog.plogbackend.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPageResponse;
import com.plog.plogbackend.domain.review.enums.PlaceReviewSortType;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import com.plog.plogbackend.domain.review.repository.PlaceReviewQueryRepository;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewListItem;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceReviewPageServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private PostRepository postRepository;
  @Mock private BookMarkRepository bookMarkRepository;
  @Mock private PlaceReviewStatisticsService placeReviewStatisticsService;
  @Mock private PlaceReviewQueryRepository placeReviewQueryRepository;
  @Mock private Member member;
  @InjectMocks private PlaceReviewPageService placeReviewPageService;

  @Test
  @DisplayName("내 기록 장소이면 리뷰 요약과 리뷰 목록 페이지를 반환한다")
  void getRecordReviewPage_whenRecordedPlace_returnsSummaryAndReviewPage() {
    UUID memberKey = UUID.randomUUID();
    Long memberId = 10L;
    Long placeId = 1L;
    Cursorable<String> cursorable = new Cursorable<>(null, 1);
    PlaceReviewSortType sortType = PlaceReviewSortType.LATEST;
    PlaceReviewSummary summary = new PlaceReviewSummary(2L, 4.5, List.of());
    PlaceReviewListItem item =
        new PlaceReviewListItem(
            10L,
            memberId,
            "남나밍",
            "https://profile/namnaming.jpg",
            5,
            LocalDateTime.of(2026, 5, 20, 16, 5),
            environments(),
            "장소 리뷰 내용",
            List.of("https://storage/review.jpg"));
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(member.getId()).willReturn(memberId);
    given(postRepository.existsByMemberIdAndPlaceId(memberId, placeId)).willReturn(true);
    given(placeReviewStatisticsService.getSummary(placeId)).willReturn(summary);
    given(placeReviewQueryRepository.findReviewPageByPlaceId(placeId, cursorable, true, sortType))
        .willReturn(Slice.of(new ArrayList<>(List.of(item)), cursorable, review -> "10"));

    PlaceReviewPageResponse response =
        placeReviewPageService.getRecordReviewPage(memberKey, placeId, cursorable, true, sortType);

    assertThat(response.summary().reviewCount()).isEqualTo(2L);
    assertThat(response.reviews().content()).hasSize(1);
    assertThat(response.reviews().content().get(0).nickname()).isEqualTo("남나밍");
    assertThat(response.reviews().content().get(0).profileImageUrl())
        .isEqualTo("https://profile/namnaming.jpg");
    assertThat(response.reviews().content().get(0).isAuthor()).isTrue();
    assertThat(response.reviews().content().get(0).environments()).hasSize(4);
    assertThat(response.reviews().content().get(0).imageUrls())
        .containsExactly("https://storage/review.jpg");
    assertThat(response.reviews().hasNext()).isFalse();
  }

  @Test
  @DisplayName("내 기록 장소가 아니면 리뷰 페이지 조회를 거부한다")
  void getRecordReviewPage_whenNotRecordedPlace_throwsPlaceNotFound() {
    UUID memberKey = UUID.randomUUID();
    Long memberId = 10L;
    Long placeId = 1L;
    Cursorable<String> cursorable = new Cursorable<>(null, 10);
    PlaceReviewSortType sortType = PlaceReviewSortType.LATEST;
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(member.getId()).willReturn(memberId);
    given(postRepository.existsByMemberIdAndPlaceId(memberId, placeId)).willReturn(false);

    assertThatThrownBy(
            () ->
                placeReviewPageService.getRecordReviewPage(
                    memberKey, placeId, cursorable, false, sortType))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.PLACE_NOT_FOUND);
  }

  @Test
  @DisplayName("회원을 찾을 수 없으면 내 기록 장소 리뷰 페이지 조회를 거부한다")
  void getRecordReviewPage_whenMemberNotFound_throwsMemberNotFound() {
    UUID memberKey = UUID.randomUUID();
    Long placeId = 1L;
    Cursorable<String> cursorable = new Cursorable<>(null, 10);
    PlaceReviewSortType sortType = PlaceReviewSortType.LATEST;
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                placeReviewPageService.getRecordReviewPage(
                    memberKey, placeId, cursorable, false, sortType))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("북마크한 장소이면 리뷰 요약과 리뷰 목록 페이지를 반환한다")
  void getBookmarkReviewPage_whenBookmarkedPlace_returnsSummaryAndReviewPage() {
    UUID memberKey = UUID.randomUUID();
    Long memberId = 10L;
    Long placeId = 1L;
    Cursorable<String> cursorable = new Cursorable<>(null, 1);
    PlaceReviewSortType sortType = PlaceReviewSortType.LATEST;
    PlaceReviewSummary summary = new PlaceReviewSummary(0L, 0.0, List.of());
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(member.getId()).willReturn(memberId);
    given(bookMarkRepository.existsByMemberIdAndPlaceId(memberId, placeId)).willReturn(true);
    given(placeReviewStatisticsService.getSummary(placeId)).willReturn(summary);
    given(placeReviewQueryRepository.findReviewPageByPlaceId(placeId, cursorable, true, sortType))
        .willReturn(Slice.of(new ArrayList<>(), cursorable, review -> "unused"));

    PlaceReviewPageResponse response =
        placeReviewPageService.getBookmarkReviewPage(
            memberKey, placeId, cursorable, true, sortType);

    assertThat(response.reviews().content()).isEmpty();
    assertThat(response.reviews().hasNext()).isFalse();
    verify(placeReviewQueryRepository).findReviewPageByPlaceId(placeId, cursorable, true, sortType);
  }

  @Test
  @DisplayName("북마크한 장소가 아니면 리뷰 페이지 조회를 거부한다")
  void getBookmarkReviewPage_whenNotBookmarkedPlace_throwsPlaceNotFound() {
    UUID memberKey = UUID.randomUUID();
    Long memberId = 10L;
    Long placeId = 1L;
    Cursorable<String> cursorable = new Cursorable<>(null, 10);
    PlaceReviewSortType sortType = PlaceReviewSortType.LATEST;
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(member.getId()).willReturn(memberId);
    given(bookMarkRepository.existsByMemberIdAndPlaceId(memberId, placeId)).willReturn(false);

    assertThatThrownBy(
            () ->
                placeReviewPageService.getBookmarkReviewPage(
                    memberKey, placeId, cursorable, false, sortType))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.PLACE_NOT_FOUND);
  }

  @Test
  @DisplayName("회원을 찾을 수 없으면 북마크 장소 리뷰 페이지 조회를 거부한다")
  void getBookmarkReviewPage_whenMemberNotFound_throwsMemberNotFound() {
    UUID memberKey = UUID.randomUUID();
    Long placeId = 1L;
    Cursorable<String> cursorable = new Cursorable<>(null, 10);
    PlaceReviewSortType sortType = PlaceReviewSortType.LATEST;
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                placeReviewPageService.getBookmarkReviewPage(
                    memberKey, placeId, cursorable, false, sortType))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.MEMBER_NOT_FOUND);
  }

  private Map<ReviewEnvironmentName, Integer> environments() {
    Map<ReviewEnvironmentName, Integer> environments = new EnumMap<>(ReviewEnvironmentName.class);
    environments.put(ReviewEnvironmentName.SPACE_SIZE, 5);
    environments.put(ReviewEnvironmentName.NOISE_LEVEL, 4);
    environments.put(ReviewEnvironmentName.CONGESTION_LEVEL, 3);
    environments.put(ReviewEnvironmentName.FOCUS_LEVEL, 2);
    return environments;
  }
}
