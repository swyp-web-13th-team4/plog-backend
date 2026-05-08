package com.plog.plogbackend.domain.map.implement;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;

import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.map.model.MapCount;
import com.plog.plogbackend.domain.map.model.MapPin;
import com.plog.plogbackend.domain.map.model.PlaceRecord;
import com.plog.plogbackend.domain.map.model.PlaceSearchResult;
import com.plog.plogbackend.domain.map.model.SortType;
import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.domain.map.repository.MapQueryRepository;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.querydsl.core.Tuple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapManager {
  private final MapQueryRepository mapQueryRepository;
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final BookMarkRepository bookMarkRepository;

  public Slice<MapPin> getRecordsPins(
      UUID memberKey, Viewport viewport, SortType sortType, Cursorable<String> cursorable) {
    Long memberId = getMemberId(memberKey);
    Slice<Tuple> tupleSlice =
        mapQueryRepository.findRecordPinsByMemberId(memberId, viewport, sortType, cursorable);

    List<Long> placeIds = tupleSlice.getContent().stream().map(t -> t.get(place.id)).toList();
    Map<Long, PlaceCategoryCode> categoryMap =
        placeIds.isEmpty()
            ? Map.of()
            : toCategoryModeMap(
                mapQueryRepository.findRecordCategoryCountsByPlaceIds(memberId, placeIds));

    return tupleSlice.map(
        tuple ->
            new MapPin(
                tuple.get(place.id),
                tuple.get(place.name),
                tuple.get(place.address),
                tuple.get(place.latitude),
                tuple.get(place.longitude),
                tuple.get(post.id.count()),
                tuple.get(post.studyTime.sum()),
                tuple.get(post.focus.avg()),
                tuple.get(8, String.class),
                categoryMap.get(tuple.get(place.id)),
                tuple.get(post.studyDate.max())));
  }

  public Slice<MapPin> getBookmarkPins(
      UUID memberKey, Viewport viewport, SortType sortType, Cursorable<String> cursorable) {
    Long memberId = getMemberId(memberKey);
    Slice<Tuple> tupleSlice =
        mapQueryRepository.findBookmarkPinsByMemberId(memberId, viewport, sortType, cursorable);

    List<Long> placeIds = tupleSlice.getContent().stream().map(t -> t.get(place.id)).toList();
    Map<Long, PlaceCategoryCode> categoryMap =
        placeIds.isEmpty()
            ? Map.of()
            : toCategoryModeMap(
                mapQueryRepository.findBookmarkCategoryCountsByPlaceIds(memberId, placeIds));

    return tupleSlice.map(
        tuple ->
            new MapPin(
                tuple.get(place.id),
                tuple.get(place.name),
                tuple.get(place.address),
                tuple.get(place.latitude),
                tuple.get(place.longitude),
                tuple.get(bookMark.id.count()),
                tuple.get(post.studyTime.sum()),
                tuple.get(post.focus.avg()),
                tuple.get(8, String.class),
                categoryMap.get(tuple.get(place.id)),
                tuple.get(post.studyDate.max())));
  }

  public Slice<PlaceRecord> getPlaceRecords(
      UUID memberKey,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    Slice<Tuple> tupleSlice =
        mapQueryRepository.findRecordsByPlaceId(
            getMemberId(memberKey), placeId, sortType, tags, cursorable);
    return tupleSlice.map(this::toPlaceRecord);
  }

  public Slice<PlaceRecord> getPlaceBookmarks(
      UUID memberKey,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    Slice<Tuple> tupleSlice =
        mapQueryRepository.findBookmarksByPlaceId(
            getMemberId(memberKey), placeId, sortType, tags, cursorable);
    return tupleSlice.map(this::toPlaceRecord);
  }

  public List<PlaceSearchResult> searchRecordedPlaces(UUID memberKey, String keyword) {
    Long memberId = getMemberId(memberKey);
    return mapQueryRepository.findRecordedPlacesByKeyword(memberId, keyword).stream()
        .map(
            t ->
                new PlaceSearchResult(
                    t.get(place.id),
                    t.get(place.name),
                    t.get(place.address),
                    t.get(place.latitude),
                    t.get(place.longitude),
                    t.get(post.studyDate.max())))
        .toList();
  }

  private PlaceRecord toPlaceRecord(Tuple tuple) {
    var p = tuple.get(post);
    return new PlaceRecord(
        p.getId(),
        p.getStudyDate(),
        p.getStudyTime(),
        p.getFocus(),
        tuple.get(1, String.class),
        tuple.get(post.placeCategory.categoryName));
  }

  private Map<Long, PlaceCategoryCode> toCategoryModeMap(List<Tuple> tuples) {
    Map<Long, Long> maxCounts = new HashMap<>();
    Map<Long, PlaceCategoryCode> result = new HashMap<>();
    for (Tuple t : tuples) {
      Long placeId = t.get(0, Long.class);
      PlaceCategoryCode cat = t.get(1, PlaceCategoryCode.class);
      Long cnt = t.get(2, Long.class);
      if (cnt > maxCounts.getOrDefault(placeId, -1L)) {
        maxCounts.put(placeId, cnt);
        result.put(placeId, cat);
      }
    }
    return result;
  }

  public MapCount getMapCount(UUID memberKey) {
    Long memberId = getMemberId(memberKey);
    long recordCnt = postRepository.countByMemberId(memberId);
    long bookmarkCnt = bookMarkRepository.countByMemberId(memberId);

    return MapCount.of(recordCnt, bookmarkCnt);
  }

  private Long getMemberId(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND))
        .getId();
  }
}
