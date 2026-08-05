package com.plog.plogbackend.domain.review.repository;

import static com.plog.plogbackend.domain.post.entity.QPost.post;
import static com.plog.plogbackend.domain.review.entity.QPlaceReview.placeReview;
import static com.plog.plogbackend.domain.review.entity.QPlaceReviewEnvironment.placeReviewEnvironment;

import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewEnvironmentCount;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewRatingSummary;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaceReviewStatisticsRepository {

  private final JPAQueryFactory queryFactory;

  public PlaceReviewRatingSummary findRatingSummaryByPlaceId(Long placeId) {
    return queryFactory
        .select(
            Projections.constructor(
                PlaceReviewRatingSummary.class, placeReview.id.count(), placeReview.rating.avg()))
        .from(placeReview)
        .join(placeReview.post, post)
        .where(post.place.id.eq(placeId), placeReview.status.eq(EntityStatus.ACTIVE))
        .fetchOne();
  }

  public List<PlaceReviewEnvironmentCount> findEnvironmentCountsByPlaceId(Long placeId) {
    return queryFactory
        .select(
            Projections.constructor(
                PlaceReviewEnvironmentCount.class,
                placeReviewEnvironment.name,
                placeReviewEnvironment.score,
                placeReviewEnvironment.id.count()))
        .from(placeReviewEnvironment)
        .join(placeReviewEnvironment.placeReview, placeReview)
        .join(placeReview.post, post)
        .where(post.place.id.eq(placeId), placeReview.status.eq(EntityStatus.ACTIVE))
        .groupBy(placeReviewEnvironment.name, placeReviewEnvironment.score)
        .fetch();
  }
}
