package com.plog.plogbackend.domain.review.controller;

import com.plog.plogbackend.domain.review.controller.api.PlaceReviewMapper;
import com.plog.plogbackend.domain.review.dto.request.PlaceReviewCreateRequest;
import com.plog.plogbackend.domain.review.dto.request.PlaceReviewUpdateRequest;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewResponse;
import com.plog.plogbackend.domain.review.service.PlaceReviewCommandService;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewCreateCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewUpdateCommand;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "장소 리뷰", description = "장소 리뷰 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed/review")
public class PlaceReviewController {

  private final PlaceReviewCommandService placeReviewCommandService;

  @Operation(summary = "장소 리뷰 생성", description = "장소 리뷰 정보와 이미지를 함께 업로드합니다. (이미지 최대 5개)")
  @PostMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<PlaceReviewResponse>> createReview(
      @PathVariable Long postId,
      @Parameter(description = "장소 리뷰 텍스트 데이터") @RequestPart("request") @Valid
          PlaceReviewCreateRequest request,
      @Parameter(description = "장소 리뷰 이미지 (최대 5개)") @RequestPart(value = "images", required = false)
          List<MultipartFile> images,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {
    PlaceReviewCreateCommand command = PlaceReviewMapper.from(postId, request, memberKey);
    PlaceReviewResponse response = placeReviewCommandService.create(command, images);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "장소 리뷰 수정", description = "장소 리뷰 별점, 환경 점수, 내용을 수정합니다. (작성 후 30일 이내)")
  @PutMapping("/{reviewId}")
  public ResponseEntity<ApiResponse<PlaceReviewResponse>> updateReview(
      @PathVariable Long reviewId,
      @Parameter(description = "장소 리뷰 수정 데이터") @RequestBody @Valid PlaceReviewUpdateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {
    PlaceReviewUpdateCommand command = PlaceReviewMapper.from(reviewId, request, memberKey);
    PlaceReviewResponse response = placeReviewCommandService.update(command);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
