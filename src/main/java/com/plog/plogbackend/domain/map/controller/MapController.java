package com.plog.plogbackend.domain.map.controller;

import com.plog.plogbackend.domain.map.controller.dto.request.MapViewportRequest;
import com.plog.plogbackend.domain.map.controller.dto.response.MapPinResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.PageResponse;
import com.plog.plogbackend.domain.map.service.MapService;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.support.paging.CursorDefault;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapController {

  private final MapService mapService;

  @GetMapping("/records")
  public ResponseEntity<ApiResponse<PageResponse<MapPinResponse>>> getRecords(
      @AuthenticationPrincipal UUID memberKey,
      @Valid MapViewportRequest request,
      @CursorDefault Cursorable<Long> cursorable) {
    Slice<MapPinResponse> slice =
        mapService
            .findMyRecordPins(memberKey, request.toViewport(), cursorable)
            .map(MapPinResponse::from);
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }

  @GetMapping("/bookmarks")
  public ResponseEntity<ApiResponse<PageResponse<MapPinResponse>>> getBookmarks(
      @AuthenticationPrincipal UUID memberKey,
      @Valid MapViewportRequest request,
      @CursorDefault Cursorable<Long> cursorable) {
    Slice<MapPinResponse> slice =
        mapService
            .findMyBookmarkPins(memberKey, request.toViewport(), cursorable)
            .map(MapPinResponse::from);
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }
}
