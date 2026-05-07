package com.plog.plogbackend.domain.post.controller;

import com.plog.plogbackend.domain.post.controller.dto.request.post.RecentPlaceSaveRequest;
import com.plog.plogbackend.domain.post.controller.dto.response.RecentPlaceDeleteResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.RecentPlaceSaveResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.RecentPlaceSearchListResponse;
import com.plog.plogbackend.domain.post.service.RecentPlaceSearchService;
import com.plog.plogbackend.domain.post.service.dto.RecentPlaceSaveCommand;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "최근 장소 검색", description = "장소 검색 화면의 최근 검색 이력 API")
@RestController
@RequestMapping("/api/place/recent")
@RequiredArgsConstructor
public class RecentPlaceSearchController {

  private final RecentPlaceSearchService service;

  @Operation(summary = "최근 장소 목록 조회", description = "장소 검색 화면 진입 시 호출")
  @GetMapping
  public ResponseEntity<ApiResponse<RecentPlaceSearchListResponse>> list(
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    return ResponseEntity.ok(ApiResponse.success(service.findRecent(memberKey)));
  }

  @Operation(summary = "최근 장소 저장", description = "사용자가 카카오 검색 결과에서 장소를 선택했을 때 호출. 같은 장소면 시간만 갱신됨.")
  @PostMapping
  public ResponseEntity<ApiResponse<RecentPlaceSaveResponse>> save(
      @Valid @RequestBody RecentPlaceSaveRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    RecentPlaceSaveResponse saveResponse =
        service.save(memberKey, RecentPlaceSaveCommand.from(request));

    return ResponseEntity.ok(ApiResponse.success(saveResponse));
  }

  @Operation(summary = "최근 장소 개별 삭제 (X 버튼)")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<RecentPlaceDeleteResponse>> deleteOne(
      @PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    RecentPlaceDeleteResponse response = service.deleteOne(id, memberKey);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "최근 장소 전체 삭제 (전체삭제 버튼)")
  @DeleteMapping
  public ResponseEntity<ApiResponse<RecentPlaceDeleteResponse>> deleteAll(
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    RecentPlaceDeleteResponse response = service.deleteAll(memberKey);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
