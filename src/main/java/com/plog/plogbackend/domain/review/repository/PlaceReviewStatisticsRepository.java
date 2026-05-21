package com.plog.plogbackend.domain.review.repository;

import com.plog.plogbackend.domain.post.entity.QPost;
import com.plog.plogbackend.domain.review.entity.QPlaceReview;
import com.plog.plogbackend.domain.review.entity.QPlaceReviewEnvironment;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewEnvironmentCount;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewRatingSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.plog.plogbackend.domain.post.entity.QPost.*;
import static com.plog.plogbackend.domain.review.entity.QPlaceReview.*;
import static com.plog.plogbackend.domain.review.entity.QPlaceReviewEnvironment.placeReviewEnvironment;

@Repository
@RequiredArgsConstructor
public class PlaceReviewStatisticsRepository{

    private final JPAQueryFactory queryFactory;

    public PlaceReviewRatingSummary findRatingSummaryByPlaceId(Long placeId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                PlaceReviewRatingSummary.class,
                                placeReview.id.count(),
                                placeReview.rating.avg()))
                .from(placeReview)
                .join(placeReview.post, post)
                .where(post.place.id.eq(placeId))
                .fetchOne();
    }

    //placeReview.status.eq(ACTIVE) 조건을 넣지 않는다. 우리가 정한 정책이 삭제된 리뷰도 통계에는 포함
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
                .where(post.place.id.eq(placeId))
                .groupBy(placeReviewEnvironment.name, placeReviewEnvironment.score)
                .fetch();
    }
}