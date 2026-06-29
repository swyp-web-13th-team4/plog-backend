package com.plog.plogbackend.security.error;

import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 미인증 상태 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Value("${spring.security.front.frontend-url}")
  private String frontendUrl;

  private final JsonMapper jsonMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    String requestUri = request.getRequestURI();

    // 어드민 페이지 요청인 경우 카카오 소셜 로그인으로 리다이렉트
    // ExceptionTranslationFilter가 이미 SavedRequest를 세션에 저장하므로
    // 로그인 성공 후 원래 요청 URL로 복원 가능
    if (requestUri.startsWith("/admin")) {
      log.debug("어드민 페이지 미인증 접근 → 카카오 로그인으로 리다이렉트: {}", requestUri);
      response.sendRedirect("/oauth2/authorization/kakao");
      return;
    }

    // API 요청인 경우 기존 JSON 에러 응답
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");

    ApiResponse<Void> apiResponse = ApiResponse.error(ErrorType.REQUIRED_AUTH);
    response.getWriter().write(jsonMapper.writeValueAsString(apiResponse));
  }
}
