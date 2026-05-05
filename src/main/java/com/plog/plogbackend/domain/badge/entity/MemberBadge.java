package com.plog.plogbackend.domain.badge.entity;

import com.plog.plogbackend.domain.Member.Member;
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

  private MemberBadge(Member member, Badge badge) {
    this.member = member;
    this.badge = badge;
    this.acquiredAt = LocalDateTime.now();
  }

  public static MemberBadge of(Member member, Badge badge) {
    return new MemberBadge(member, badge);
  }
}
