package com.plog.plogbackend.domain.Member;

import com.github.f4b6a3.uuid.UuidCreator;
import com.plog.plogbackend.domain.Member.enums.Role;
import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Member PK는 {@code memberKey} 이걸로 쓰시면 됩니다 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeStatusEntity {

  // ==========================================
  // 1. 내부 식별자 (조인, 인덱스 최적화용)
  // ==========================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ==========================================
  // 2. 외부 식별자 (API 통신, 클라이언트 노출용)
  // ==========================================

  @Column(nullable = false, unique = true, updatable = false, columnDefinition = "BINARY(16)")
  private UUID memberKey;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(length = 500)
  private String profileImage;

  @Column(nullable = false, unique = true)
  private String providerId; // 카카오 소셜 로그인 식별키

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(length = 130)
  private String introduction;

  // 추가
  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
  private List<BookMark> bookMarks = new ArrayList<>();

  // ==========================================
  // 3. 빌더
  // ==========================================

  @Builder
  private Member(
      UUID memberKey, String providerId, String nickname, String profileImage, Role role) {
    this.memberKey = memberKey;
    this.providerId = providerId;
    this.nickname = nickname;
    this.profileImage = profileImage;
    this.role = role;
  }

  // ==========================================
  // 4. 정적 팩토리 메서드
  // ==========================================

  public static Member createNewMember(String nickname, String providerId, String profileImage) {
    return Member.builder()
        .memberKey(UuidCreator.getTimeOrderedEpoch())
        .providerId(providerId)
        .nickname(nickname)
        .profileImage(profileImage)
        .role(Role.ROLE_USER)
        .build();
  }

  // ==========================================
  // 5. 비즈니스 메서드
  // ==========================================

  /** 프로필 이미지 URL 업데이트 */
  public void updateProfileImage(String imageUrl) {
    this.profileImage = imageUrl;
  }

  /** 닉네임 업데이트 */
  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  /** 프로필(닉네임 + 이미지 + 소개글)을 한 번에 업데이트합니다. null인 값은 변경하지 않습니다. */
  public void updateProfile(String nickname, String imageUrl, String introduction) {
    if (nickname != null && !nickname.isBlank()) {
      this.nickname = nickname;
    }
    if (imageUrl != null && !imageUrl.isBlank()) {
      this.profileImage = imageUrl;
    }
    if (introduction != null) {
      this.introduction = introduction;
    }
  }
}
