package com.plog.plogbackend.domain.review.controller.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.review.dto.request.PlaceReviewCreateRequest;
import com.plog.plogbackend.domain.review.dto.request.PlaceReviewUpdateRequest;
import com.plog.plogbackend.domain.review.dto.request.ReviewEnvironmentRequest;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewCreateCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewUpdateCommand;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceReviewMapperTest {

  @Test
  @DisplayName("장소 리뷰 생성 요청을 서비스 Command로 변환한다")
  void fromCreateRequest() {
    Long postId = 1L;
    UUID memberKey = UUID.randomUUID();
    ReviewEnvironmentRequest environments = new ReviewEnvironmentRequest(5, 4, 3, 2);
    PlaceReviewCreateRequest request = new PlaceReviewCreateRequest(4, environments, "집중하기 좋았어요");

    PlaceReviewCreateCommand command = PlaceReviewMapper.from(postId, request, memberKey);

    assertThat(command.postId()).isEqualTo(postId);
    assertThat(command.memberKey()).isEqualTo(memberKey);
    assertThat(command.rating()).isEqualTo(4);
    assertThat(command.content()).isEqualTo("집중하기 좋았어요");
    assertThat(command.environments())
        .containsEntry(ReviewEnvironmentName.SPACE_SIZE, 5)
        .containsEntry(ReviewEnvironmentName.NOISE_LEVEL, 4)
        .containsEntry(ReviewEnvironmentName.CONGESTION_LEVEL, 3)
        .containsEntry(ReviewEnvironmentName.FOCUS_LEVEL, 2);
  }

  @Test
  @DisplayName("장소 리뷰 수정 요청을 서비스 Command로 변환한다")
  void fromUpdateRequest() {
    Long reviewId = 10L;
    UUID memberKey = UUID.randomUUID();
    ReviewEnvironmentRequest environments = new ReviewEnvironmentRequest(1, 2, 3, 4);
    PlaceReviewUpdateRequest request = new PlaceReviewUpdateRequest(3, environments, "수정한 리뷰입니다");

    PlaceReviewUpdateCommand command = PlaceReviewMapper.from(reviewId, request, memberKey);

    assertThat(command.reviewId()).isEqualTo(reviewId);
    assertThat(command.memberKey()).isEqualTo(memberKey);
    assertThat(command.rating()).isEqualTo(3);
    assertThat(command.content()).isEqualTo("수정한 리뷰입니다");
    assertThat(command.environments())
        .containsEntry(ReviewEnvironmentName.SPACE_SIZE, 1)
        .containsEntry(ReviewEnvironmentName.NOISE_LEVEL, 2)
        .containsEntry(ReviewEnvironmentName.CONGESTION_LEVEL, 3)
        .containsEntry(ReviewEnvironmentName.FOCUS_LEVEL, 4);
  }
}
