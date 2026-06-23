package com.plog.plogbackend.domain.notification.dto;

import com.plog.plogbackend.domain.notification.enums.NotificationType;
import java.util.List;

/** 알림 설정 조회 응답 DTO. */
public record NotificationSettingResponse(
    boolean isAllEnabled, List<TypeSettingEntry> typeSettings) {

  public record TypeSettingEntry(NotificationType type, boolean enabled) {}
}
