package com.plog.plogbackend.domain.map.implement;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;
import static com.plog.plogbackend.domain.post.entity.QPostTag.postTag;
import static com.plog.plogbackend.domain.tag.QTag.tag;

import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.map.model.MapCount;
import com.plog.plogbackend.domain.map.model.MapPin;
import com.plog.plogbackend.domain.map.model.PlaceDetail;
import com.plog.plogbackend.domain.map.model.PlaceRecord;
import com.plog.plogbackend.domain.map.model.PlaceSearchResult;
import com.plog.plogbackend.domain.map.model.PlaceSummary;
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
import com.querydsl.core.types.dsl.NumberExpression;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapManager {
  private final MapQueryRepository mapQueryRepository;
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final BookMarkRepository bookMarkRepository;

  public List<MapPin> getRecordsPins(UUID memberKey, Viewport viewport) {
    Long memberId = getMemberId(memberKey);
    List<Tuple> pins = mapQueryRepository.findRecordPinsByMemberId(memberId, viewport);

    return pins.stream()
        .map(
            t ->
                MapPin.of(
                    t.get(place.id),
                    t.get(place.latitude),
                    t.get(place.longitude),
                    t.get(post.id.count()),
                    t.get(4, String.class)))
        .toList();
  }

  public List<MapPin> getBookmarkPins(UUID memberKey, Viewport viewport) {
    Long memberId = getMemberId(memberKey);
    List<Tuple> pins = mapQueryRepository.findBookmarkPinsByMemberId(memberId, viewport);
    return pins.stream()
        .map(
            t ->
                MapPin.of(
                    t.get(place.id),
                    t.get(place.latitude),
                    t.get(place.longitude),
                    t.get(bookMark.id.count()),
                    t.get(4, String.class)))
        .toList();
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
    Map<Long, List<PlaceTag>> tagsMap = fetchTagsMap(tupleSlice);
    return tupleSlice.map(t -> toPlaceRecord(t, tagsMap));
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
    Map<Long, List<PlaceTag>> tagsMap = fetchTagsMap(tupleSlice);
    return tupleSlice.map(t -> toPlaceRecord(t, tagsMap));
  }

  public Slice<PlaceSummary> getAllRecordPlaces(
      UUID memberKey, SortType sortType, Cursorable<String> cursorable) {
    Long memberId = getMemberId(memberKey);
    Slice<Tuple> tupleSlice =
        mapQueryRepository.findAllRecordPlaces(memberId, sortType, cursorable);

    List<Long> placeIds = tupleSlice.getContent().stream().map(t -> t.get(place.id)).toList();
    Map<Long, PlaceCategoryCode> categoryMap =
        toCategoryModeMap(
            mapQueryRepository.findRecordCategoryCountsByPlaceIds(memberId, placeIds));

    return tupleSlice.map(t -> toPlaceSummary(t, categoryMap, post.id.count()));
  }

  public Slice<PlaceSummary> getAllBookmarkPlaces(
      UUID memberKey, SortType sortType, Cursorable<String> cursorable) {
    Long memberId = getMemberId(memberKey);
    Slice<Tuple> tupleSlice =
        mapQueryRepository.findAllBookmarkPlaces(memberId, sortType, cursorable);

    List<Long> placeIds = tupleSlice.getContent().stream().map(t -> t.get(place.id)).toList();
    Map<Long, PlaceCategoryCode> categoryMap =
        toCategoryModeMap(
            mapQueryRepository.findBookmarkCategoryCountsByPlaceIds(memberId, placeIds));

    return tupleSlice.map(t -> toPlaceSummary(t, categoryMap, bookMark.id.count()));
  }

  public PlaceDetail getRecordPinDetail(UUID memberKey, Long placeId) {
    Long memberId = getMemberId(memberKey);
    Tuple t =
        mapQueryRepository
            .findRecordPinDetailByPlaceId(memberId, placeId)
            .orElseThrow(() -> new AppException(ErrorType.PLACE_NOT_FOUND));
    PlaceCategoryCode category =
        toCategoryModeMap(
                mapQueryRepository.findRecordCategoryCountsByPlaceIds(memberId, List.of(placeId)))
            .get(placeId);
    return PlaceDetail.of(
        t.get(place.id),
        t.get(place.name),
        t.get(place.address),
        t.get(post.id.count()),
        t.get(post.focus.avg()),
        t.get(post.studyTime.sum()).longValue(),
        t.get(6, String.class),
        category);
  }

  public PlaceDetail getBookmarkPinDetail(UUID memberKey, Long placeId) {
    Long memberId = getMemberId(memberKey);
    Tuple t =
        mapQueryRepository
            .findBookmarkPinDetailByPlaceId(memberId, placeId)
            .orElseThrow(() -> new AppException(ErrorType.PLACE_NOT_FOUND));
    PlaceCategoryCode category =
        toCategoryModeMap(
                mapQueryRepository.findBookmarkCategoryCountsByPlaceIds(memberId, List.of(placeId)))
            .get(placeId);
    return PlaceDetail.of(
        t.get(place.id),
        t.get(place.name),
        t.get(place.address),
        t.get(bookMark.id.count()),
        t.get(post.focus.avg()),
        t.get(post.studyTime.sum()).longValue(),
        t.get(6, String.class),
        category);
  }

  private PlaceSummary toPlaceSummary(
      Tuple t, Map<Long, PlaceCategoryCode> categoryMap, NumberExpression<Long> countExpr) {
    Long placeId = t.get(place.id);
    Integer studyTimeSum = t.get(8, Integer.class);
    return PlaceSummary.of(
        placeId,
        t.get(place.name),
        t.get(place.address),
        t.get(place.latitude),
        t.get(place.longitude),
        t.get(countExpr),
        t.get(6, String.class),
        categoryMap.get(placeId),
        t.get(post.studyDate.max()),
        studyTimeSum != null ? studyTimeSum.longValue() : 0L,
        t.get(9, Double.class));
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

  private PlaceRecord toPlaceRecord(Tuple tuple, Map<Long, List<PlaceTag>> tagsMap) {
    var p = tuple.get(post);
    return new PlaceRecord(
        p.getId(),
        p.getPlace().getName(),
        p.getTitle(),
        p.getStudyDate(),
        p.getStudyTime(),
        p.getFocus(),
        p.getContents(),
        tuple.get(1, String.class),
        tuple.get(post.placeCategory.categoryName),
        tagsMap.getOrDefault(p.getId(), List.of()));
  }

  private Map<Long, List<PlaceTag>> fetchTagsMap(Slice<Tuple> tupleSlice) {
    List<Long> postIds = tupleSlice.getContent().stream().map(t -> t.get(post).getId()).toList();
    return mapQueryRepository.findPlaceTagsByPostIds(postIds).stream()
        .collect(
            Collectors.groupingBy(
                t -> t.get(postTag.post.id),
                Collectors.mapping(t -> t.get(tag.placeTag), Collectors.toList())));
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
