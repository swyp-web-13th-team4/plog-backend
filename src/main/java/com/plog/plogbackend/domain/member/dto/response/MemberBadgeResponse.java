package com.plog.plogbackend.domain.member.dto.response;

import com.plog.plogbackend.domain.badge.entity.Badge;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MemberBadgeResponse(
    @Schema(description = "뱃지 ID", example = "1") Long id,
    @Schema(description = "뱃지 이름", example = "플로깅 마스터") String name,
    @Schema(description = "뱃지 설명", example = "플로깅 10회 참여 시 획득") String description,
    @Schema(description = "뱃지 이미지 URL", example = "https://...") String imageUrl,
    @Schema(description = "획득 여부", example = "true") boolean isAcquired,
    @Schema(description = "획득 일자", example = "2024-05-17T00:00:00") LocalDateTime acquiredAt) {
  public static MemberBadgeResponse of(Badge badge, boolean isAcquired, LocalDateTime acquiredAt) {
    return new MemberBadgeResponse(
        badge.getId(),
        badge.getName(),
        badge.getDescription(),
        badge.getImageUrl(),
        isAcquired,
        acquiredAt);
  }
}
