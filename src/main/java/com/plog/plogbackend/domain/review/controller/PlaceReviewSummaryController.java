package com.plog.plogbackend.domain.review.controller;

import com.plog.plogbackend.domain.review.dto.response.PlaceReviewSummaryResponse;
import com.plog.plogbackend.domain.review.service.PlaceReviewStatisticsService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "장소 리뷰", description = "장소 리뷰 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class PlaceReviewSummaryController {

  private final PlaceReviewStatisticsService placeReviewStatisticsService;

  @GetMapping("/record/{placeId}")
  @Operation(summary = "기록 장소 리뷰 요약 조회", description = "기록 장소에 대한 리뷰 요약 통계를 반환합니다.")
  public ResponseEntity<ApiResponse<PlaceReviewSummaryResponse>> getRecordReviewSummary(
      @PathVariable Long placeId) {
    return ResponseEntity.ok(ApiResponse.success(getReviewSummary(placeId)));
  }

  @GetMapping("/bookmark/{placeId}")
  @Operation(summary = "북마크 장소 리뷰 요약 조회", description = "북마크 장소에 대한 리뷰 요약 통계를 반환합니다.")
  public ResponseEntity<ApiResponse<PlaceReviewSummaryResponse>> getBookmarkReviewSummary(
      @PathVariable Long placeId) {
    return ResponseEntity.ok(ApiResponse.success(getReviewSummary(placeId)));
  }

  private PlaceReviewSummaryResponse getReviewSummary(Long placeId) {
    return PlaceReviewSummaryResponse.from(placeReviewStatisticsService.getSummary(placeId));
  }
}
