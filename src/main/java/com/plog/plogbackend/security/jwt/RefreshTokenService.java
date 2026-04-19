package com.plog.plogbackend.security.jwt;

import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh Token 생성·검증·갱신·삭제를 담당하는 서비스
 *
 * <p>Refresh Token Rotation 전략을 사용하여 보안을 강화합니다. - 새 access token을 발급할 때마다 refresh token도 함께 교체 -
 * 탈취된 refresh token의 재사용을 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProvider jwtProvider;

  // ==========================================
  // 1. Refresh Token 발급 (로그인/회원가입 시)
  // ==========================================

  /** 회원의 refresh token을 생성하고 DB에 저장합니다. 기존 토큰이 있으면 갱신(Rotation)합니다. */
  @Transactional
  public String createRefreshToken(UUID memberKey) {
    String tokenValue = jwtProvider.createRefreshToken(memberKey);
    long validityInMs = jwtProvider.getRefreshTokenValidityInMs();

    Optional<RefreshToken> existing = refreshTokenRepository.findByMemberKey(memberKey);

    if (existing.isPresent()) {
      // 기존 토큰이 있으면 값만 교체 (Rotation)
      existing.get().updateToken(tokenValue, validityInMs);
    } else {
      // 최초 발급
      RefreshToken refreshToken = RefreshToken.create(tokenValue, memberKey, validityInMs);
      refreshTokenRepository.save(refreshToken);
    }

    return tokenValue;
  }

  // ==========================================
  // 2. Refresh Token 검증 및 Access Token 재발급
  // ==========================================

  /**
   * Refresh Token으로 새 Access Token + 새 Refresh Token을 발급합니다.
   *
   * @return 갱신된 TokenPair (accessToken + refreshToken)
   */
  @Transactional
  public TokenPair refreshAccessToken(String refreshTokenValue) {
    // 1) JWT 서명 검증
    if (!jwtProvider.isValidToken(refreshTokenValue)) {
      throw new AppException(ErrorType.INVALID_REFRESH_TOKEN);
    }

    // 2) DB에서 토큰 조회
    RefreshToken storedToken =
        refreshTokenRepository
            .findByToken(refreshTokenValue)
            .orElseThrow(() -> new AppException(ErrorType.INVALID_REFRESH_TOKEN));

    // 3) 만료 여부 확인
    if (storedToken.isExpired()) {
      refreshTokenRepository.delete(storedToken);
      throw new AppException(ErrorType.EXPIRED_REFRESH_TOKEN);
    }

    // 4) 새 Access Token 발급
    UUID memberKey = storedToken.getMemberKey();
    String newAccessToken = jwtProvider.createAccessToken(memberKey);

    // 5) Refresh Token Rotation - 새 refresh token으로 교체
    String newRefreshToken = jwtProvider.createRefreshToken(memberKey);
    storedToken.updateToken(newRefreshToken, jwtProvider.getRefreshTokenValidityInMs());

    return new TokenPair(newAccessToken, newRefreshToken);
  }

  // ==========================================
  // 3. Refresh Token 삭제 (로그아웃 시)
  // ==========================================

  /** 회원의 refresh token을 DB에서 삭제합니다. (로그아웃) */
  @Transactional
  public void deleteRefreshToken(UUID memberKey) {
    refreshTokenRepository.deleteByMemberKey(memberKey);
  }

  /** Access Token + Refresh Token 쌍 */
  public record TokenPair(String accessToken, String refreshToken) {}
}
