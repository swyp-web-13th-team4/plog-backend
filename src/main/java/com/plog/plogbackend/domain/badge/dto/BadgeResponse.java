package com.plog.plogbackend.domain.badge.dto;

import com.plog.plogbackend.domain.badge.entity.Badge;
import java.time.LocalDateTime;

public record BadgeResponse(
    Long id, String name, String description, String imageUrl, LocalDateTime acquiredAt) {
  public static BadgeResponse from(Badge badge) {
    return new BadgeResponse(
        badge.getId(), badge.getName(), badge.getDescription(), badge.getImageUrl(), null);
  }

  public static BadgeResponse from(Badge badge, LocalDateTime acquiredAt) {
    return new BadgeResponse(
        badge.getId(), badge.getName(), badge.getDescription(), badge.getImageUrl(), acquiredAt);
  }
}
