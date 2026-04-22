package com.plog.plogbackend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

  @Value("${jwt.secret}")
  private String secretKey;

  @Getter
  @Value("${jwt.access-token-validity-in-ms}")
  private long accessTokenValidityInMs;

  @Getter
  @Value("${jwt.register-token-validity-in-ms}")
  private long registerTokenValidityInMs;

  @Getter
  @Value("${jwt.refresh-token-validity-in-ms}")
  private long refreshTokenValidityInMs;

  private SecretKey key; // Member UUID 식별키

  @PostConstruct // yml 에 저장된 JWT 토큰 시크릿 키를 메모리에 적제 (성능 향상 목적)
  public void init() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  // ==========================================
  // 1. 토큰 생성 (회원가입 전용 임시 토큰 , 회원 토큰 , 리프레시 토큰)
  // ==========================================

  public String createRegisterToken(String providerId) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + registerTokenValidityInMs);
    return Jwts.builder()
        .subject("REGISTER")
        .claim("providerId", providerId)
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
  }

  public String createAccessToken(UUID memberKey) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + accessTokenValidityInMs);
    return Jwts.builder()
        .subject("ACCESS")
        .claim("memberKey", memberKey.toString()) // 회원 식별자는 ID 가 아니라 memberKey로 결정
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
  }

  public String createRefreshToken(UUID memberKey) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + refreshTokenValidityInMs);
    return Jwts.builder()
        .subject("REFRESH")
        .claim("memberKey", memberKey.toString())
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
  }

  // ==========================================
  // 2. 검증 메서드 (JWT 토큰 검증 , 일반/임시/리프레시 토큰 검증)
  // ==========================================

  public boolean isValidToken(String token) { // JWT 토큰 검증
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      log.warn("JWT 검증 실패: {}", e.getMessage());
      return false;
    }
  }

  /** JWT 토큰이 만료되었는지 확인 (서명은 유효하지만 만료된 경우 true) */
  public boolean isExpiredToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return false; // 정상 파싱되면 만료되지 않음
    } catch (ExpiredJwtException e) {
      return true; // 만료된 경우
    } catch (Exception e) {
      return false; // 만료가 아닌 다른 에러 (서명 위조 등)
    }
  }

  public boolean isAccessToken(String token) { // 임시 JWT 토큰이 아니라 일반 JWT 액세스 토큰인지 검사
    String subject =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    return "ACCESS".equals(subject);
  }

  public boolean isRegisterToken(String token) { // 회원가입 전용 임시 JWT 인지 검사
    String subject =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    return "REGISTER".equals(subject);
  }

  /** 만료된 토큰에서도 subject를 읽어 ACCESS 토큰인지 확인 */
  public boolean isExpiredAccessToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return false; // 만료되지 않았으면 false
    } catch (ExpiredJwtException e) {
      return "ACCESS".equals(e.getClaims().getSubject());
    } catch (Exception e) {
      return false;
    }
  }

  // ==========================================
  // 3. 식별키 관련 메서드 (카카오 식별키 , 회원 UUID 식별키)
  // ==========================================

  public String getProviderIdFromToken(String token) { // 카카오 소셜 인증후 발급되는 카카오 회원 식별키를 가져오는 메서드
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return claims.get("providerId", String.class);
  }

  public UUID getMemberKeyFromToken(String token) { // JWT 토큰에서 memberkey(UUID) 가져오는 메서드
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    String memberKeyString = claims.get("memberKey", String.class);
    return UUID.fromString(memberKeyString);
  }
}
