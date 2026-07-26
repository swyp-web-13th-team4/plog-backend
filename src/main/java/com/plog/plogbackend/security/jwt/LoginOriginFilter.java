package com.plog.plogbackend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginOriginFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (request.getRequestURI().startsWith("/oauth2/authorization/kakao")) {
      String redirect = request.getParameter("redirect"); // "admin" or "user"
      if (redirect != null) {
        request.getSession().setAttribute("LOGIN_ORIGIN", redirect);
      }
    }
    filterChain.doFilter(request, response);
  }
}
