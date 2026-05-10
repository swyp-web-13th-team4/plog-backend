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

    // 클라이언트의 요청이 API(JSON)를 기대하는지 확인
    String acceptHeader = request.getHeader("Accept");
    String requestedWithHeader = request.getHeader("X-Requested-With");
    boolean isAjaxRequest = (acceptHeader != null && acceptHeader.contains("application/json"))
            || "XMLHttpRequest".equals(requestedWithHeader)
            || request.getRequestURI().startsWith("/api/"); // API 경로는 무조건 AJAX로 간주

    if (isAjaxRequest) {
      // 프론트엔드의 axios/fetch 등 API 요청인 경우 401 JSON 응답 반환 (리다이렉트 금지!)
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");
      ApiResponse<Void> apiResponse = ApiResponse.error(ErrorType.REQUIRED_AUTH);
      response.getWriter().write(jsonMapper.writeValueAsString(apiResponse));
    } else {
      // 일반 브라우저 접근인 경우에만 프론트엔드 로그인 페이지로 리다이렉트
      response.sendRedirect(frontendUrl + "/login");
    }
  }
}
