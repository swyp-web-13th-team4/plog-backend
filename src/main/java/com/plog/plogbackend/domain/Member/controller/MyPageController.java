package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.response.MyPageResponse;
import com.plog.plogbackend.domain.Member.service.MyPageService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "마이페이지", description = "마이페이지 관련 API")
@RestController
@RequestMapping("/api/members/mypage")
@RequiredArgsConstructor
public class MyPageController {

  private final MyPageService myPageService;

  @Operation(summary = "마이페이지 종합 정보 조회", description = "로그인한 회원의 정보, 작성한 글, 북마크, 배지를 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(Authentication authentication) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    MyPageResponse response = myPageService.getMyPageData(memberKey);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
