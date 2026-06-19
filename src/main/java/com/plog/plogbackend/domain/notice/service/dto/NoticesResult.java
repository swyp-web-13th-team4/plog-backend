package com.plog.plogbackend.domain.notice.service.dto;

import com.plog.plogbackend.domain.notice.entity.Notice;
import java.time.LocalDateTime;

public record NoticesResult(Long id, String title, String content, LocalDateTime localDateTime) {

  public static NoticesResult from(Notice notice) {

    return new NoticesResult(
        notice.getId(), notice.getTitle(), notice.getContent(), notice.getLocalDateTime());
  }
}
