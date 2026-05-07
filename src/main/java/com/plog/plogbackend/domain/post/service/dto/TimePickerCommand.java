package com.plog.plogbackend.domain.post.service.dto;

import com.plog.plogbackend.domain.post.controller.dto.request.post.TimePickerRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record TimePickerCommand(Integer hour, Integer minute) {

  public static TimePickerCommand from(TimePickerRequest req) {
    return new TimePickerCommand(req.hour(), req.minute());
  }

  /** 주어진 날짜와 결합해 LocalDateTime으로 변환. Service가 자정 보정 등에 사용. */
  public LocalDateTime atDate(LocalDate date) {
    return LocalDateTime.of(date, LocalTime.of(hour, minute));
  }
}
