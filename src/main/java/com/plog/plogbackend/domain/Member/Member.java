package com.plog.plogbackend.domain.Member;

import com.github.f4b6a3.uuid.UuidCreator;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
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

  // 기본 프로필 이미지
  private static final String DEFAULT_PROFILE_URL =
      "https://your-domain.com/images/default-profile.png"; // 예시 주소

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
  private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  private Role role;

  // ==========================================
  // 3. 빌더
  // ==========================================
  @Builder
  private Member(UUID memberKey, String nickname, String profileImageUrl, Role role) {
    this.memberKey = memberKey;
    this.nickname = nickname;
    this.profileImageUrl = profileImageUrl;
    this.role = role;
  }

  // ==========================================
  // 4. 정적 팩토리 메서드 (안전한 객체 생성 보장)
  // ==========================================

  /** 디폴트 생성자 */
  public static Member createNewMember(String nickname, String profileImageUrl) {
    return Member.builder()
        .memberKey(UuidCreator.getTimeOrderedEpoch())
        .nickname(nickname)
        .profileImageUrl(getOrDefaultImage(profileImageUrl))
        .role(Role.ROLE_USER)
        .build();
  }

  /** 카카오 등 소셜 로그인 최초 접근 시 사용 (GUEST 권한 부여) */
  public static Member createGuest(String temporaryNickname) {
    return Member.builder()
        .memberKey(UuidCreator.getTimeOrderedEpoch())
        .nickname(temporaryNickname) // 카카오 소셜 로그인 응답으로 오는 식별자 id 값을 임시로 넣음.
        .role(Role.ROLE_GUEST) // 추가 정보 입력 전까지 사용할 권한
        .build();
  }

  // ==========================================
  // 5. 비즈니스 메서드
  // ==========================================

  /** 소셜 로그인 후 추가 정보 입력 시 정회원(USER)으로 승격 */
  public void completeSignUp(String nickname, String profileImageUrl) {
    this.nickname = nickname; // 시용자에게 직접 입력받음.
    this.profileImageUrl = getOrDefaultImage(profileImageUrl); // 시용자에게 직접 입력받음.
    this.role = Role.ROLE_USER; // 추가정보 입력후 정회원 승격
  }

  /** 기본 프로필 이미지 설정 */
  private static String getOrDefaultImage(String imageUrl) {
    // String이 null이거나 공백("")인 경우 기본 이미지 반환
    if (imageUrl == null || imageUrl.isBlank()) {
      return DEFAULT_PROFILE_URL;
    }
    return imageUrl;
  }
}
