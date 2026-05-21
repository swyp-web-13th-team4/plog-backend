package com.plog.plogbackend.domain.map.repository;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;
import static com.plog.plogbackend.domain.post.entity.QPostImage.postImage;
import static com.plog.plogbackend.domain.post.entity.QPostTag.postTag;
import static com.plog.plogbackend.domain.tag.QTag.tag;

import com.plog.plogbackend.domain.bookmark.entity.QBookMark;
import com.plog.plogbackend.domain.map.repository.dto.MapPin;
import com.plog.plogbackend.domain.map.repository.dto.PlaceDetail;
import com.plog.plogbackend.domain.map.repository.dto.PlaceRecord;
import com.plog.plogbackend.domain.map.repository.dto.PlaceSearchResult;
import com.plog.plogbackend.domain.map.repository.dto.PlaceSummary;
import com.plog.plogbackend.domain.map.repository.dto.Viewport;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.QPost;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.global.common.enums.SortType;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MapQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<MapPin> findRecordPinsByMemberId(Long memberId, Viewport viewport) {
    Expression<String> thumbnail = recordThumbnail(memberId);
    return queryFactory
        .select(place.id, place.latitude, place.longitude, post.id.count(), thumbnail)
        .from(post)
        .join(post.place, place)
        .where(
            post.member.id.eq(memberId),
            place.latitude.between(viewport.getSwLat(), viewport.getNeLat()),
            place.longitude.between(viewport.getSwLng(), viewport.getNeLng()))
        .groupBy(place.id)
        .fetch()
        .stream()
        .map(
            tuple ->
                MapPin.of(
                    tuple.get(place.id),
                    tuple.get(place.latitude),
                    tuple.get(place.longitude),
                    tuple.get(post.id.count()),
                    tuple.get(thumbnail)))
        .toList();
  }

  public List<MapPin> findBookmarkPinsByMemberId(Long memberId, Viewport viewport) {
    Expression<String> thumbnail = bookmarkThumbnail(memberId);
    return queryFactory
        .select(place.id, place.latitude, place.longitude, bookMark.id.count(), thumbnail)
        .from(bookMark)
        .join(bookMark.post, post)
        .join(post.place, place)
        .where(
            bookMark.member.id.eq(memberId),
            place.latitude.between(viewport.getSwLat(), viewport.getNeLat()),
            place.longitude.between(viewport.getSwLng(), viewport.getNeLng()))
        .groupBy(place.id)
        .fetch()
        .stream()
        .map(
            tuple ->
                MapPin.of(
                    tuple.get(place.id),
                    tuple.get(place.latitude),
                    tuple.get(place.longitude),
                    tuple.get(bookMark.id.count()),
                    tuple.get(thumbnail)))
        .toList();
  }

  public Slice<PlaceRecord> findRecordsByPlaceId(
      Long memberId,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    StringExpression thumbnail = postImage.imageUrl.min();
    List<Tuple> tuples =
        queryFactory
            .select(post, thumbnail, place.name)
            .from(post)
            .join(post.place, place)
            .leftJoin(postImage)
            .on(postImage.post.id.eq(post.id))
            .where(
                post.member.id.eq(memberId),
                post.place.id.eq(placeId),
                tagFilterCondition(tags),
                postCursorCondition(sortType, cursorable.getCursor()))
            .groupBy(post.id)
            .orderBy(postOrderBy(sortType))
            .limit(cursorable.getLimit() + 1)
            .fetch();
    Slice<Tuple> tupleSlice = Slice.of(tuples, cursorable, postCursorExtractor(sortType));
    Map<Long, List<PlaceTag>> tagMap =
        toTags(tupleSlice.getContent().stream().map(tuple -> tuple.get(post).getId()).toList());
    return tupleSlice.map(
        tuple -> {
          Post p = tuple.get(post);
          return PlaceRecord.of(
              p,
              tuple.get(place.name),
              tuple.get(thumbnail),
              tagMap.getOrDefault(p.getId(), List.of()));
        });
  }

  public Slice<PlaceRecord> findBookmarksByPlaceId(
      Long memberId,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    StringExpression thumbnail = postImage.imageUrl.min();
    List<Tuple> tuples =
        queryFactory
            .select(post, thumbnail, place.name)
            .from(bookMark)
            .join(bookMark.post, post)
            .join(post.place, place)
            .leftJoin(postImage)
            .on(postImage.post.id.eq(post.id))
            .where(
                bookMark.member.id.eq(memberId),
                post.place.id.eq(placeId),
                tagFilterCondition(tags),
                postCursorCondition(sortType, cursorable.getCursor()))
            .groupBy(post.id)
            .orderBy(postOrderBy(sortType))
            .limit(cursorable.getLimit() + 1)
            .fetch();
    Slice<Tuple> tupleSlice = Slice.of(tuples, cursorable, postCursorExtractor(sortType));
    Map<Long, List<PlaceTag>> tagMap =
        toTags(tupleSlice.getContent().stream().map(tuple -> tuple.get(post).getId()).toList());
    return tupleSlice.map(
        tuple -> {
          Post p = tuple.get(post);
          return PlaceRecord.of(
              p,
              tuple.get(place.name),
              tuple.get(thumbnail),
              tagMap.getOrDefault(p.getId(), List.of()));
        });
  }

  public Slice<PlaceSummary> findAllRecordPlace(
      Long memberId, SortType sortType, Cursorable<String> cursorable) {
    NumberExpression<Long> countExpr = post.id.count();
    DateExpression<LocalDate> latestStudyDate = post.studyDate.max();
    NumberExpression<Long> studyTimeSumExpr = post.studyTime.sum().longValue();
    NumberExpression<Double> avgFocusExpr = post.focus.avg();
    Expression<String> thumbnail = recordThumbnail(memberId);
    List<Tuple> tuples =
        queryFactory
            .select(
                place.id,
                place.name,
                place.address,
                place.latitude,
                place.longitude,
                countExpr,
                thumbnail,
                latestStudyDate,
                studyTimeSumExpr,
                avgFocusExpr)
            .from(post)
            .join(post.place, place)
            .where(post.member.id.eq(memberId))
            .groupBy(place.id)
            .having(recordPlaceCursorHaving(sortType, cursorable.getCursor(), countExpr))
            .orderBy(orderBy(sortType, false))
            .limit(cursorable.getLimit() + 1)
            .fetch();
    Slice<Tuple> tupleSlice =
        Slice.of(
            tuples, cursorable, recordPlaceCursorExtractor(sortType, countExpr, latestStudyDate));
    List<Long> placeIds = tupleSlice.getContent().stream().map(t -> t.get(place.id)).toList();
    Map<Long, PlaceCategoryCode> categoryMap = fetchRecordCategoryMap(memberId, placeIds);
    return tupleSlice.map(
        t -> {
          Long pid = t.get(place.id);
          Long studyTime = t.get(studyTimeSumExpr);
          return PlaceSummary.of(
              pid,
              t.get(place.name),
              t.get(place.address),
              t.get(place.latitude),
              t.get(place.longitude),
              t.get(countExpr),
              t.get(thumbnail),
              categoryMap.get(pid),
              studyTime != null ? studyTime : 0L,
              t.get(avgFocusExpr));
        });
  }

  public Slice<PlaceSummary> findAllBookmarkPlaces(
      Long memberId, SortType sortType, Cursorable<String> cursorable) {
    NumberExpression<Long> countExpr = bookMark.id.count();
    DateTimeExpression<LocalDateTime> latestBookmarkedAt = bookMark.createdAt.max();
    NumberExpression<Long> studyTimeSumExpr = post.studyTime.sum().longValue();
    NumberExpression<Double> avgFocusExpr = post.focus.avg();
    Expression<String> thumbnail = bookmarkThumbnail(memberId);
    List<Tuple> tuples =
        queryFactory
            .select(
                place.id,
                place.name,
                place.address,
                place.latitude,
                place.longitude,
                countExpr,
                thumbnail,
                latestBookmarkedAt,
                studyTimeSumExpr,
                avgFocusExpr)
            .from(bookMark)
            .join(bookMark.post, post)
            .join(post.place, place)
            .where(bookMark.member.id.eq(memberId))
            .groupBy(place.id)
            .having(bookmarkPlaceCursorHaving(sortType, cursorable.getCursor(), countExpr))
            .orderBy(orderBy(sortType, true))
            .limit(cursorable.getLimit() + 1)
            .fetch();
    Slice<Tuple> tupleSlice =
        Slice.of(
            tuples,
            cursorable,
            bookmarkPlaceCursorExtractor(sortType, countExpr, latestBookmarkedAt));
    List<Long> placeIds = tupleSlice.getContent().stream().map(t -> t.get(place.id)).toList();
    Map<Long, PlaceCategoryCode> categoryMap = fetchBookmarkCategoryMap(memberId, placeIds);
    Map<Long, Long> userCountMap = fetchPlaceBookmarkUserCountMap(placeIds);
    return tupleSlice.map(
        t -> {
          Long pid = t.get(place.id);
          Long studyTime = t.get(studyTimeSumExpr);
          return PlaceSummary.of(
              pid,
              t.get(place.name),
              t.get(place.address),
              t.get(place.latitude),
              t.get(place.longitude),
              userCountMap.getOrDefault(pid, 0L),
              t.get(thumbnail),
              categoryMap.get(pid),
              studyTime != null ? studyTime : 0L,
              t.get(avgFocusExpr));
        });
  }

  private Map<Long, Long> fetchPlaceBookmarkUserCountMap(List<Long> placeIds) {
    if (placeIds.isEmpty()) return Map.of();
    QBookMark bm2 = new QBookMark("bm2");
    QPost p2 = new QPost("p2");
    return queryFactory
        .select(p2.place.id, bm2.member.id.countDistinct())
        .from(bm2)
        .join(bm2.post, p2)
        .where(p2.place.id.in(placeIds))
        .groupBy(p2.place.id)
        .fetch()
        .stream()
        .collect(
            Collectors.toMap(t -> t.get(p2.place.id), t -> t.get(bm2.member.id.countDistinct())));
  }

  public List<PlaceSearchResult> findRecordedPlacesByKeyword(Long memberId, String keyword) {
    DateExpression<LocalDate> latestStudyDate = post.studyDate.max();
    return queryFactory
        .select(
            place.id, place.name, place.address, place.latitude, place.longitude, latestStudyDate)
        .from(post)
        .join(post.place, place)
        .where(post.member.id.eq(memberId), place.name.containsIgnoreCase(keyword))
        .groupBy(place.id)
        .orderBy(latestStudyDate.desc())
        .fetch()
        .stream()
        .map(
            t ->
                new PlaceSearchResult(
                    t.get(place.id),
                    t.get(place.name),
                    t.get(place.address),
                    t.get(place.latitude),
                    t.get(place.longitude),
                    t.get(latestStudyDate)))
        .toList();
  }

  private Map<Long, List<PlaceTag>> toTags(List<Long> postIds) {
    if (postIds.isEmpty()) return Map.of();
    return queryFactory
        .select(postTag.post.id, tag.placeTag)
        .from(postTag)
        .join(postTag.tag, tag)
        .where(postTag.post.id.in(postIds))
        .fetch()
        .stream()
        .collect(
            Collectors.groupingBy(
                t -> t.get(postTag.post.id),
                Collectors.mapping(t -> t.get(tag.placeTag), Collectors.toList())));
  }

  private Map<Long, PlaceCategoryCode> fetchRecordCategoryMap(Long memberId, List<Long> placeIds) {
    if (placeIds.isEmpty()) return Map.of();
    NumberExpression<Long> cntExpr = post.id.count();
    List<Tuple> tuples =
        queryFactory
            .select(post.place.id, post.placeCategory, cntExpr)
            .from(post)
            .where(post.member.id.eq(memberId), post.place.id.in(placeIds))
            .groupBy(post.place.id, post.placeCategory)
            .fetch();
    return toCategoryModeMap(tuples, cntExpr);
  }

  private Map<Long, PlaceCategoryCode> fetchBookmarkCategoryMap(
      Long memberId, List<Long> placeIds) {
    if (placeIds.isEmpty()) return Map.of();
    NumberExpression<Long> cntExpr = bookMark.id.count();
    List<Tuple> tuples =
        queryFactory
            .select(post.place.id, post.placeCategory, cntExpr)
            .from(bookMark)
            .join(bookMark.post, post)
            .where(bookMark.member.id.eq(memberId), post.place.id.in(placeIds))
            .groupBy(post.place.id, post.placeCategory)
            .fetch();
    return toCategoryModeMap(tuples, cntExpr);
  }

  private Map<Long, PlaceCategoryCode> toCategoryModeMap(
      List<Tuple> tuples, NumberExpression<Long> cntExpr) {
    Map<Long, Long> maxCounts = new HashMap<>();
    Map<Long, PlaceCategoryCode> result = new HashMap<>();
    for (Tuple t : tuples) {
      Long placeId = t.get(post.place.id);
      PlaceCategoryCode cat = t.get(post.placeCategory);
      Long cnt = t.get(cntExpr);
      if (cnt > maxCounts.getOrDefault(placeId, -1L)) {
        maxCounts.put(placeId, cnt);
        result.put(placeId, cat);
      }
    }
    return result;
  }

  public Optional<PlaceDetail> findRecordPinDetailByPlaceId(Long memberId, Long placeId) {
    NumberExpression<Long> countExpr = post.id.count();
    NumberExpression<Long> studyTimeSumExpr = post.studyTime.sum().longValue();
    NumberExpression<Double> avgFocusExpr = post.focus.avg();
    Expression<String> thumbnail = recordThumbnail(memberId);
    Tuple t =
        queryFactory
            .select(
                place.id,
                place.name,
                place.address,
                countExpr,
                avgFocusExpr,
                studyTimeSumExpr,
                thumbnail)
            .from(post)
            .join(post.place, place)
            .where(post.member.id.eq(memberId), place.id.eq(placeId))
            .groupBy(place.id)
            .fetchOne();
    if (t == null) return Optional.empty();
    Long studyTime = t.get(studyTimeSumExpr);
    PlaceCategoryCode category = fetchRecordCategoryMap(memberId, List.of(placeId)).get(placeId);
    return Optional.of(
        PlaceDetail.of(
            t.get(place.id),
            t.get(place.name),
            t.get(place.address),
            t.get(countExpr),
            t.get(avgFocusExpr),
            studyTime != null ? studyTime : 0L,
            t.get(thumbnail),
            category));
  }

  public Optional<PlaceDetail> findBookmarkPinDetailByPlaceId(Long memberId, Long placeId) {
    NumberExpression<Long> studyTimeSumExpr = post.studyTime.sum().longValue();
    NumberExpression<Double> avgFocusExpr = post.focus.avg();
    Expression<String> thumbnail = bookmarkThumbnail(memberId);
    Tuple t =
        queryFactory
            .select(place.id, place.name, place.address, avgFocusExpr, studyTimeSumExpr, thumbnail)
            .from(bookMark)
            .join(bookMark.post, post)
            .join(post.place, place)
            .where(bookMark.member.id.eq(memberId), place.id.eq(placeId))
            .groupBy(place.id)
            .fetchOne();
    if (t == null) return Optional.empty();
    Long studyTime = t.get(studyTimeSumExpr);
    Long userCount = fetchPlaceBookmarkUserCountMap(List.of(placeId)).getOrDefault(placeId, 0L);
    PlaceCategoryCode category = fetchBookmarkCategoryMap(memberId, List.of(placeId)).get(placeId);
    return Optional.of(
        PlaceDetail.of(
            t.get(place.id),
            t.get(place.name),
            t.get(place.address),
            userCount,
            t.get(avgFocusExpr),
            studyTime != null ? studyTime : 0L,
            t.get(thumbnail),
            category));
  }

  // ── cursor having ──────────────────────────────────────────────────────────

  private BooleanExpression recordPlaceCursorHaving(
      SortType sortType, String cursor, NumberExpression<Long> countExpr) {
    if (cursor == null || cursor.isBlank()) return null;
    String[] parts = cursor.split("\\|");
    if (sortType == SortType.RECORD_COUNT) {
      long count = Long.parseLong(parts[0]);
      long id = Long.parseLong(parts[1]);
      return countExpr.lt(count).or(countExpr.eq(count).and(place.id.lt(id)));
    }
    LocalDate studyDate = LocalDate.parse(parts[0]);
    long id = Long.parseLong(parts[1]);
    return post.studyDate
        .max()
        .lt(studyDate)
        .or(post.studyDate.max().eq(studyDate).and(place.id.lt(id)));
  }

  private BooleanExpression bookmarkPlaceCursorHaving(
      SortType sortType, String cursor, NumberExpression<Long> countExpr) {
    if (cursor == null || cursor.isBlank()) return null;
    String[] parts = cursor.split("\\|");
    if (sortType == SortType.RECORD_COUNT) {
      long count = Long.parseLong(parts[0]);
      long id = Long.parseLong(parts[1]);
      return countExpr.lt(count).or(countExpr.eq(count).and(place.id.lt(id)));
    }
    LocalDateTime bookmarkedAt = LocalDateTime.parse(parts[0]);
    long id = Long.parseLong(parts[1]);
    return bookMark
        .createdAt
        .max()
        .lt(bookmarkedAt)
        .or(bookMark.createdAt.max().eq(bookmarkedAt).and(place.id.lt(id)));
  }

  // ── cursor condition ───────────────────────────────────────────────────────

  private BooleanExpression postCursorCondition(SortType sortType, String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    if (sortType == SortType.LATEST) return post.id.lt(Long.parseLong(cursor));

    String[] parts = cursor.split("\\|");
    int value = Integer.parseInt(parts[0]);
    long id = Long.parseLong(parts[1]);
    NumberPath<Integer> field = sortType == SortType.STUDY_TIME ? post.studyTime : post.focus;
    return field.lt(value).or(field.eq(value).and(post.id.lt(id)));
  }

  // ── cursor extractors ──────────────────────────────────────────────────────

  private Function<Tuple, String> recordPlaceCursorExtractor(
      SortType sortType,
      NumberExpression<Long> countExpr,
      DateExpression<LocalDate> latestStudyDate) {
    if (sortType == SortType.RECORD_COUNT) return t -> t.get(countExpr) + "|" + t.get(place.id);
    return t -> t.get(latestStudyDate) + "|" + t.get(place.id);
  }

  private Function<Tuple, String> bookmarkPlaceCursorExtractor(
      SortType sortType,
      NumberExpression<Long> countExpr,
      DateTimeExpression<LocalDateTime> latestBookmarkedAt) {
    if (sortType == SortType.RECORD_COUNT) return t -> t.get(countExpr) + "|" + t.get(place.id);
    return t -> t.get(latestBookmarkedAt) + "|" + t.get(place.id);
  }

  private Function<Tuple, String> postCursorExtractor(SortType sortType) {
    return switch (sortType) {
      case STUDY_TIME -> t -> t.get(post).getStudyTime() + "|" + t.get(post).getId();
      case FOCUS -> t -> t.get(post).getFocus() + "|" + t.get(post).getId();
      default -> t -> String.valueOf(t.get(post).getId());
    };
  }

  // ── order by ───────────────────────────────────────────────────────────────

  private OrderSpecifier<?>[] postOrderBy(SortType sortType) {
    return switch (sortType) {
      case STUDY_TIME -> new OrderSpecifier<?>[] {post.studyTime.desc(), post.id.desc()};
      case FOCUS -> new OrderSpecifier<?>[] {post.focus.desc(), post.id.desc()};
      default -> new OrderSpecifier<?>[] {post.id.desc()};
    };
  }

  private OrderSpecifier<?>[] orderBy(SortType sortType, boolean isBookmark) {
    if (sortType == SortType.RECORD_COUNT) {
      return new OrderSpecifier[] {
        isBookmark ? bookMark.id.count().desc() : post.id.count().desc(), place.id.desc()
      };
    }
    return new OrderSpecifier[] {
      isBookmark ? bookMark.createdAt.max().desc() : post.studyDate.max().desc(), place.id.desc()
    };
  }

  // ── tag filter ─────────────────────────────────────────────────────────────

  private BooleanExpression tagFilterCondition(List<PlaceTag> tags) {
    if (tags == null || tags.isEmpty()) return null;
    return post.id.in(
        JPAExpressions.select(postTag.post.id)
            .from(postTag)
            .join(postTag.tag, tag)
            .where(tag.placeTag.in(tags)));
  }

  // ── thumbnails ─────────────────────────────────────────────────────────────

  private Expression<String> recordThumbnail(Long memberId) {
    return JPAExpressions.select(postImage.imageUrl.max())
        .from(postImage)
        .join(postImage.post, post)
        .where(post.place.id.eq(place.id), post.member.id.eq(memberId));
  }

  private Expression<String> bookmarkThumbnail(Long memberId) {
    return JPAExpressions.select(postImage.imageUrl.max())
        .from(postImage)
        .join(postImage.post, post)
        .join(bookMark)
        .on(bookMark.post.id.eq(post.id), bookMark.member.id.eq(memberId))
        .where(post.place.id.eq(place.id));
  }
}
