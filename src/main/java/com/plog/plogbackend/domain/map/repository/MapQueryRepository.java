package com.plog.plogbackend.domain.map.repository;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;
import static com.plog.plogbackend.domain.post.entity.QPostImage.postImage;
import static com.plog.plogbackend.domain.post.entity.QPostTag.postTag;
import static com.plog.plogbackend.domain.tag.QTag.tag;

import com.plog.plogbackend.domain.map.model.SortType;
import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MapQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<Tuple> findRecordPinsByMemberId(Long memberId, Viewport viewport) {

    return queryFactory
        .select(
            place.id,
            place.latitude,
            place.longitude,
            post.id.count(),
            latestThumbnailByPlace(memberId))
        .from(post)
        .join(post.place, place)
        .where(
            post.member.id.eq(memberId),
            place.latitude.between(viewport.getSwLat(), viewport.getNeLat()),
            place.longitude.between(viewport.getSwLng(), viewport.getNeLng()))
        .groupBy(place.id)
        .fetch();
  }

  public List<Tuple> findBookmarkPinsByMemberId(Long memberId, Viewport viewport) {

    return queryFactory
        .select(
            place.id,
            place.latitude,
            place.longitude,
            bookMark.id.count(),
            latestThumbnailByBookmark(memberId))
        .from(bookMark)
        .join(bookMark.post, post)
        .join(post.place, place)
        .where(
            bookMark.member.id.eq(memberId),
            place.latitude.between(viewport.getSwLat(), viewport.getNeLat()),
            place.longitude.between(viewport.getSwLng(), viewport.getNeLng()))
        .groupBy(place.id)
        .fetch();
  }

  public Slice<Tuple> findRecordsByPlaceId(
      Long memberId,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    List<Tuple> tuples =
        queryFactory
            .select(post, postImage.imageUrl.min(), post.placeCategory.categoryName)
            .from(post)
            .leftJoin(post.placeCategory)
            .leftJoin(postImage)
            .on(postImage.post.id.eq(post.id))
            .where(
                post.member.id.eq(memberId),
                post.place.id.eq(placeId),
                tagFilterCondition(tags),
                cursorConditionByPost(sortType, cursorable.getCursor()))
            .groupBy(post.id)
            .orderBy(orderByPost(sortType))
            .limit(cursorable.getLimit() + 1)
            .fetch();
    return new Slice<>(tuples, cursorable, hasNext(cursorable, tuples));
  }

  public Slice<Tuple> findBookmarksByPlaceId(
      Long memberId,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    List<Tuple> tuples =
        queryFactory
            .select(post, postImage.imageUrl.min(), post.placeCategory.categoryName)
            .from(bookMark)
            .join(bookMark.post, post)
            .leftJoin(post.placeCategory)
            .leftJoin(postImage)
            .on(postImage.post.id.eq(post.id))
            .where(
                bookMark.member.id.eq(memberId),
                post.place.id.eq(placeId),
                tagFilterCondition(tags),
                cursorConditionByPost(sortType, cursorable.getCursor()))
            .groupBy(post.id)
            .orderBy(orderByPost(sortType))
            .limit(cursorable.getLimit() + 1)
            .fetch();
    return new Slice<>(tuples, cursorable, hasNext(cursorable, tuples));
  }

  public Slice<Tuple> findAllRecordPlaces(
      Long memberId, SortType sortType, Cursorable<String> cursorable) {
    NumberExpression<Long> countExpr = post.id.count();
    List<Tuple> tuples =
        queryFactory
            .select(
                place.id,
                place.name,
                place.address,
                place.latitude,
                place.longitude,
                countExpr,
                latestThumbnailByPlace(memberId),
                post.studyDate.max())
            .from(post)
            .join(post.place, place)
            .where(post.member.id.eq(memberId))
            .groupBy(place.id)
            .having(
                latestCursorHaving(sortType, cursorable.getCursor()),
                countCursorHaving(sortType, cursorable.getCursor(), countExpr))
            .orderBy(orderByPlace(sortType, countExpr))
            .limit(cursorable.getLimit() + 1)
            .fetch();
    return new Slice<>(tuples, cursorable, hasNext(cursorable, tuples));
  }

  public Slice<Tuple> findAllBookmarkPlaces(
      Long memberId, SortType sortType, Cursorable<String> cursorable) {
    NumberExpression<Long> countExpr = bookMark.id.count();
    List<Tuple> tuples =
        queryFactory
            .select(
                place.id,
                place.name,
                place.address,
                place.latitude,
                place.longitude,
                countExpr,
                latestThumbnailByBookmark(memberId),
                post.studyDate.max())
            .from(bookMark)
            .join(bookMark.post, post)
            .join(post.place, place)
            .where(bookMark.member.id.eq(memberId))
            .groupBy(place.id)
            .having(
                latestCursorHaving(sortType, cursorable.getCursor()),
                countCursorHaving(sortType, cursorable.getCursor(), countExpr))
            .orderBy(orderByPlace(sortType, countExpr))
            .limit(cursorable.getLimit() + 1)
            .fetch();
    return new Slice<>(tuples, cursorable, hasNext(cursorable, tuples));
  }

  public List<Tuple> findRecordedPlacesByKeyword(Long memberId, String keyword) {
    return queryFactory
        .select(
            place.id,
            place.name,
            place.address,
            place.latitude,
            place.longitude,
            post.studyDate.max())
        .from(post)
        .join(post.place, place)
        .where(post.member.id.eq(memberId), place.name.containsIgnoreCase(keyword))
        .groupBy(place.id)
        .orderBy(post.studyDate.max().desc())
        .fetch();
  }

  public List<Tuple> findPlaceTagsByPostIds(List<Long> postIds) {
    if (postIds.isEmpty()) return List.of();
    return queryFactory
        .select(postTag.post.id, tag.placeTag)
        .from(postTag)
        .join(postTag.tag, tag)
        .where(postTag.post.id.in(postIds))
        .fetch();
  }

  public List<Tuple> findRecordCategoryCountsByPlaceIds(Long memberId, List<Long> placeIds) {
    return queryFactory
        .select(post.place.id, post.placeCategory.categoryName, post.id.count())
        .from(post)
        .join(post.placeCategory)
        .where(post.member.id.eq(memberId), post.place.id.in(placeIds))
        .groupBy(post.place.id, post.placeCategory.categoryName)
        .fetch();
  }

  public List<Tuple> findBookmarkCategoryCountsByPlaceIds(Long memberId, List<Long> placeIds) {
    return queryFactory
        .select(post.place.id, post.placeCategory.categoryName, bookMark.id.count())
        .from(bookMark)
        .join(bookMark.post, post)
        .join(post.placeCategory)
        .where(bookMark.member.id.eq(memberId), post.place.id.in(placeIds))
        .groupBy(post.place.id, post.placeCategory.categoryName)
        .fetch();
  }

  private OrderSpecifier<?>[] orderByPlace(SortType sortType, NumberExpression<Long> countExpr) {
    if (sortType == SortType.RECORD_COUNT) {
      return new OrderSpecifier<?>[] {countExpr.desc(), place.id.desc()};
    }
    return new OrderSpecifier<?>[] {post.studyDate.max().desc(), place.id.desc()};
  }

  private OrderSpecifier<?>[] orderByPost(SortType sortType) {
    return switch (sortType) {
      case STUDY_TIME -> new OrderSpecifier<?>[] {post.studyTime.desc(), post.id.desc()};
      case FOCUS -> new OrderSpecifier<?>[] {post.focus.desc(), post.id.desc()};
      default -> new OrderSpecifier<?>[] {post.id.desc()};
    };
  }

  private BooleanExpression latestCursorHaving(SortType sortType, String cursor) {
    if (cursor == null || cursor.isBlank() || sortType == SortType.RECORD_COUNT) return null;
    String[] parts = cursor.split(":");
    LocalDate studyDate = LocalDate.parse(parts[0]);
    long id = Long.parseLong(parts[1]);
    return post.studyDate
        .max()
        .lt(studyDate)
        .or(post.studyDate.max().eq(studyDate).and(place.id.lt(id)));
  }

  private BooleanExpression countCursorHaving(
      SortType sortType, String cursor, NumberExpression<Long> countExpr) {
    if (cursor == null || cursor.isBlank() || sortType != SortType.RECORD_COUNT) return null;
    String[] parts = cursor.split(":");
    long count = Long.parseLong(parts[0]);
    long id = Long.parseLong(parts[1]);
    return countExpr.lt(count).or(countExpr.eq(count).and(place.id.lt(id)));
  }

  private BooleanExpression cursorConditionByPost(SortType sortType, String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    return switch (sortType) {
      case STUDY_TIME -> {
        String[] parts = cursor.split(":");
        int studyTime = Integer.parseInt(parts[0]);
        long id = Long.parseLong(parts[1]);
        yield post.studyTime.lt(studyTime).or(post.studyTime.eq(studyTime).and(post.id.lt(id)));
      }
      case FOCUS -> {
        String[] parts = cursor.split(":");
        int focus = Integer.parseInt(parts[0]);
        long id = Long.parseLong(parts[1]);
        yield post.focus.lt(focus).or(post.focus.eq(focus).and(post.id.lt(id)));
      }
      default -> post.id.lt(Long.parseLong(cursor));
    };
  }

  private BooleanExpression tagFilterCondition(List<PlaceTag> tags) {
    if (tags == null || tags.isEmpty()) return null;
    return post.id.in(
        JPAExpressions.select(postTag.post.id)
            .from(postTag)
            .join(postTag.tag, tag)
            .where(tag.placeTag.in(tags)));
  }

  private Expression<String> latestThumbnailByPlace(Long memberId) {
    return JPAExpressions.select(postImage.imageUrl)
        .from(postImage)
        .where(
            postImage.id.eq(
                JPAExpressions.select(postImage.id.max())
                    .from(postImage)
                    .join(postImage.post, post)
                    .where(post.place.id.eq(place.id).and(post.member.id.eq(memberId)))));
  }

  private Expression<String> latestThumbnailByBookmark(Long memberId) {
    return JPAExpressions.select(postImage.imageUrl)
        .from(postImage)
        .where(
            postImage.id.eq(
                JPAExpressions.select(postImage.id.max())
                    .from(postImage)
                    .join(postImage.post, post)
                    .join(bookMark)
                    .on(bookMark.post.id.eq(post.id).and(bookMark.member.id.eq(memberId)))
                    .where(post.place.id.eq(place.id))));
  }

  private <T> boolean hasNext(Cursorable<?> cursorable, List<T> content) {
    if (content.size() > cursorable.getLimit()) {
      content.remove(content.size() - 1);
      return true;
    }
    return false;
  }
}
