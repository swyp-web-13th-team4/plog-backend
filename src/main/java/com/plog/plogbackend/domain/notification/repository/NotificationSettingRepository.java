package com.plog.plogbackend.domain.notification.repository;

import com.plog.plogbackend.domain.notification.entity.NotificationSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

  Optional<NotificationSetting> findByMemberId(Long memberId);
}
