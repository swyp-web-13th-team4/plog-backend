package com.plog.plogbackend.domain.Member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 공간별 순위 응답 DTO */
@Schema(description = "공간별 순위 정보")
public record SpaceRankingResponse(
    @Schema(description = "장소 카테고리명") String placeCategoryName,
    @Schema(description = "평균 집중도 (소수점 첫째 자리까지)") Double averageFocus) {}
