package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.dto.MemberSignupResponse;
import com.plog.plogbackend.domain.Member.service.MemberService;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "로그인 인증/인가")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @Operation(summary = "회원가입(카카오 인증 후)", description = "추가 정보 입력 후 가입 완료")
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<MemberSignupResponse>> signup(
      @RequestHeader("Authorization") String authorizationHeader,
      @Valid @RequestBody MemberSignupRequest request) {
    String registerToken = resolveToken(authorizationHeader);
    MemberSignupResponse response = memberService.signup(registerToken, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  private String resolveToken(String authorizationHeader) {
    if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    }
    throw new AppException(ErrorType.INVALID_AUTH_HEADER);
  }
}
