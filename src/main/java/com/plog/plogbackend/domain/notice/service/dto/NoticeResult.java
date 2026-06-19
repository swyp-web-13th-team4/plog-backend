package com.plog.plogbackend.domain.notice.service.dto;

import com.plog.plogbackend.domain.notice.entity.Notice;
import java.time.LocalDateTime;

public record NoticeResult(Long id, String title, String content, LocalDateTime localDateTime) {

  public static NoticeResult from(Notice notice) {

    return new NoticeResult(
        notice.getId(), notice.getTitle(), notice.getContent(), notice.getLocalDateTime());
  }
}
