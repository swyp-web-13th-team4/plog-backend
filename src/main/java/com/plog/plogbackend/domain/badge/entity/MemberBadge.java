package com.plog.plogbackend.domain.badge.entity;

import com.plog.plogbackend.domain.member.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회원이 획득한 뱃지를 기록하는 중간 테이블 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "member_badge",
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "badge_id"}))
public class MemberBadge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "badge_id", nullable = false)
  private Badge badge;

  @Column(nullable = false)
  private LocalDateTime acquiredAt;

  /**
   * SSE 알림 전송 여부.
   * false: 아직 클라이언트에게 전송되지 않은 상태 (SSE 연결이 없었거나 전송 실패)
   * true : SSE로 정상 전송 완료
   */
  @Column(nullable = false)
  private boolean notified = false;

  private MemberBadge(Member member, Badge badge) {
    this.member = member;
    this.badge = badge;
    this.acquiredAt = LocalDateTime.now();
    this.notified = false;
  }

  public static MemberBadge of(Member member, Badge badge) {
    return new MemberBadge(member, badge);
  }

  /** SSE 알림 전송 완료 처리 */
  public void markNotified() {
    this.notified = true;
  }
}
