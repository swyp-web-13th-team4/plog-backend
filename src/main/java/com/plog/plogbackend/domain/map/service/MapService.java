package com.plog.plogbackend.domain.map.service;

import com.plog.plogbackend.domain.block.repository.BlockRepository;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.map.repository.MapQueryRepository;
import com.plog.plogbackend.domain.map.repository.dto.MapCount;
import com.plog.plogbackend.domain.map.repository.dto.MapPin;
import com.plog.plogbackend.domain.map.repository.dto.PlaceDetail;
import com.plog.plogbackend.domain.map.repository.dto.PlaceRecord;
import com.plog.plogbackend.domain.map.repository.dto.PlaceSearchResult;
import com.plog.plogbackend.domain.map.repository.dto.PlaceSummary;
import com.plog.plogbackend.domain.map.repository.dto.Viewport;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewRatingSummary;
import com.plog.plogbackend.domain.review.service.PlaceReviewStatisticsService;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.global.common.enums.SortType;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapService {

  private final MapQueryRepository mapQueryRepository;
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final BookMarkRepository bookMarkRepository;
  private final BlockRepository blockRepository;
  private final PlaceReviewStatisticsService placeReviewStatisticsService;

  public MapCount getMapCount(UUID memberKey) {
    Long memberId = getMemberId(memberKey);
    return MapCount.of(
        postRepository.countByMemberId(memberId), bookMarkRepository.countByMemberId(memberId));
  }

  public List<MapPin> findMyRecordPins(UUID memberKey, Viewport viewport) {
    return mapQueryRepository.findRecordPinsByMemberId(getMemberId(memberKey), viewport);
  }

  public List<MapPin> findMyBookmarkPins(UUID memberKey, Viewport viewport) {
    Long memberId = getMemberId(memberKey);
    List<Long> blockedIds = blockRepository.findBlockedMemberIdsByBlockerId(memberId);
    return mapQueryRepository.findBookmarkPinsByMemberId(memberId, viewport, blockedIds);
  }

  public Slice<PlaceSummary> findAllRecordPlaces(
      UUID memberKey, SortType sortType, Cursorable<String> cursorable) {
    return mapQueryRepository.findAllRecordPlace(getMemberId(memberKey), sortType, cursorable);
  }

  public Slice<PlaceSummary> findAllBookmarkPlaces(
      UUID memberKey, SortType sortType, Cursorable<String> cursorable) {
    Long memberId = getMemberId(memberKey);
    List<Long> blockedIds = blockRepository.findBlockedMemberIdsByBlockerId(memberId);
    return mapQueryRepository.findAllBookmarkPlaces(memberId, sortType, cursorable, blockedIds);
  }

  public Slice<PlaceRecord> findPlaceRecords(
      UUID memberKey,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    return mapQueryRepository.findRecordsByPlaceId(
        getMemberId(memberKey), placeId, sortType, tags, cursorable);
  }

  public Slice<PlaceRecord> findPlaceBookmarks(
      UUID memberKey,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    Long memberId = getMemberId(memberKey);
    List<Long> blockedIds = blockRepository.findBlockedMemberIdsByBlockerId(memberId);
    return mapQueryRepository.findBookmarksByPlaceId(
        memberId, placeId, sortType, tags, cursorable, blockedIds);
  }

  public List<PlaceSearchResult> searchRecordedPlaces(UUID memberKey, String keyword) {
    return mapQueryRepository.findRecordedPlacesByKeyword(getMemberId(memberKey), keyword);
  }

  public PlaceDetail findRecordPinDetail(UUID memberKey, Long placeId) {
    PlaceDetail detail =
        mapQueryRepository
            .findRecordPinDetailByPlaceId(getMemberId(memberKey), placeId)
            .orElseThrow(() -> new AppException(ErrorType.PLACE_NOT_FOUND));
    return withReviewSummary(detail);
  }

  public PlaceDetail findBookmarkPinDetail(UUID memberKey, Long placeId) {
    Long memberId = getMemberId(memberKey);
    List<Long> blockedIds = blockRepository.findBlockedMemberIdsByBlockerId(memberId);
    PlaceDetail detail =
        mapQueryRepository
            .findBookmarkPinDetailByPlaceId(memberId, placeId, blockedIds)
            .orElseThrow(() -> new AppException(ErrorType.PLACE_NOT_FOUND));
    return withReviewSummary(detail);
  }

  private PlaceDetail withReviewSummary(PlaceDetail detail) {
    PlaceReviewRatingSummary ratingSummary =
        placeReviewStatisticsService.getRatingSummary(detail.getPlaceId());
    return PlaceDetail.of(
        detail.getPlaceId(),
        detail.getPlaceName(),
        detail.getAddress(),
        detail.getCount(),
        detail.getAvgFocus(),
        detail.getTotalStudyTime(),
        detail.getThumbnailUrl(),
        detail.getPlaceCategory(),
        ratingSummary.reviewCount(),
        ratingSummary.averageRating());
  }

  private Long getMemberId(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND))
        .getId();
  }
}
