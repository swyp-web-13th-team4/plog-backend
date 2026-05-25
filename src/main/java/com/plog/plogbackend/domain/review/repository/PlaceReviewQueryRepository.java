package com.plog.plogbackend.domain.review.repository;

import static com.plog.plogbackend.domain.member.QMember.member;
import static com.plog.plogbackend.domain.post.entity.QPost.post;
import static com.plog.plogbackend.domain.review.entity.QPlaceReview.placeReview;
import static com.plog.plogbackend.domain.review.entity.QPlaceReviewEnvironment.placeReviewEnvironment;
import static com.plog.plogbackend.domain.review.entity.QPlaceReviewImage.placeReviewImage;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewBaseItem;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewListItem;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaceReviewQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Slice<PlaceReviewListItem> findReviewPageByPlaceId(
      Long placeId, Cursorable<String> cursorable) {
    Slice<PlaceReviewBaseItem> baseSlice = findBaseReviewSlice(placeId, cursorable);
    List<Long> reviewIds = extractReviewIds(baseSlice);

    Map<Long, Map<ReviewEnvironmentName, Integer>> environmentMap = fetchEnvironmentMap(reviewIds);
    Map<Long, List<String>> imageMap = fetchImageMap(reviewIds);

    return baseSlice.map(
        item ->
            toListItem(
                item,
                environmentMap.getOrDefault(item.reviewId(), Map.of()),
                imageMap.getOrDefault(item.reviewId(), List.of())));
  }

  private Slice<PlaceReviewBaseItem> findBaseReviewSlice(
      Long placeId, Cursorable<String> cursorable) {
    List<PlaceReviewBaseItem> baseItems = fetchBaseReviewItems(placeId, cursorable);

    return Slice.of(baseItems, cursorable, item -> item.createdAt() + "|" + item.reviewId());
  }

  private List<PlaceReviewBaseItem> fetchBaseReviewItems(
      Long placeId, Cursorable<String> cursorable) {
    return queryFactory
        .select(
            Projections.constructor(
                PlaceReviewBaseItem.class,
                placeReview.id,
                member.nickname,
                placeReview.rating,
                placeReview.createdAt,
                placeReview.content))
        .from(placeReview)
        .join(placeReview.member, member)
        .join(placeReview.post, post)
        .where(
            post.place.id.eq(placeId),
            placeReview.status.eq(EntityStatus.ACTIVE),
            reviewCursorCondition(cursorable.getCursor()))
        .orderBy(placeReview.createdAt.desc(), placeReview.id.desc())
        .limit(cursorable.getLimit() + 1)
        .fetch();
  }

  private BooleanExpression reviewCursorCondition(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    String[] parts = cursor.split("\\|");
    LocalDateTime createdAt = LocalDateTime.parse(parts[0]);
    Long reviewId = Long.parseLong(parts[1]);

    return placeReview
        .createdAt
        .lt(createdAt)
        .or(placeReview.createdAt.eq(createdAt).and(placeReview.id.lt(reviewId)));
  }

  private List<Long> extractReviewIds(Slice<PlaceReviewBaseItem> baseSlice) {
    return baseSlice.getContent().stream().map(PlaceReviewBaseItem::reviewId).toList();
  }

  private Map<Long, Map<ReviewEnvironmentName, Integer>> fetchEnvironmentMap(List<Long> reviewIds) {
    if (reviewIds.isEmpty()) {
      return Map.of();
    }

    Map<Long, Map<ReviewEnvironmentName, Integer>> environmentMap = new HashMap<>();

    List<Tuple> tuples =
        queryFactory
            .select(
                placeReviewEnvironment.placeReview.id,
                placeReviewEnvironment.name,
                placeReviewEnvironment.score)
            .from(placeReviewEnvironment)
            .where(placeReviewEnvironment.placeReview.id.in(reviewIds))
            .fetch();

    for (Tuple tuple : tuples) {
      Long reviewId = tuple.get(placeReviewEnvironment.placeReview.id);
      ReviewEnvironmentName name = tuple.get(placeReviewEnvironment.name);
      Integer score = tuple.get(placeReviewEnvironment.score);

      environmentMap
          .computeIfAbsent(reviewId, ignored -> new EnumMap<>(ReviewEnvironmentName.class))
          .put(name, score);
    }

    return environmentMap;
  }

  private Map<Long, List<String>> fetchImageMap(List<Long> reviewIds) {
    if (reviewIds.isEmpty()) {
      return Map.of();
    }

    Map<Long, List<String>> imageMap = new HashMap<>();

    List<Tuple> tuples =
        queryFactory
            .select(placeReviewImage.placeReview.id, placeReviewImage.imageUrl)
            .from(placeReviewImage)
            .where(placeReviewImage.placeReview.id.in(reviewIds))
            .orderBy(placeReviewImage.id.asc())
            .fetch();

    for (Tuple tuple : tuples) {
      Long reviewId = tuple.get(placeReviewImage.placeReview.id);
      String imageUrl = tuple.get(placeReviewImage.imageUrl);

      imageMap.computeIfAbsent(reviewId, ignored -> new ArrayList<>()).add(imageUrl);
    }

    return imageMap;
  }

  private PlaceReviewListItem toListItem(
      PlaceReviewBaseItem item,
      Map<ReviewEnvironmentName, Integer> environments,
      List<String> imageUrls) {
    return new PlaceReviewListItem(
        item.reviewId(),
        item.nickname(),
        item.rating(),
        item.createdAt(),
        environments,
        item.content(),
        imageUrls);
  }
}
