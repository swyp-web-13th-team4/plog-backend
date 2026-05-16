package com.plog.plogbackend.domain.member.dto.response;

import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 집중 환경 분석 응답 DTO
 *
 * <p>3가지 영역의 분석 결과를 담습니다:
 *
 * <ul>
 *   <li>영역 1: 시간대별 몰입 분석 - 집중도가 가장 높은 시간대와 해당 평균 점수
 *   <li>영역 2: 태그 & 집중도 분석 - 평균 집중도가 가장 높은 태그 및 해당 평균 점수
 *   <li>영역 3: 집중 방해 요소 분석 - 집중도 편차가 가장 큰 태그 및 해당 평균 점수
 * </ul>
 */
@Schema(description = "나에게 맞는 집중 환경 조건 분석 결과")
public record FocusEnvironmentResponse(
    @Schema(description = "가장 집중도가 높은 시간대 (오전/오후/밤/새벽)") String bestTimePeriod,
    @Schema(description = "최고 집중 시간대의 평균 집중도 점수") Double bestTimePeriodAvgFocus,
    @Schema(description = "평균 집중도가 가장 높은 PlaceTag") PlaceTag bestPlaceTag,
    @Schema(description = "최고 집중 태그의 평균 집중도 점수") Double bestPlaceTagAvgFocus,
    @Schema(description = "집중 방해 요소 PlaceTag") PlaceTag worstPlaceTag,
    @Schema(description = "집중 방해 태그의 평균 집중도 점수") Double worstPlaceTagAvgFocus) {}
