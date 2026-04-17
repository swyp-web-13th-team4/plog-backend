package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.service.MemberService;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.util.CookieUtil;
import com.plog.plogbackend.security.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "로그인 인증/인가")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final JwtProvider jwtProvider;
  private final CookieUtil cookieUtil;

  @Operation(summary = "회원가입(카카오 인증 후)", description = "추가 정보 입력 후 가입 완료")
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<Void>> signup(
      @CookieValue(value = "registerToken", required = false) String registerToken,
      @Valid @RequestBody MemberSignupRequest request,
      HttpServletResponse response) {

    if (registerToken == null) {
      throw new AppException(ErrorType.INVALID_AUTH_HEADER); // token not found
    }

    // 1. 회원 저장 후 memberKey 반환 (accessToken은 서비스 레이어에서 생성하지 않음)
    UUID memberKey = memberService.signup(registerToken, request);

    // 2. 컨트롤러에서 accessToken 생성 후 쿠키에 담음
    String accessToken = jwtProvider.createAccessToken(memberKey);
    ResponseCookie accessCookie =
        cookieUtil.createCookie(
            "accessToken", accessToken, jwtProvider.getAccessTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

    // 3. 사용이 끝난 registerToken 쿠키 삭제 (즉시 만료)
    ResponseCookie deleteRegisterCookie = cookieUtil.deleteCookie("registerToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteRegisterCookie.toString());

    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(summary = "로그아웃", description = "accessToken 쿠키를 만료시켜 로그아웃 처리")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
    ResponseCookie deleteCookie = cookieUtil.deleteCookie("accessToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    return ResponseEntity.ok(ApiResponse.success());
  }
}
