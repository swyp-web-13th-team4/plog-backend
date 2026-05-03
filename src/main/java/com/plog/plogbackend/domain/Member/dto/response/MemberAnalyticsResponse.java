package com.plog.plogbackend.domain.Member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * GET /api/members/analytics 응답 - 회원 분석 정보
 *
 * <p>5가지 분석 결과를 포함합니다:
 *
 * <ol>
 *   <li>기록 횟수 (totalPostCount)
 *   <li>총 작업 시간 (totalStudyTime)
 *   <li>나의 작업 유형 카드 (workTypeCard) - 5개 미만 시 null
 *   <li>나에게 맞는 집중 환경 조건 (focusEnvironment) - 15개 미만 시 null
 *   <li>공간별 순위 (spaceRankings) - 15개 미만 시 null
 * </ol>
 */
@Schema(description = "회원 분석 정보 응답")
public record MemberAnalyticsResponse(
    @Schema(description = "총 기록 횟수") Integer totalPostCount,
    @Schema(description = "총 작업 시간 (분 단위)") Integer totalStudyTime,
    @Schema(description = "작업 유형 카드 (게시글 5개 미만 시 null)") WorkTypeCardResponse workTypeCard,
    @Schema(description = "집중 환경 분석 (게시글 15개 미만 시 null)")
        FocusEnvironmentResponse focusEnvironment,
    @Schema(description = "공간별 순위 상위 3개 (게시글 15개 미만 시 null)")
        List<SpaceRankingResponse> spaceRankings) {}
