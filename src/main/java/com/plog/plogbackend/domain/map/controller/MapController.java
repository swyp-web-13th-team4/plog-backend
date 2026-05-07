package com.plog.plogbackend.domain.map.controller;

import com.plog.plogbackend.domain.map.controller.dto.request.MapRequest;
import com.plog.plogbackend.domain.map.controller.dto.response.MapPinResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.PageResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.PlaceRecordResponse;
import com.plog.plogbackend.domain.map.model.SortType;
import com.plog.plogbackend.domain.map.service.MapService;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.support.paging.CursorDefault;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
@Tag(name = "지도", description = "지도 관련 API")
public class MapController {

  private final MapService mapService;

  @GetMapping("/records")
  @Operation(summary = "지도 홈 화면 내 기록 목록 조회", description = "지도 홈 화면에서 뷰포트 내의 핀에 대한 내 기록 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<PageResponse<MapPinResponse>>> getRecords(
      @AuthenticationPrincipal UUID memberKey,
      @Valid MapRequest request,
      @CursorDefault Cursorable<String> cursorable) {
    Slice<MapPinResponse> slice =
        mapService
            .findMyRecordPins(memberKey, request.toViewport(), request.sortType(), cursorable)
            .map(MapPinResponse::from);
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }

  @GetMapping("/bookmarks")
  @Operation(
      summary = "지도 홈 화면 북마크 목록 조회",
      description = "지도 홈 화면에서 뷰포트 내의 핀에 대한 자신의 북마크 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<PageResponse<MapPinResponse>>> getBookmarks(
      @AuthenticationPrincipal UUID memberKey,
      @Valid MapRequest request,
      @CursorDefault Cursorable<String> cursorable) {
    Slice<MapPinResponse> slice =
        mapService
            .findMyBookmarkPins(memberKey, request.toViewport(), request.sortType(), cursorable)
            .map(MapPinResponse::from);
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }

  @GetMapping("/{placeId}/records")
  @Operation(summary = "장소별 내 기록 목록 조회", description = "특정 장소에 대한 내 기록 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<PageResponse<PlaceRecordResponse>>> getPlaceRecords(
      @AuthenticationPrincipal UUID memberKey,
      @PathVariable Long placeId,
      @RequestParam(defaultValue = "LATEST") SortType sortType,
      @RequestParam(required = false) List<PlaceTag> tags,
      @CursorDefault Cursorable<String> cursorable) {
    Slice<PlaceRecordResponse> slice =
        mapService
            .findPlaceRecords(memberKey, placeId, sortType, tags, cursorable)
            .map(PlaceRecordResponse::from);
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }

  @GetMapping("/{placeId}/bookmarks")
  @Operation(summary = "장소별 북마크 목록 조회", description = "특정 장소에 대한 내 북마크 기록 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<PageResponse<PlaceRecordResponse>>> getPlaceBookmarks(
      @AuthenticationPrincipal UUID memberKey,
      @PathVariable Long placeId,
      @RequestParam(defaultValue = "LATEST") SortType sortType,
      @RequestParam(required = false) List<PlaceTag> tags,
      @CursorDefault Cursorable<String> cursorable) {
    Slice<PlaceRecordResponse> slice =
        mapService
            .findPlaceBookmarks(memberKey, placeId, sortType, tags, cursorable)
            .map(PlaceRecordResponse::from);
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }
}
