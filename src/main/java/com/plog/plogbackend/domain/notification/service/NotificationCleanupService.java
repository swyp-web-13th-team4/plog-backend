package com.plog.plogbackend.domain.notification.service;

import com.plog.plogbackend.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 오래된 알림을 자동으로 정리하는 스케줄러 서비스.
 *
 * <p>매일 새벽 4시에 실행되며, 생성된 지 30일이 지난 알림을 DB에서 영구 삭제(Hard Delete)합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

  private static final int RETENTION_DAYS = 30;

  private final NotificationRepository notificationRepository;

  /** 매일 새벽 4시에 30일 이상 된 알림을 일괄 삭제합니다. */
  @Scheduled(cron = "0 0 4 * * ?")
  @Transactional
  public void cleanupOldNotifications() {
    java.time.LocalDateTime threshold = java.time.LocalDateTime.now().minusDays(RETENTION_DAYS);

    notificationRepository.deleteByCreatedAtBefore(threshold);
    log.info("{}일 이상 된 알림 정리 완료 (기준 시각: {})", RETENTION_DAYS, threshold);
  }
}
