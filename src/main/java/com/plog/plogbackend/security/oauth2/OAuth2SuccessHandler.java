package com.plog.plogbackend.security.oauth2;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.enums.Role;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.global.util.CookieUtil;
import com.plog.plogbackend.security.jwt.JwtProvider;
import com.plog.plogbackend.security.jwt.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

/** 카카오 인증 성공 후 실행되는 작업 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Value("${spring.security.front.frontend-url}")
  private String frontendUrl;

  private final MemberRepository memberRepository;
  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

  private final RequestCache requestCache = new HttpSessionRequestCache();

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    // 1. 카카오 고유 ID 추출
    String providerId = "kakao_" + oAuth2User.getAttributes().get("id");

    // 2. SavedRequest 확인 (로그인 전 요청 URL)
    SavedRequest savedRequest = requestCache.getRequest(request, response);
    boolean isAdminLogin = false;
    if (savedRequest != null) {
      String savedUrl = savedRequest.getRedirectUrl();
      isAdminLogin = savedUrl.contains("/admin");
      // 사용한 SavedRequest 제거
      requestCache.removeRequest(request, response);
    }

    // 3. DB에서 회원 조회
    Optional<Member> memberOpt = memberRepository.findByProviderId(providerId);

    if (memberOpt.isPresent()) {
      Member member = memberOpt.get();

      // ==========================================
      // [기존 회원] → JWT 발급 후 분기 리다이렉트
      // ==========================================

      // JWT 쿠키 발급 (어드민, 앱 모두 동일)
      String accessToken = jwtProvider.createAccessToken(member.getMemberKey());
      ResponseCookie accessCookie =
          cookieUtil.createCookie(
              "accessToken", accessToken, jwtProvider.getAccessTokenValidityInMs());
      response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

      String refreshToken = refreshTokenService.createRefreshToken(member.getMemberKey());
      ResponseCookie refreshCookie =
          cookieUtil.createCookie(
              "refreshToken", refreshToken, jwtProvider.getRefreshTokenValidityInMs());
      response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

      if (isAdminLogin) {
        // 어드민 로그인 흐름: 역할 확인 후 분기
        if (member.getRole() == Role.ROLE_ADMIN) {
          log.info("어드민 로그인 성공: {}", member.getMemberKey());
          getRedirectStrategy().sendRedirect(request, response, "/admin/notice");
        } else {
          log.warn("어드민 권한 없는 회원의 어드민 로그인 시도: {}", member.getMemberKey());
          getRedirectStrategy().sendRedirect(request, response, "/admin/error");
        }
      } else {
        // 일반 앱 로그인 흐름: 기존 로직 유지
        String redirectUrl =
            frontendUrl
                + "/api/auth/callback"
                + "?accessToken="
                + accessToken
                + "&refreshToken="
                + refreshToken;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
      }

    } else {
      // ==========================================
      // [신규 회원] → 어드민이면 프론트로, 앱이면 가입 페이지로
      // ==========================================

      if (isAdminLogin) {
        // 어드민 경로로 접근한 신규 유저 → 에러 페이지로 리다이렉트
        log.warn("어드민 경로로 접근한 미가입 유저 → 에러 페이지로 리다이렉트");
        getRedirectStrategy().sendRedirect(request, response, "/admin/error");
      } else {
        // 기존 회원가입 흐름 유지
        String registerToken = jwtProvider.createRegisterToken(providerId);
        ResponseCookie cookie2 =
            cookieUtil.createCookie(
                "registerToken", registerToken, jwtProvider.getRegisterTokenValidityInMs());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie2.toString());

        String redirectUrl =
            frontendUrl + "/api/auth/callback" + "?registerToken=" + registerToken;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
      }
    }
  }
}
