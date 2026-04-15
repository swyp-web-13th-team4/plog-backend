package com.plog.plogbackend.domain.member;

import com.plog.plogbackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAgreement extends BaseTimeEntity { // ← BaseTimeStatusEntity 대신 BaseTimeEntity

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Terms agreementType;

  @Column(nullable = false)
  private boolean agreed; // isAgreed → agreed (Lombok 호환성 개선)

  // agreedAt 필드 제거 → createdAt(부모)이 그 역할을 대신

  @Builder
  public MemberAgreement(Member member, Terms agreementType, boolean agreed) {
    this.member = member;
    this.agreementType = agreementType;
    this.agreed = agreed;
  }
}
