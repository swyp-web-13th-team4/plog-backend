package com.plog.plogbackend.domain.notification.entity;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.notification.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림창에 표시되는 통합 알림 엔티티.
 *
 * <p>삭제 시 하드 딜리트를 적용합니다. 따라서 {@code BaseTimeStatusEntity}를 상속받지 않고 {@code createdAt}만 독자적으로 관리합니다.
 *
 * <p>30일이 지난 알림은 {@code NotificationCleanupService}에 의해 자동으로 삭제됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 알림을 받을 사용자 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  private Member receiver;

  /** 알림 종류 */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NotificationType type;

  /** 알림 내용 (예: "OOO님이 게시글에 좋아요를 눌렀습니다.") */
  @Column(nullable = false, length = 255)
  private String content;

  /** 클릭 시 이동할 URL 또는 리소스 경로 (예: "/post/56") */
  @Column(length = 500)
  private String relatedUrl;

  /** 읽음 여부 (안 읽은 알림 뱃지 표시용) */
  @Column(nullable = false)
  private boolean isRead = false;

  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  private Notification(Member receiver, NotificationType type, String content, String relatedUrl) {
    this.receiver = receiver;
    this.type = type;
    this.content = content;
    this.relatedUrl = relatedUrl;
    this.createdAt = LocalDateTime.now();
  }

  public static Notification create(
      Member receiver, NotificationType type, String content, String relatedUrl) {
    return new Notification(receiver, type, content, relatedUrl);
  }

  /** 알림을 읽음 상태로 변경합니다. */
  public void markAsRead() {
    this.isRead = true;
  }
}
