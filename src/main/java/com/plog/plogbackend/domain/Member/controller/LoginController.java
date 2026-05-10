package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.request.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.service.MemberService;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.util.CookieUtil;
import com.plog.plogbackend.security.jwt.JwtProvider;
import com.plog.plogbackend.security.jwt.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "로그인/회원가입", description = "회원가입, 로그아웃 관련 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class LoginController {

  private final MemberService memberService;
  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

  @Operation(
      summary = "회원가입(카카오 인증 후)",
      description = "닉네임, 약관동의, 프로필 이미지(직접 업로드 또는 기본 이미지 URL 선택)를 받아 가입 완료 (멀티파트 폼 데이터)")
  @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<java.util.Map<String, String>>> signup(
      @CookieValue(value = "registerToken", required = false) String cookieToken,
      @RequestParam(value = "registerToken", required = false) String paramToken,
      @Valid @RequestPart("request") MemberSignupRequest request,
      @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
      @RequestParam(value = "defaultImageId", required = false) Long defaultImageId,
      HttpServletResponse response) {

    String registerToken = cookieToken != null ? cookieToken : paramToken;
    if (registerToken == null) {
      throw new AppException(ErrorType.INVALID_AUTH_HEADER); // token not found
    }

    // 1. 회원 저장 후 memberKey 반환
    UUID memberKey = memberService.signup(registerToken, request, profileImage, defaultImageId);

    // 2. 컨트롤러에서 accessToken 생성 후 쿠키에 담음
    String accessToken = jwtProvider.createAccessToken(memberKey);
    ResponseCookie accessCookie =
        cookieUtil.createCookie(
            "accessToken", accessToken, jwtProvider.getAccessTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

    // 3. Refresh Token 발급 및 DB 저장 후 쿠키에 담음
    String refreshToken = refreshTokenService.createRefreshToken(memberKey);
    ResponseCookie refreshCookie =
        cookieUtil.createCookie(
            "refreshToken", refreshToken, jwtProvider.getRefreshTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    // 4. 사용이 끝난 registerToken 쿠키 삭제 (즉시 만료)
    ResponseCookie deleteRegisterCookie = cookieUtil.deleteCookie("registerToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteRegisterCookie.toString());

    java.util.Map<String, String> tokens = new java.util.HashMap<>();
    tokens.put("accessToken", accessToken);
    tokens.put("refreshToken", refreshToken);

    return ResponseEntity.ok(ApiResponse.success(tokens));
  }

  @Operation(
      summary = "로그아웃",
      description = "accessToken, refreshToken 쿠키를 만료시키고 DB에서 refreshToken을 삭제하여 로그아웃 처리")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      org.springframework.security.core.Authentication authentication,
      HttpServletResponse response) {

    // DB에서 refresh token 삭제
    if (authentication != null && authentication.getPrincipal() instanceof UUID memberKey) {
      refreshTokenService.deleteRefreshToken(memberKey);
    }

    // 쿠키 삭제
    ResponseCookie deleteAccessCookie = cookieUtil.deleteCookie("accessToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());

    ResponseCookie deleteRefreshCookie = cookieUtil.deleteCookie("refreshToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

    return ResponseEntity.ok(ApiResponse.success());
  }
}
