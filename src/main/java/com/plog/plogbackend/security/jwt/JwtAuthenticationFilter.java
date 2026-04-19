package com.plog.plogbackend.security.jwt;

import com.plog.plogbackend.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String accessToken = CookieUtil.getCookieValue(request, "accessToken");
    String refreshToken = CookieUtil.getCookieValue(request, "refreshToken");

    // Case 1: Access Token이 유효한 경우 → 정상 인증 처리
    if (accessToken != null
        && jwtProvider.isValidToken(accessToken)
        && jwtProvider.isAccessToken(accessToken)) {
      setAuthentication(accessToken, request);
      filterChain.doFilter(request, response);
      return;
    }

    // Case 2: Access Token이 만료되었고, Refresh Token이 존재하는 경우 → 자동 갱신
    if (accessToken != null
        && jwtProvider.isExpiredAccessToken(accessToken)
        && refreshToken != null) {
      try {
        RefreshTokenService.TokenPair tokenPair =
            refreshTokenService.refreshAccessToken(refreshToken);

        // 새 Access Token 쿠키 설정
        ResponseCookie newAccessCookie =
            cookieUtil.createCookie(
                "accessToken", tokenPair.accessToken(), jwtProvider.getAccessTokenValidityInMs());
        response.addHeader(HttpHeaders.SET_COOKIE, newAccessCookie.toString());

        // 새 Refresh Token 쿠키 설정 (Rotation)
        ResponseCookie newRefreshCookie =
            cookieUtil.createCookie(
                "refreshToken",
                tokenPair.refreshToken(),
                jwtProvider.getRefreshTokenValidityInMs());
        response.addHeader(HttpHeaders.SET_COOKIE, newRefreshCookie.toString());

        // 새 Access Token으로 인증 설정
        setAuthentication(tokenPair.accessToken(), request);
        log.debug("Access Token 자동 갱신 완료");
      } catch (Exception e) {
        log.warn("Refresh Token으로 갱신 실패: {}", e.getMessage());
        // 갱신 실패 시 인증 없이 진행 (401 응답은 시큐리티 설정에서 처리)
      }
    }

    filterChain.doFilter(request, response);
  }

  /** SecurityContext에 인증 정보를 설정합니다. */
  private void setAuthentication(String token, HttpServletRequest request) {
    UUID memberKey = jwtProvider.getMemberKeyFromToken(token);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(memberKey, null, Collections.emptyList());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
