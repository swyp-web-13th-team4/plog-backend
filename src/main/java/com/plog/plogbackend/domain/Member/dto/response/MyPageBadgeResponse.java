package com.plog.plogbackend.domain.Member.dto.response;

import com.plog.plogbackend.domain.badge.dto.BadgeResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * GET /api/members/badge 응답
 * - 획득한 배지 목록
 */
public record MyPageBadgeResponse(
    @Schema(description = "획득한 배지 목록") List<BadgeResponse> myBadges) {}
