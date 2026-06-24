package com.plog.plogbackend.domain.notification.entity;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개별 알림 타입별 ON/OFF를 관리하는 엔티티.
 *
 * <p>(member_id, notification_type) 조합을 유니크 키로 가집니다.
 *
 * <h3>확장성</h3>
 *
 * <p>{@link NotificationType}에 새 상수를 추가하면 이 테이블에 자동으로 행이 추가(또는 조회 시 기본값 true 적용) 되므로, DB 스키마 변경 없이
 * 새로운 알림 종류를 지원할 수 있습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "notification_type_setting",
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "notification_type"}))
public class NotificationTypeSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false, length = 30)
  private NotificationType notificationType;

  @Column(nullable = false)
  private boolean enabled = true;

  private NotificationTypeSetting(Member member, NotificationType notificationType) {
    this.member = member;
    this.notificationType = notificationType;
    this.enabled = true;
  }

  public static NotificationTypeSetting createDefault(
      Member member, NotificationType notificationType) {
    return new NotificationTypeSetting(member, notificationType);
  }

  public void updateEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
