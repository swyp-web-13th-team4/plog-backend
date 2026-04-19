package com.plog.plogbackend.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

  @Value("${cookie.secure:true}")
  private boolean secureCookie;

  @Value("${cookie.sameSite:None}")
  private String sameSite;

  public ResponseCookie createCookie(String name, String value, long maxAgeMs) {
    return ResponseCookie.from(name, value)
        .path("/")
        .httpOnly(true)
        .secure(secureCookie)
        .sameSite(sameSite) // 테스트용 HTTP 환경에서는 SameSite=None 사용 불가
        .maxAge(maxAgeMs / 1000) // ms를 초 단위로 변환
        .build();
  }

  public ResponseCookie deleteCookie(String name) {
    return ResponseCookie.from(name, "")
        .path("/")
        .httpOnly(true)
        .secure(secureCookie)
        .sameSite(sameSite)
        .maxAge(0) // 쿠키 즉시 만료
        .build();
  }

  /** 토큰 쿠키를 response에 직접 추가합니다. */
  public void addCookie(HttpServletResponse response, String name, String value, long maxAgeMs) {
    response.addHeader(HttpHeaders.SET_COOKIE, createCookie(name, value, maxAgeMs).toString());
  }

  /** 쿠키를 response에서 즉시 만료시킵니다. */
  public void expireCookie(HttpServletResponse response, String name) {
    response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie(name).toString());
  }

  public static String getCookieValue(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
