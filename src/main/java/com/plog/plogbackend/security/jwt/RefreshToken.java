package com.plog.plogbackend.security.jwt;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** MySQL에 저장되는 Refresh Token 엔티티 */
@Entity
@Table(
    name = "refresh_token",
    indexes = {@Index(name = "idx_refresh_token_member_key", columnList = "memberKey")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 512)
  private String token;

  @Column(nullable = false, columnDefinition = "BINARY(16)")
  private UUID memberKey;

  @Column(nullable = false)
  private LocalDateTime expiryDate;

  @Builder
  private RefreshToken(String token, UUID memberKey, LocalDateTime expiryDate) {
    this.token = token;
    this.memberKey = memberKey;
    this.expiryDate = expiryDate;
  }

  public static RefreshToken create(String token, UUID memberKey, long validityInMs) {
    return RefreshToken.builder()
        .token(token)
        .memberKey(memberKey)
        .expiryDate(LocalDateTime.now().plusNanos(validityInMs * 1_000_000))
        .build();
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiryDate);
  }

  /** 토큰 값 갱신 (Rotation) */
  public void updateToken(String newToken, long validityInMs) {
    this.token = newToken;
    this.expiryDate = LocalDateTime.now().plusNanos(validityInMs * 1_000_000);
  }
}
