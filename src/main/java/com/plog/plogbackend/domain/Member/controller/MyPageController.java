package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.response.MyPageBadgeResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPageBookmarkResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPagePostsResponse;
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
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MyPageController {

  private final MyPageService myPageService;

  /** 회원 기본 정보 + 내가 작성한 게시글 목록 조회 GET /api/members/mypage */
  @Operation(
      summary = "마이페이지 기본 정보 조회",
      description = "로그인한 회원의 기본 정보(닉네임·프로필·소개글)와 내가 작성한 게시글 목록을 조회합니다.")
  @GetMapping("/mypage")
  public ResponseEntity<ApiResponse<MyPagePostsResponse>> getMyPage(Authentication authentication) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    return ResponseEntity.ok(ApiResponse.success(myPageService.getMyPageData(memberKey)));
  }

  /** 북마크한 게시글 목록 조회 GET /api/members/bookmark */
  @Operation(summary = "북마크 목록 조회", description = "로그인한 회원이 북마크한 게시글 목록을 조회합니다.")
  @GetMapping("/bookmark")
  public ResponseEntity<ApiResponse<MyPageBookmarkResponse>> getMyBookmarks(
      Authentication authentication) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    return ResponseEntity.ok(ApiResponse.success(myPageService.getMyBookmarks(memberKey)));
  }

  /** 획득한 배지 목록 조회 GET /api/members/badge */
  @Operation(summary = "배지 목록 조회", description = "로그인한 회원이 획득한 배지 목록을 조회합니다.")
  @GetMapping("/badge")
  public ResponseEntity<ApiResponse<MyPageBadgeResponse>> getMyBadges(
      Authentication authentication) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    return ResponseEntity.ok(ApiResponse.success(myPageService.getMyBadges(memberKey)));
  }

  // TODO: GET /api/members/analytics - 분석 정보 API 추가 예정
}
