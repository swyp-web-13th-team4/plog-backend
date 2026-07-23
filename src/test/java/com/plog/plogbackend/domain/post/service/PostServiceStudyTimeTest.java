package com.plog.plogbackend.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostServiceStudyTimeTest {

  @Test
  @DisplayName("종료 시각이 시작 시각보다 이르면 다음 날 종료 시각으로 보정한다")
  void adjustEndedAt_movesEndToNextDay_whenStudyCrossesMidnight() {
    LocalDateTime startedAt = LocalDateTime.of(2026, 7, 23, 22, 0);
    LocalDateTime endedAt = LocalDateTime.of(2026, 7, 23, 2, 0);

    LocalDateTime adjustedEndedAt = PostService.adjustEndedAt(startedAt, endedAt);

    assertThat(adjustedEndedAt).isEqualTo(LocalDateTime.of(2026, 7, 24, 2, 0));
  }

  @Test
  @DisplayName("시작 시각과 종료 시각이 같으면 유효하지 않은 공부 시간 범위 예외를 던진다")
  void adjustEndedAt_throwsException_whenStartAndEndAreEqual() {
    LocalDateTime sameTime = LocalDateTime.of(2026, 7, 23, 22, 0);

    assertThatThrownBy(() -> PostService.adjustEndedAt(sameTime, sameTime))
        .isInstanceOf(AppException.class)
        .extracting(exception -> ((AppException) exception).getErrorType())
        .isEqualTo(ErrorType.INVALID_STUDY_TIME_RANGE);
  }
}
