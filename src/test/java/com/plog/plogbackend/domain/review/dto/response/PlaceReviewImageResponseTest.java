package com.plog.plogbackend.domain.review.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PlaceReviewImageResponseTest {

  @Test
  @DisplayName("리뷰 이미지가 없으면 빈 이미지 응답을 만든다")
  void from_nullImages_returnsEmptyResponse() {
    PlaceReviewImageResponse response = PlaceReviewImageResponse.from(null);

    assertThat(response.images()).isEmpty();
    assertThat(response.total()).isZero();
  }

  @Test
  @DisplayName("리뷰 이미지 ID와 URL을 수정 폼 응답으로 변환한다")
  void from_convertsImageIdAndUrl() {
    PlaceReviewImage image = PlaceReviewImage.of(null, "https://storage/review.jpg");
    ReflectionTestUtils.setField(image, "id", 10L);

    PlaceReviewImageResponse response = PlaceReviewImageResponse.from(List.of(image));

    assertThat(response.total()).isEqualTo(1);
    assertThat(response.images()).hasSize(1);
    assertThat(response.images().get(0).id()).isEqualTo(10L);
    assertThat(response.images().get(0).url()).isEqualTo("https://storage/review.jpg");
  }
}
