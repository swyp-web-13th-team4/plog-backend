package com.plog.plogbackend.domain.Member;

import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeStatusEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String nickname; // 인증후, 사용자에게 직접 입력받음

  @Column(length = 500)
  private String profileImageUrl; // 이미지 파일 경로와 파일명

  @Enumerated(EnumType.STRING)
  private Role role;

  @Enumerated(EnumType.STRING)
  private Status status; // soft delete

  // 회원이 삭제되면 동의 내역도 함께 삭제되도록 설정 (Cascade)
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberAgreement> agreements = new ArrayList<>();

  @Builder
  public Member(
      String nickname,
      String profileImageUrl,
      Role role) { // createdAt - 생성 시각은 NULL 이었다가 DB 저장 시점에 자동기록
    this.nickname = nickname;
    this.profileImageUrl = profileImageUrl;
    this.role = role != null ? role : Role.ROLE_USER;
    this.status = Status.ACTIVE;
  }
}
