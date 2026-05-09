package com.plog.plogbackend.domain.post.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(
    name = "TimePickerResponse",
    description = "타임피커 응답 — hour/minute 분리",
    example = "{\"hour\": 14, \"minute\": 30}")
public record TimePickerResponse(
    @Schema(description = "시 (0~23)", example = "14") Integer hour,
    @Schema(description = "분 (0~59)", example = "30") Integer minute) {

  public static TimePickerResponse from(LocalDateTime localDateTime) {
    return new TimePickerResponse(localDateTime.getHour(), localDateTime.getMinute());
  }
}
