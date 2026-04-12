package com.plog.plogbackend.domain.Member;

import com.plog.plogbackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname; // 인증후, 사용자에게 직접 입력받음

    @Column(length = 500)
    private String profileImageUrl; // 이미지 파일 경로와 파일명

    @Enumerated(EnumType.STRING)
    private Role role;

    private String provider; // 다른 소셜 로그인 화장을 위한 변수

    @Enumerated(EnumType.STRING)
    private MemberStatus status; // soft delete

    // 회원이 삭제되면 동의 내역도 함께 삭제되도록 설정 (Cascade)
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAgreement> agreements = new ArrayList<>();

    @Builder
    public Member(String nickname, String profileImageUrl, Role role, String provider) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = role != null ? role : Role.ROLE_USER;
        this.provider = provider;
        this.status = MemberStatus.ACTIVE;
    }
}
