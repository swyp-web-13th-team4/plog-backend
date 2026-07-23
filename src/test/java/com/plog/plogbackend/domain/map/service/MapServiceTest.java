package com.plog.plogbackend.domain.map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.map.repository.MapQueryRepository;
import com.plog.plogbackend.domain.map.repository.dto.PlaceDetail;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewRatingSummary;
import com.plog.plogbackend.domain.review.service.PlaceReviewStatisticsService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

  @Mock private MapQueryRepository mapQueryRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private PostRepository postRepository;
  @Mock private BookMarkRepository bookMarkRepository;
  @Mock private com.plog.plogbackend.domain.block.repository.BlockRepository blockRepository;
  @Mock private PlaceReviewStatisticsService placeReviewStatisticsService;
  @Mock private Member member;
  @InjectMocks private MapService mapService;

  @Test
  @DisplayName("기록 핀 상세 조회 시 장소 상세만 반환한다")
  void findRecordPinDetail_returnsPlaceDetail() {
    UUID memberKey = UUID.randomUUID();
    Long memberId = 1L;
    Long placeId = 1L;
    PlaceDetail detail = placeDetail(placeId);
    given(member.getId()).willReturn(memberId);
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(mapQueryRepository.findRecordPinDetailByPlaceId(memberId, placeId))
        .willReturn(Optional.of(detail));
    given(placeReviewStatisticsService.getRatingSummary(placeId))
        .willReturn(new PlaceReviewRatingSummary(10L, 4.2));

    PlaceDetail result = mapService.findRecordPinDetail(memberKey, placeId);

    assertThat(result.getReviewCount()).isEqualTo(10L);
    assertThat(result.getAverageRating()).isEqualTo(4.2);
  }

  @Test
  @DisplayName("북마크 핀 상세 조회 시 장소 상세만 반환한다")
  void findBookmarkPinDetail_returnsPlaceDetail() {
    UUID memberKey = UUID.randomUUID();
    Long memberId = 1L;
    Long placeId = 1L;
    PlaceDetail detail = placeDetail(placeId);
    given(member.getId()).willReturn(memberId);
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(blockRepository.findBlockedMemberIdsByBlockerId(memberId))
        .willReturn(java.util.List.of());
    given(
            mapQueryRepository.findBookmarkPinDetailByPlaceId(
                org.mockito.ArgumentMatchers.eq(memberId),
                org.mockito.ArgumentMatchers.eq(placeId),
                org.mockito.ArgumentMatchers.any()))
        .willReturn(Optional.of(detail));
    given(placeReviewStatisticsService.getRatingSummary(placeId))
        .willReturn(new PlaceReviewRatingSummary(8L, 4.5));

    PlaceDetail result = mapService.findBookmarkPinDetail(memberKey, placeId);

    assertThat(result.getReviewCount()).isEqualTo(8L);
    assertThat(result.getAverageRating()).isEqualTo(4.5);
  }

  private PlaceDetail placeDetail(Long placeId) {
    return PlaceDetail.of(
        placeId,
        "스타벅스 광화문점",
        "서울시 종로구 세종대로 172",
        24L,
        4.5,
        1440L,
        "https://storage/place.jpg",
        PlaceCategoryCode.CAFE,
        0L,
        0.0);
  }
}
