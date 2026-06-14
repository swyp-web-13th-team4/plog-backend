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

    PlaceDetail result = mapService.findRecordPinDetail(memberKey, placeId);

    assertThat(result).isSameAs(detail);
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
    given(mapQueryRepository.findBookmarkPinDetailByPlaceId(memberId, placeId, org.mockito.ArgumentMatchers.any()))
        .willReturn(Optional.of(detail));

    PlaceDetail result = mapService.findBookmarkPinDetail(memberKey, placeId);

    assertThat(result).isSameAs(detail);
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
        PlaceCategoryCode.CAFE);
  }
}
