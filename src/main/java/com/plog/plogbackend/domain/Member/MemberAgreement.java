package com.plog.plogbackend.domain.Member;

import com.plog.plogbackend.domain.Member.entity.Terms;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_agreement")
public class MemberAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms; // 동의한 약관

    @Column(nullable = false)
    private boolean isAgreed;

    private LocalDateTime agreedAt;

    @Builder
    public MemberAgreement(Member member, Terms terms, boolean isAgreed) {
        this.member = member;
        this.terms = terms;
        this.isAgreed = isAgreed;
        this.agreedAt = isAgreed ? LocalDateTime.now() : null;
    }
}
