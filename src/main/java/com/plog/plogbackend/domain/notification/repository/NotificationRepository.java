package com.plog.plogbackend.domain.notification.repository;

import com.plog.plogbackend.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /** 안 읽은 알림 개수 조회 */
  long countByReceiverIdAndIsReadFalse(Long receiverId);

  /** 해당 사용자의 모든 알림을 읽음 처리 (벌크 업데이트) */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
  void markAllAsRead(Long receiverId);

  /** 해당 사용자의 모든 알림 삭제 (Hard Delete) */
  void deleteAllByReceiverId(Long receiverId);

  /** 생성일이 threshold 이전인 알림을 일괄 삭제 (30일 자동 정리용) */
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :threshold")
  void deleteByCreatedAtBefore(LocalDateTime threshold);
}
