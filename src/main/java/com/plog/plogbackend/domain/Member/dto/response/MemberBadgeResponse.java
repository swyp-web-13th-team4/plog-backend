package com.plog.plogbackend.domain.Member.dto.response;

import com.plog.plogbackend.domain.badge.entity.Badge;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberBadgeResponse(
    @Schema(description = "뱃지 ID", example = "1") Long id,
    @Schema(description = "뱃지 이름", example = "플로깅 마스터") String name,
    @Schema(description = "뱃지 설명", example = "플로깅 10회 참여 시 획득") String description,
    @Schema(description = "뱃지 이미지 URL", example = "https://...") String imageUrl,
    @Schema(description = "획득 여부", example = "true") boolean isAcquired) {
  public static MemberBadgeResponse of(Badge badge, boolean isAcquired) {
    return new MemberBadgeResponse(
        badge.getId(), badge.getName(), badge.getDescription(), badge.getImageUrl(), isAcquired);
  }
}
