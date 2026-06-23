package com.plog.plogbackend.domain.notice.controller.dto;

import com.plog.plogbackend.domain.notice.service.dto.NoticeResult;
import java.time.LocalDateTime;

public record NoticeResponse(Long id, String title, String content, LocalDateTime localDateTime) {

  public static NoticeResponse of(NoticeResult result) {

    return new NoticeResponse(
        result.id(), result.title(), result.content(), result.localDateTime());
  }
}
