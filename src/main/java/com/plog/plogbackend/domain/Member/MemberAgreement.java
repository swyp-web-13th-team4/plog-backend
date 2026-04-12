package com.plog.plogbackend.domain.Member;

import com.plog.plogbackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String agreementType; // 예: "MARKETING"

    @Column(nullable = false)
    private boolean isAgreed;

    private LocalDateTime agreedAt;

    @Builder
    public MemberAgreement(Member member, String agreementType, boolean isAgreed) {
        this.member = member;
        this.agreementType = agreementType;
        this.isAgreed = isAgreed;
        this.agreedAt = isAgreed ? LocalDateTime.now() : null;
    }
}