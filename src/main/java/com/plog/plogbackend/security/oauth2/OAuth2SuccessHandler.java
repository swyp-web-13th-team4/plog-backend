package com.plog.plogbackend.security.oauth2;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.MemberRepository;
import com.plog.plogbackend.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** 카카오 인증 성공 후 실행되는 작업 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Value("${spring.security.front.frontend-url}")
  private String frontendUrl;

  private final MemberRepository memberRepository;
  private final JwtProvider jwtProvider; // JWT 생성 유틸리티 클래스 (가정)

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    // 1. 카카오 고유 ID 추출
    String providerId = "kakao_" + oAuth2User.getAttributes().get("id");

    // 2. DB에서 회원 조회 - 기존 사용자 인지 아니면 회원가입자 인지 판단하기위한 멤버
    Optional<Member> memberOpt = memberRepository.findByProviderId(providerId);

    if (memberOpt.isPresent()) {
      // ==========================================
      // [기존 회원] -> 일반 로그인 처리
      // ==========================================
      Member member = memberOpt.get();
      String accessToken = jwtProvider.createAccessToken(member.getMemberKey());

      // 프론트엔드의 메인 페이지(또는 로그인 성공 처리 페이지)로 리다이렉트
      String redirectUrl = frontendUrl + "/success.html?token=" + accessToken; // 로컬 HTML 테스트 전용 경로
      //      String redirectUrl = frontendUrl+ "/login/success?token=" + accessToken; // 실 서비스 경로
      getRedirectStrategy().sendRedirect(request, response, redirectUrl);

    } else {
      // ==========================================
      // [신규 회원] -> 임시 가입 토큰 발급 및 가입창으로 이동
      // ==========================================
      // 카카오 ID를 담은 유효기간 15분 임시 토큰 생성
      String registerToken = jwtProvider.createRegisterToken(providerId);

      // 프론트엔드 추가 정보 입력(회원가입) 페이지로 리다이렉트
      String redirectUrl =
          frontendUrl + "/signup.html?registerToken=" + registerToken; // 로컬 HTML 테스트 전용 경로
      //      String redirectUrl = frontendUrl+"/signup?registerToken=" + registerToken; // 실 서비스 경로
      getRedirectStrategy().sendRedirect(request, response, redirectUrl); // 리다이렉트
    }
  }
}
