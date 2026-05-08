package com.plog.plogbackend.domain.post.controller.dto.request.post;

import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public record PostUpdateRequest(
    @Schema(description = "제목") @NotBlank(message = "제목은 필수입니다.") String title,
    @Schema(description = "내용") @NotBlank(message = "내용은 필수입니다.") String contents,
    @Schema(description = "공부 시작 시각") @NotNull @Valid TimePickerRequest startedAt,
    @Schema(description = "공부 종료 시각") @NotNull @Valid TimePickerRequest endedAt,
    @Schema(description = "공부 날짜") @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate studyDate,
    @Min(value = 1, message = "집중도 최소값은 1입니다.")
        @Max(value = 5, message = "집중도 최대값은 5입니다.")
        @Schema(description = "집중도")
        @NotNull
        Integer focus,
    @Schema(description = "공개 범위") @NotNull PublicScope scope,
    @Schema(description = "장소 정보") @NotNull @Valid PlaceRequest place,
    @Schema(description = "장소 태그", example = "[\"WIFI_FAST\", \"GOOD_VIBE\"]") @NotEmpty
        List<PlaceTag> placeTags,
    @Schema(description = "장소 카테고리", example = "STUDY_CAFE") @NotNull
        PlaceCategoryCode categoryCode,
    @Schema(description = "유지할 기존 이미지 ID 목록 (없으면 빈 배열)") @NotNull List<Long> keepImageIds) {}
