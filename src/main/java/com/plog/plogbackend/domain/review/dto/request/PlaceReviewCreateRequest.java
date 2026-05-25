package com.plog.plogbackend.domain.review.dto.request;

import static com.plog.plogbackend.domain.review.entity.PlaceReview.*;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaceReviewCreateRequest(
    /** POST요청으로 별점,장소 환경,후기,이미지 */
    @Schema(description = "장소 리뷰 별점", example = "5") @NotNull @Min(1) @Max(5) Integer rating,
    @Schema(description = "장소 환경 점수") @Valid @NotNull ReviewEnvironmentRequest environments,
    /** 컨텍츠 최대 길이 300 * */
    @Schema(description = "장소 리뷰 내용", example = "집중하기 좋은 공간이었어요.")
        @Size(max = CONTENT_MAX_LENGTH)
        String content) {}
