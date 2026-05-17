package com.plog.plogbackend.domain.post;

import static com.plog.plogbackend.domain.post.entity.Post.*;
import static com.plog.plogbackend.domain.post.entity.Post.MAX_CONTENTS_COUNT;

import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class PostValidator {

  /** 타임피커 최대 범위 레인지 */
  private static final int MAX_STUDY_MINUTES = 24 * 60;

  public static void validateTitleAndContent(String title, String contents) {
    int titleLength = title.trim().length();
    if (titleLength < MIN_TITLE_LENGTH || titleLength > MAX_TITLE_LENGTH) {
      throw new AppException(ErrorType.INVALID_TITLE_LENGTH);
    }
    String trimmedContents = contents.trim();
    int contentsCount = trimmedContents.codePointCount(0, trimmedContents.length());
    if (contentsCount < MIN_CONTENTS_COUNT || contentsCount > MAX_CONTENTS_COUNT) {
      throw new AppException(ErrorType.INVALID_CONTENTS_LENGTH);
    }
  }

  public static void validateStudyDuration(LocalDateTime startedAt, LocalDateTime endedAt) {
    long minutes = Duration.between(startedAt, endedAt).toMinutes();
    if (minutes > MAX_STUDY_MINUTES) {
      throw new AppException(ErrorType.STUDY_TIME_TOO_LONG);
    }
  }
}
