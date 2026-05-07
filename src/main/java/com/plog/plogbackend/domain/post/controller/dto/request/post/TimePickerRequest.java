package com.plog.plogbackend.domain.post.controller.dto.request.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 타임 피커는 {hour: 12, minute: 30}을 전송합니다 이에 맞게 TimePicker용 DTO를 만들었습니다
 *
 * @param hour
 * @param minute
 */
@Schema(
    name = "TimePickerRequest",
    description = "타임피커 입력 — hour/minute을 분리해서 전송",
    example = "{\"hour\": 14, \"minute\": 30}")
public record TimePickerRequest(
    @Schema(description = "시 (0~23)", example = "14") @NotNull @Min(0) @Max(23) Integer hour,
    @Schema(description = "분 (0~59)", example = "30") @NotNull @Min(0) @Max(59) Integer minute) {}
