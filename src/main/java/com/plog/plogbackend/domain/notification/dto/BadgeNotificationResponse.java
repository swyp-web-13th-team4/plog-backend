package com.plog.plogbackend.domain.notification.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BadgeNotificationResponse {
  private Long badgeId;
  private String badgeName;
  private String badgeImage;
  private String message;
  private LocalDateTime acquiredAt;

  public static BadgeNotificationResponse of(
      Long badgeId, String badgeName, String badgeImage, LocalDateTime acquiredAt) {
    return BadgeNotificationResponse.builder()
        .badgeId(badgeId)
        .badgeName(badgeName)
        .badgeImage(badgeImage)
        .message("새로운 뱃지를 획득했습니다: " + badgeName)
        .acquiredAt(acquiredAt)
        .build();
  }
}
