package com.plog.plogbackend.domain.review.controller;

import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPageResponse;
import com.plog.plogbackend.domain.review.enums.PlaceReviewSortType;
import com.plog.plogbackend.domain.review.service.PlaceReviewPageService;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.support.paging.CursorDefault;
import com.plog.plogbackend.global.support.paging.Cursorable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "장소 리뷰", description = "장소 리뷰 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class PlaceReviewSummaryController {

  private final PlaceReviewPageService placeReviewPageService;

  @GetMapping("/{placeId}")
  @Operation(
      summary = "장소 리뷰 화면 조회",
      description =
          "장소의 리뷰 요약과 방문자 리뷰 목록을 커서 페이징으로 반환합니다. "
              + "summary는 전체 장소 기준이며, imageOnly와 sortType은 reviews 목록에만 적용됩니다. "
              + "imageOnly 또는 sortType 변경 시 cursor를 생략하고 첫 페이지부터 다시 요청합니다.")
  public ResponseEntity<ApiResponse<PlaceReviewPageResponse>> getReviews(
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey,
      @Parameter(description = "장소 ID") @PathVariable Long placeId,
      @Parameter(description = "커서 페이징 정보입니다. 첫 페이지는 cursor를 생략하고, 다음 페이지는 nextCursor를 전달합니다.")
          @CursorDefault
          Cursorable<String> cursorable,
      @Parameter(description = "true이면 이미지가 있는 리뷰만 조회합니다. 기본값은 false입니다.")
          @RequestParam(defaultValue = "false")
          boolean imageOnly,
      @Parameter(
              description = "리뷰 목록 정렬 조건입니다. 기본값은 LATEST입니다.",
              schema = @Schema(allowableValues = {"LATEST", "OLDEST", "RATING_HIGH", "RATING_LOW"}))
          @RequestParam(defaultValue = "LATEST")
          PlaceReviewSortType sortType) {
    return ResponseEntity.ok(
        ApiResponse.success(
            placeReviewPageService.getReviewPage(
                memberKey, placeId, cursorable, imageOnly, sortType)));
  }
}
