package com.plog.plogbackend.domain.map.controller.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.map.repository.dto.PlaceDetail;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.model.PlaceReviewEnvironmentSummary;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceDetailResponseTest {

  @Test
  @DisplayName("장소 상세 응답에 리뷰 요약 통계를 포함한다")
  void from_includesReviewSummary() {
    PlaceReviewSummary reviewSummary =
        new PlaceReviewSummary(
            15L,
            4.27,
            List.of(new PlaceReviewEnvironmentSummary(ReviewEnvironmentName.SPACE_SIZE, 4, 15L)));
    PlaceDetail detail =
        PlaceDetail.of(
                1L,
                "스타벅스 광화문점",
                "서울시 종로구 세종대로 172",
                24L,
                4.5,
                1440L,
                "https://storage/place.jpg",
                PlaceCategoryCode.CAFE)
            .withReviewSummary(reviewSummary);

    PlaceDetailResponse response = PlaceDetailResponse.from(detail);

    assertThat(response.reviewSummary().reviewCount()).isEqualTo(15L);
    assertThat(response.reviewSummary().averageRating()).isEqualTo(4.27);
    assertThat(response.reviewSummary().environments()).hasSize(1);
    PlaceReviewEnvironmentSummaryResponse environment =
        response.reviewSummary().environments().get(0);
    assertThat(environment.environmentName()).isEqualTo("spaceSize");
    assertThat(environment.title()).isEqualTo("공간 크기");
    assertThat(environment.iconName()).isEqualTo("company-filled");
    assertThat(environment.score()).isEqualTo(4);
    assertThat(environment.label()).isEqualTo("넓은 편이에요");
    assertThat(environment.count()).isEqualTo(15L);
  }

  @Test
  @DisplayName("리뷰 데이터가 없으면 장소 상세 응답의 리뷰 요약을 null로 반환한다")
  void from_withoutReviews_returnsNullReviewSummary() {
    PlaceDetail detail =
        PlaceDetail.of(
            1L,
            "스타벅스 광화문점",
            "서울시 종로구 세종대로 172",
            24L,
            4.5,
            1440L,
            "https://storage/place.jpg",
            PlaceCategoryCode.CAFE);

    PlaceDetailResponse response = PlaceDetailResponse.from(detail);

    assertThat(response.reviewSummary()).isNull();
  }
}
