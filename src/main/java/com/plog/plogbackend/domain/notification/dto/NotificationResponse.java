package com.plog.plogbackend.domain.notification.dto;

import com.plog.plogbackend.domain.notification.entity.Notification;
import com.plog.plogbackend.domain.notification.enums.NotificationType;
import java.time.LocalDateTime;

/**
 * 알림 목록 응답 DTO.
 *
 * <p>SSE 스트리밍 전송 시에도 동일한 형태를 사용하여 프론트엔드에서 같은 UI 컴포넌트를 재사용할 수 있습니다.
 */
public record NotificationResponse(
    Long notificationId,
    NotificationType type,
    String content,
    String relatedUrl,
    boolean isRead,
    LocalDateTime createdAt) {

  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getContent(),
        notification.getRelatedUrl(),
        notification.isRead(),
        notification.getCreatedAt());
  }
}
