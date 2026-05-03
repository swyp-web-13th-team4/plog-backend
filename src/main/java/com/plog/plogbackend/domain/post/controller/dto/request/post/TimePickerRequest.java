package com.plog.plogbackend.domain.post.controller.dto.request.post;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 타임 피커는 {hour: 12, minute: 30}을 전송합니다
 * 이에 맞게 TimePicker용 DTO를 만들었습니다
 * @param hour
 * @param minute
 */
public record TimePickerRequest(
        @Min(0) @Max(23) Integer hour,
        @Min(0) @Max(59) Integer minute) {}
