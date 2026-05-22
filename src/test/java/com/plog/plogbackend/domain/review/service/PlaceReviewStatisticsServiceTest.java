package com.plog.plogbackend.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import com.plog.plogbackend.domain.review.repository.PlaceReviewStatisticsRepository;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewEnvironmentCount;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewRatingSummary;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceReviewStatisticsServiceTest {

  @Mock private PlaceReviewStatisticsRepository placeReviewStatisticsRepository;
  @InjectMocks private PlaceReviewStatisticsService placeReviewStatisticsService;

  @Test
  @DisplayName("장소 리뷰 통계가 없으면 빈 요약을 반환한다")
  void getSummary_withoutReviews_returnsEmptySummary() {
    Long placeId = 1L;
    given(placeReviewStatisticsRepository.findRatingSummaryByPlaceId(placeId))
        .willReturn(new PlaceReviewRatingSummary(0L, null));

    PlaceReviewSummary summary = placeReviewStatisticsService.getSummary(placeId);

    assertThat(summary.reviewCount()).isZero();
    assertThat(summary.averageRating()).isZero();
    assertThat(summary.environments()).isEmpty();
  }

  @Test
  @DisplayName("환경 항목별 가장 많이 선택된 점수를 대표 응답으로 선택한다")
  void getSummary_selectsMostSelectedEnvironmentScore() {
    Long placeId = 1L;
    given(placeReviewStatisticsRepository.findRatingSummaryByPlaceId(placeId))
        .willReturn(new PlaceReviewRatingSummary(15L, 4.27));
    given(placeReviewStatisticsRepository.findEnvironmentCountsByPlaceId(placeId))
        .willReturn(
            List.of(
                new PlaceReviewEnvironmentCount(ReviewEnvironmentName.SPACE_SIZE, 5, 3L),
                new PlaceReviewEnvironmentCount(ReviewEnvironmentName.SPACE_SIZE, 4, 15L),
                new PlaceReviewEnvironmentCount(ReviewEnvironmentName.NOISE_LEVEL, 3, 2L),
                new PlaceReviewEnvironmentCount(ReviewEnvironmentName.NOISE_LEVEL, 4, 10L)));

    PlaceReviewSummary summary = placeReviewStatisticsService.getSummary(placeId);

    assertThat(summary.reviewCount()).isEqualTo(15L);
    assertThat(summary.averageRating()).isEqualTo(4.27);
    assertThat(summary.environments())
        .extracting("name", "score", "count")
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple(ReviewEnvironmentName.SPACE_SIZE, 4, 15L),
            org.assertj.core.groups.Tuple.tuple(ReviewEnvironmentName.NOISE_LEVEL, 4, 10L));
  }
}
