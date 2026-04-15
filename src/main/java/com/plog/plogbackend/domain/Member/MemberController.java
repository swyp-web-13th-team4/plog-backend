package com.plog.plogbackend.domain.Member;

import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.dto.MemberSignupResponse;
import com.plog.plogbackend.global.common.ApiResponse;
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

  @Operation(summary = "회원가입 완성", description = "추가 정보 입력 후 가입 완료")
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<MemberSignupResponse>> signup(
      @RequestHeader("Authorization") String authorizationHeader,
      @Valid @RequestBody MemberSignupRequest request)
        ///
        // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 여기 밑에 부터는 공통 응답 객체 머지 되면 수정 하겠습니다
      {
    String registerToken = resolveToken(authorizationHeader);
    MemberSignupResponse response = memberService.signup(registerToken, request);
    return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", response));
  }

  private String resolveToken(String authorizationHeader) {
    if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    }
    throw new IllegalArgumentException("올바르지 않은 인증 헤더입니다.");
  }
}
