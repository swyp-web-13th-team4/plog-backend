package com.plog.plogbackend.domain.place.controller;

import com.plog.plogbackend.domain.place.dto.response.PlaceInfoResponse;
import com.plog.plogbackend.domain.place.dto.response.PlaceNameResponse;
import com.plog.plogbackend.domain.place.service.PlaceService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
@Tag(name = "장소", description = "장소 관련 API")
public class PlaceController {

  private final PlaceService placeService;

  @GetMapping("/{placeId}")
  @Operation(summary = "장소 기본 정보 조회", description = "장소 ID로 장소의 기본 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<PlaceInfoResponse>> getPlaceInfo(
      @Parameter(description = "장소 ID") @PathVariable Long placeId) {
    return ResponseEntity.ok(ApiResponse.success(placeService.getPlaceInfo(placeId)));
  }

  @GetMapping("/{placeId}/name")
  @Operation(summary = "장소명 조회", description = "장소 ID로 장소명을 조회합니다.")
  public ResponseEntity<ApiResponse<PlaceNameResponse>> getPlaceName(
      @Parameter(description = "장소 ID") @PathVariable Long placeId) {
    return ResponseEntity.ok(ApiResponse.success(placeService.getPlaceName(placeId)));
  }
}
