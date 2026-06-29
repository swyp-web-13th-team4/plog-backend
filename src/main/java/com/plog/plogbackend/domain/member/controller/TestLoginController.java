package com.plog.plogbackend.domain.member.controller;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.enums.Role;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.util.CookieUtil;
import com.plog.plogbackend.security.jwt.JwtProvider;
import com.plog.plogbackend.security.jwt.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/testlogin")
@RequiredArgsConstructor
public class TestLoginController {

  private final MemberRepository memberRepository;
  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

  private final org.springframework.security.web.savedrequest.RequestCache requestCache = new org.springframework.security.web.savedrequest.HttpSessionRequestCache();

  /**
   * 로컬 테스트 로그인 진입점.
   * 카카오 로그인으로 명시적 리다이렉트하며, 돌아올 때 이 경로를 알 수 있도록 RequestCache에 저장합니다.
   */
  @GetMapping
  public String testLoginEntry(HttpServletRequest request, HttpServletResponse response) {
    // 이미 로그인된 사용자인지 확인하는 것도 가능하지만, 테스트용이므로 무조건 덮어쓸 수 있도록 리다이렉트합니다.
    requestCache.saveRequest(request, response);
    return "redirect:/oauth2/authorization/kakao";
  }

  /** 테스트 전용 가입 페이지 (템플릿 렌더링) */
  @GetMapping("/signup")
  public String testSignupPage() {
    return "admin/test_signup";
  }

  /** 테스트 전용 가입 폼 제출 (폼 데이터) */
  @PostMapping("/signup")
  public String testSignupProcess(
      @RequestParam("nickname") String nickname,
      @CookieValue(value = "registerToken", required = false) String cookieToken,
      HttpServletResponse response) {

    if (cookieToken == null) {
      throw new AppException(ErrorType.INVALID_AUTH_HEADER);
    }

    // registerToken 검증 및 카카오 providerId 추출
    String providerId;
    try {
      providerId = jwtProvider.getProviderIdFromToken(cookieToken);
    } catch (Exception e) {
      throw new AppException(ErrorType.INVALID_AUTH_HEADER);
    }

    // 1. 기존 가입자인지 방어 로직 (이미 있으면 그대로 토큰만 발급할 수도 있으나, 여기선 오류 방지 차원)
    Member member = memberRepository.findByProviderId(providerId).orElse(null);
    if (member == null) {
      // 2. 어드민(ROLE_ADMIN) 권한으로 Member 엔티티 강제 생성 및 저장
      member = Member.builder()
          .memberKey(UUID.randomUUID())
          .providerId(providerId)
          .nickname(nickname)
          .introduction("로컬 테스트용 관리자 계정입니다.")
          .role(Role.ROLE_ADMIN)
          .build();
      memberRepository.save(member);
      log.info("로컬 테스트용 어드민 계정 생성 완료: {}", member.getMemberKey());
    }

    // 3. 생성(조회)된 멤버로 JWT 토큰 발급 및 쿠키 저장
    String accessToken = jwtProvider.createAccessToken(member.getMemberKey());
    ResponseCookie accessCookie =
        cookieUtil.createCookie("accessToken", accessToken, jwtProvider.getAccessTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

    String refreshToken = refreshTokenService.createRefreshToken(member.getMemberKey());
    ResponseCookie refreshCookie =
        cookieUtil.createCookie("refreshToken", refreshToken, jwtProvider.getRefreshTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    // 4. registerToken 쿠키 삭제
    ResponseCookie deleteRegisterCookie = cookieUtil.deleteCookie("registerToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteRegisterCookie.toString());

    return "redirect:/admin/notice";
  }
}
