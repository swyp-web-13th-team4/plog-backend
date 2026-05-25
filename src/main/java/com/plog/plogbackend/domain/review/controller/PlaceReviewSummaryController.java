package com.plog.plogbackend.domain.review.controller;

import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPageResponse;
import com.plog.plogbackend.domain.review.service.PlaceReviewPageService;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.support.paging.CursorDefault;
import com.plog.plogbackend.global.support.paging.Cursorable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "장소 리뷰", description = "장소 리뷰 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class PlaceReviewSummaryController {

  private final PlaceReviewPageService placeReviewPageService;

  @GetMapping("/record/{placeId}")
  @Operation(
      summary = "기록 장소 리뷰 화면 조회",
      description = "내가 기록한 장소의 리뷰 요약과 방문자 리뷰 목록을 커서 페이징으로 반환합니다.")
  public ResponseEntity<ApiResponse<PlaceReviewPageResponse>> getRecordReviews(
      @AuthenticationPrincipal UUID memberKey,
      @PathVariable Long placeId,
      @CursorDefault Cursorable<String> cursorable) {
    return ResponseEntity.ok(
        ApiResponse.success(
            placeReviewPageService.getRecordReviewPage(memberKey, placeId, cursorable)));
  }

  @GetMapping("/bookmark/{placeId}")
  @Operation(
      summary = "북마크 장소 리뷰 화면 조회",
      description = "내가 북마크한 장소의 리뷰 요약과 방문자 리뷰 목록을 커서 페이징으로 반환합니다.")
  public ResponseEntity<ApiResponse<PlaceReviewPageResponse>> getBookmarkReviews(
      @AuthenticationPrincipal UUID memberKey,
      @PathVariable Long placeId,
      @CursorDefault Cursorable<String> cursorable) {
    return ResponseEntity.ok(
        ApiResponse.success(
            placeReviewPageService.getBookmarkReviewPage(memberKey, placeId, cursorable)));
  }
}
