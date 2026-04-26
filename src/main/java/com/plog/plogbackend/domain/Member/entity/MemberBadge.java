package com.plog.plogbackend.domain.Member.entity;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = { // 이벤트 프로그래밍 멱등성 때문에 있어야함
        @UniqueConstraint(columnNames = {"member_id", "badge_id"})
})
public class MemberBadge extends BaseTimeStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Builder
    public MemberBadge(Member member, Badge badge) {
        this.member = member;
        this.badge = badge;
    }

}