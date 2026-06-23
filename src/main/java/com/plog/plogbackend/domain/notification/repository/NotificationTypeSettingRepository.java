package com.plog.plogbackend.domain.notification.repository;

import com.plog.plogbackend.domain.notification.entity.NotificationTypeSetting;
import com.plog.plogbackend.domain.notification.enums.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTypeSettingRepository
    extends JpaRepository<NotificationTypeSetting, Long> {

  Optional<NotificationTypeSetting> findByMemberIdAndNotificationType(
      Long memberId, NotificationType notificationType);

  List<NotificationTypeSetting> findAllByMemberId(Long memberId);
}
