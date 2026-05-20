package com.plog.plogbackend.domain.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PlaceReviewEditPolicyTest {

  @Test
  @DisplayName("장소 리뷰는 수정 가능 시각까지 수정 가능하다")
  void isEditable_untilEditableUntil() {
    PlaceReview review = PlaceReview.create(null, null, 5, "좋았어요", null);
    LocalDateTime editableUntil = LocalDateTime.of(2026, 1, 31, 12, 0);
    ReflectionTestUtils.setField(review, "editableUntil", editableUntil);

    assertThat(review.isEditable(editableUntil.minusNanos(1))).isTrue();
    assertThat(review.isEditable(editableUntil)).isTrue();
    assertThat(review.isEditable(editableUntil.plusNanos(1))).isFalse();
  }

  @Test
  @DisplayName("수정 가능 기간이 지나면 장소 리뷰 수정 검증에 실패한다")
  void validateEditable_afterEditableUntil() {
    PlaceReview review = PlaceReview.create(null, null, 5, "좋았어요", null);
    LocalDateTime editableUntil = LocalDateTime.of(2026, 1, 31, 12, 0);
    ReflectionTestUtils.setField(review, "editableUntil", editableUntil);

    assertThatThrownBy(() -> review.validateEditable(editableUntil.plusNanos(1)))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.PLACE_REVIEW_EDIT_PERIOD_EXPIRED);
  }

  @Test
  @DisplayName("장소 리뷰 수정 시 별점, 내용, 환경 점수를 변경한다")
  void update_changesReviewFields() {
    PlaceReview review = PlaceReview.create(null, null, 5, "좋았어요", null);
    LocalDateTime editableUntil = LocalDateTime.of(2026, 1, 31, 12, 0);
    ReflectionTestUtils.setField(review, "editableUntil", editableUntil);
    Map<ReviewEnvironmentName, Integer> environments = new EnumMap<>(ReviewEnvironmentName.class);
    environments.put(ReviewEnvironmentName.SPACE_SIZE, 1);
    environments.put(ReviewEnvironmentName.NOISE_LEVEL, 2);
    environments.put(ReviewEnvironmentName.CONGESTION_LEVEL, 3);
    environments.put(ReviewEnvironmentName.FOCUS_LEVEL, 4);

    review.update(3, "수정한 리뷰입니다", environments, editableUntil);

    assertThat(review.getRating()).isEqualTo(3);
    assertThat(review.getContent()).isEqualTo("수정한 리뷰입니다");
    assertThat(review.getEnvironments()).containsAllEntriesOf(environments);
  }

  @Test
  @DisplayName("장소 리뷰 삭제는 상태와 삭제 시각만 변경한다")
  void delete_marksReviewDeleted() {
    PlaceReview review = PlaceReview.create(null, null, 5, "좋았어요", null);

    review.delete();

    assertThat(review.getStatus()).isEqualTo(EntityStatus.DELETED);
    assertThat(review.getDeletedAt()).isNotNull();
  }
}
