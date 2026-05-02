package com.plog.plogbackend.domain.post.controller.dto.request.post;

import com.plog.plogbackend.domain.post.entity.PublicScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public record PostCreateRequest(
    @Schema(description = "제목") @NotBlank(message = "제목은 필수입니다.") String title,
    @Schema(description = "내용") @NotBlank(message = "내용은 필수입니다") String contents,
    @Schema(description = "공부 시작 시간") LocalDateTime startedAt,
    @Schema(description = "공부 종료 시간") LocalDateTime endedAt,
    @Schema(description = "공부 날짜")
        @NotNull(message = "공부 날짜는 필수입니다.")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate studyDate,
    @Schema(description = "공부 시간") Integer studyTime,
    @Min(value = 1, message = "집중도는 최소값은 1 입니다.")
        @Max(value = 5, message = "집중도는 최대값은 5 입니다.")
        @Schema(description = "집중도")
        @NotNull(message = "집중도는 필수입니다.")
        Integer focus,
    @Schema(description = "공개 범위") @NotNull(message = "공개 범위는 필수입니다.") PublicScope scope,
    @Schema(description = "장소 ID") @NotNull(message = "장소는 필수입니다.") String placeName,
    @Schema(description = "태그 ID 목록") @NotEmpty(message = "태그는 최소 하나 이상 필요합니다.")
        List<String> tagNames,
    @Schema(description = "카테고리 목록") @NotEmpty(message = "카테고리는 최소 하나 이상 필요합니다.")
        List<String> categoryNames) {}
