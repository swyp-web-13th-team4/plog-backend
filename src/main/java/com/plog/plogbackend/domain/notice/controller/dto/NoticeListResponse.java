package com.plog.plogbackend.domain.notice.controller.dto;

import com.plog.plogbackend.domain.notice.service.dto.NoticesResult;
import java.time.LocalDateTime;

public record NoticeListResponse(
    Long id, String title, String content, LocalDateTime localDateTime) {

  public static NoticeListResponse of(NoticesResult result) {

    return new NoticeListResponse(
        result.id(), result.title(), result.content(), result.localDateTime());
  }
}
