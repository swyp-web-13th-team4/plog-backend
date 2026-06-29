package com.plog.plogbackend.security.error;

import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 인증 실패 (권한 부족) */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Value("${spring.security.front.frontend-url}")
  private String frontendUrl;

  private final JsonMapper jsonMapper;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {

    String requestUri = request.getRequestURI();

    // 어드민 페이지 접근 시 권한 부족 → 어드민 에러 페이지로 리다이렉트
    if (requestUri.startsWith("/admin")) {
      log.warn("어드민 페이지 권한 부족 → 에러 페이지로 리다이렉트: {}", requestUri);
      response.sendRedirect("/admin/error");
      return;
    }

    // API 요청인 경우 기존 JSON 에러 응답
    log.warn("권한이 없는 엔드포인트에 접근했습니다: {}", requestUri);

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json;charset=UTF-8");

    ApiResponse<Void> apiResponse = ApiResponse.error(ErrorType.FAILED_AUTH);
    response.getWriter().write(jsonMapper.writeValueAsString(apiResponse));
  }
}
