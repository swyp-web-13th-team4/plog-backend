package com.plog.plogbackend.domain.review.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.model.PlaceReviewEnvironmentSummary;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceReviewSummaryResponseTest {

  @Test
  @DisplayName("리뷰 요약 통계를 응답으로 변환한다")
  void from_convertsReviewSummary() {
    PlaceReviewSummary summary =
        new PlaceReviewSummary(
            15L,
            4.27,
            List.of(new PlaceReviewEnvironmentSummary(ReviewEnvironmentName.SPACE_SIZE, 4, 15L)));

    PlaceReviewSummaryResponse response = PlaceReviewSummaryResponse.from(summary);

    assertThat(response.reviewCount()).isEqualTo(15L);
    assertThat(response.averageRating()).isEqualTo(4.27);
    assertThat(response.environments()).hasSize(1);
    PlaceReviewEnvironmentSummaryResponse environment = response.environments().get(0);
    assertThat(environment.environmentName()).isEqualTo("spaceSize");
    assertThat(environment.title()).isEqualTo("공간 크기");
    assertThat(environment.iconName()).isEqualTo("company-filled");
    assertThat(environment.score()).isEqualTo(4);
    assertThat(environment.label()).isEqualTo("넓은 편이에요");
    assertThat(environment.count()).isEqualTo(15L);
  }

  @Test
  @DisplayName("리뷰 데이터가 없으면 null로 변환한다")
  void from_emptySummary_returnsNull() {
    PlaceReviewSummaryResponse response =
        PlaceReviewSummaryResponse.from(PlaceReviewSummary.empty());

    assertThat(response).isNull();
  }
}
