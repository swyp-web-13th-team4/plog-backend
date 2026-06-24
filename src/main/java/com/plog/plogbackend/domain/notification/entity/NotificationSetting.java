package com.plog.plogbackend.domain.notification.entity;

import com.plog.plogbackend.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 전체 알림 ON/OFF를 관리하는 엔티티. Member와 1:1 관계.
 *
 * <p>{@code isAllEnabled}가 {@code false}이면 개별 타입 설정({@link NotificationTypeSetting})과 관계없이 모든 알림이
 * 차단됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false, unique = true)
  private Member member;

  /** 전체 알림 활성화 여부 (최우선 적용). false이면 모든 알림이 차단됩니다. */
  @Column(nullable = false)
  private boolean isAllEnabled = true;

  private NotificationSetting(Member member) {
    this.member = member;
    this.isAllEnabled = true;
  }

  /** 회원가입 시 기본 설정(전체 알림 ON)으로 생성합니다. */
  public static NotificationSetting createDefault(Member member) {
    return new NotificationSetting(member);
  }

  public void updateAllEnabled(boolean enabled) {
    this.isAllEnabled = enabled;
  }
}
