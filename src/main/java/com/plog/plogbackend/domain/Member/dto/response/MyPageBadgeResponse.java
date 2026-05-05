package com.plog.plogbackend.domain.Member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** GET /api/members/badge 응답 - 전체 배지 목록 (획득 여부 포함) */
public record MyPageBadgeResponse(
    @Schema(description = "전체 배지 목록 및 획득 여부") List<MemberBadgeResponse> badges) {}
