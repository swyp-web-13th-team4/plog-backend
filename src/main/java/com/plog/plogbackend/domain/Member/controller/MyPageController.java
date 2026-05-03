package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.response.MemberAnalyticsResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  /** 대표 배지 설정 PATCH /api/members/badge/main */
  @Operation(
      summary = "대표 배지 설정",
      description = "로그인한 회원의 대표 배지를 변경합니다. 해당 배지가 존재하지 않거나 보유하지 않은 배지인 경우 오류를 반환합니다.")
  @PatchMapping("/badge/main")
  public ResponseEntity<ApiResponse<Void>> updateMainBadge(
      Authentication authentication, @RequestParam Long badgeId) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    myPageService.updateMainBadge(memberKey, badgeId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  /** 회원 분석 정보 조회 GET /api/members/analytics */
  @Operation(
      summary = "분석 정보 조회",
      description =
          "로그인한 회원의 기록 횟수, 작업 시간, 작업 유형 카드, 집중 환경 조건, 공간별 순위를 조회합니다.")
  @GetMapping("/analytics")
  public ResponseEntity<ApiResponse<MemberAnalyticsResponse>> getMemberAnalytics(
      Authentication authentication) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    return ResponseEntity.ok(
        ApiResponse.success(myPageService.getMemberAnalytics(memberKey)));
  }
}
