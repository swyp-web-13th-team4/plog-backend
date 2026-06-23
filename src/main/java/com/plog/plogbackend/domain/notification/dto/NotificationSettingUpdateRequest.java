package com.plog.plogbackend.domain.notification.dto;

import com.plog.plogbackend.domain.notification.enums.NotificationType;
import java.util.List;

/** 알림 설정 변경 요청 DTO. */
public record NotificationSettingUpdateRequest(
    Boolean isAllEnabled, List<TypeSettingEntry> typeSettings) {

  public record TypeSettingEntry(NotificationType type, boolean enabled) {}
}
