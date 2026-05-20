package com.plog.plogbackend.domain.review.dto.request;

import static com.plog.plogbackend.domain.review.entity.PlaceReview.*;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlaceReviewUpdateRequest(
    @Schema(description = "장소 리뷰 별점", example = "4") @NotNull @Min(1) @Max(5) Integer rating,
    @Schema(description = "장소 환경 점수") @Valid @NotNull ReviewEnvironmentRequest environments,
    @Schema(description = "장소 리뷰 내용", example = "다시 보니 조용하고 집중하기 좋았어요.")
        @NotBlank
        @Size(max = CONTENT_MAX_LENGTH)
        String content,
    @Schema(description = "유지할 기존 리뷰 이미지 ID 목록 (없으면 빈 배열)") @NotNull List<Long> keepImageIds) {}
