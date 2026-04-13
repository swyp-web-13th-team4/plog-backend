package com.plog.plogbackend.domain.Member;

import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAgreement extends BaseTimeStatusEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Terms agreementType; // 예: "MARKETING"

  @Column(nullable = false)
  private boolean isAgreed;

  private LocalDateTime agreedAt;

  @Builder
  public MemberAgreement(Member member, Terms agreementType, boolean isAgreed) {
    this.member = member;
    this.agreementType = agreementType;
    this.isAgreed = isAgreed;
    this.agreedAt = isAgreed ? LocalDateTime.now() : null;
  }
}
