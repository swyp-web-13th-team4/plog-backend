package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.service.MemberImageService;
import com.plog.plogbackend.domain.Member.service.MemberService;
import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.util.CookieUtil;
import com.plog.plogbackend.security.jwt.JwtProvider;
import com.plog.plogbackend.security.jwt.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원", description = "로그인 인증/인가 , 로그아웃")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final MemberImageService memberImageService;
  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

  @Operation(
      summary = "회원가입(카카오 인증 후)",
      description = "닉네임, 약관동의, 프로필 이미지(선택)를 받아 가입 완료 (멀티파트 폼 데이터)")
  @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<Void>> signup(
      @CookieValue(value = "registerToken", required = false) String registerToken,
      @Valid @RequestPart("request") MemberSignupRequest request,
      @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
      HttpServletResponse response) {

    if (registerToken == null) {
      throw new AppException(ErrorType.INVALID_AUTH_HEADER); // token not found
    }

    // 1. 회원 저장 후 memberKey 반환
    UUID memberKey = memberService.signup(registerToken, request, profileImage);

    // 2. 컨트롤러에서 accessToken 생성 후 쿠키에 담음
    String accessToken = jwtProvider.createAccessToken(memberKey);
    ResponseCookie accessCookie =
        cookieUtil.createCookie(
            "accessToken", accessToken, jwtProvider.getAccessTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

    // 3. Refresh Token 발급 및 DB 저장
    String refreshTokenValue = refreshTokenService.createRefreshToken(memberKey);
    ResponseCookie refreshCookie =
        cookieUtil.createCookie(
            "refreshToken", refreshTokenValue, jwtProvider.getRefreshTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    // 4. 사용이 끝난 registerToken 쿠키 삭제 (즉시 만료)
    ResponseCookie deleteRegisterCookie = cookieUtil.deleteCookie("registerToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteRegisterCookie.toString());

    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(
      summary = "로그아웃",
      description = "accessToken, refreshToken 쿠키를 만료시키고 DB에서 refresh token을 삭제하여 로그아웃 처리")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      Authentication authentication, HttpServletResponse response) {
    // Access Token 쿠키 삭제
    ResponseCookie deleteAccessCookie = cookieUtil.deleteCookie("accessToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());

    // Refresh Token 쿠키 삭제
    ResponseCookie deleteRefreshCookie = cookieUtil.deleteCookie("refreshToken");
    response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

    // DB에서 Refresh Token 삭제
    if (authentication != null && authentication.getPrincipal() instanceof UUID memberKey) {
      refreshTokenService.deleteRefreshToken(memberKey);
    }

    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(
      summary = "토큰 갱신",
      description =
          "Refresh Token을 이용하여 새 Access Token과 Refresh Token을 발급합니다. "
              + "필터에서 자동 갱신이 되지만, 프론트에서 명시적으로 갱신이 필요할 때 사용합니다.")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<Void>> refreshToken(
      @CookieValue(value = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null) {
      throw new AppException(ErrorType.INVALID_REFRESH_TOKEN);
    }

    RefreshTokenService.TokenPair tokenPair = refreshTokenService.refreshAccessToken(refreshToken);

    // 새 Access Token 쿠키 설정
    ResponseCookie newAccessCookie =
        cookieUtil.createCookie(
            "accessToken", tokenPair.accessToken(), jwtProvider.getAccessTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, newAccessCookie.toString());

    // 새 Refresh Token 쿠키 설정 (Rotation)
    ResponseCookie newRefreshCookie =
        cookieUtil.createCookie(
            "refreshToken", tokenPair.refreshToken(), jwtProvider.getRefreshTokenValidityInMs());
    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshCookie.toString());

    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(
      summary = "프로필 이미지 업로드/수정",
      description = "로그인한 회원의 프로필 이미지를 업로드합니다. 기존 이미지가 있으면 자동으로 교체됩니다.")
  @PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ImageUrlResponse>> uploadProfileImage(
      Authentication authentication,
      @Parameter(description = "업로드할 이미지 파일 (jpg, png 등, 최대 10MB)") @RequestPart("image")
          MultipartFile image) {

    UUID memberKey = (UUID) authentication.getPrincipal();
    ImageUrlResponse imageResponse = memberImageService.uploadProfileImage(memberKey, image);
    return ResponseEntity.ok(ApiResponse.success(imageResponse));
  }

  @Operation(summary = "프로필 이미지 삭제", description = "로그인한 회원의 프로필 이미지를 삭제하고 기본 이미지로 초기화합니다.")
  @DeleteMapping("/me/profile-image")
  public ResponseEntity<ApiResponse<Void>> deleteProfileImage(Authentication authentication) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    memberImageService.deleteProfileImage(memberKey);
    return ResponseEntity.ok(ApiResponse.success());
  }
}
