package com.plog.plogbackend.domain.map.controller;

import com.plog.plogbackend.domain.map.controller.dto.request.MapRequest;
import com.plog.plogbackend.domain.map.controller.dto.response.MapCountResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.MapPinResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.PageResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.PlaceDetailResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.PlaceRecordResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.PlaceSearchResponse;
import com.plog.plogbackend.domain.map.controller.dto.response.PlaceSummaryResponse;
import com.plog.plogbackend.domain.map.service.MapService;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.global.common.enums.SortType;
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

  @GetMapping("/count")
  @Operation(summary = "지도 기록/북마크 개수 조회", description = "본인의 전체 기록 수와 북마크 수를 반환합니다.")
  public ResponseEntity<ApiResponse<MapCountResponse>> getMapCount(
      @AuthenticationPrincipal UUID memberKey) {
    return ResponseEntity.ok(
        ApiResponse.success(MapCountResponse.of(mapService.getMapCount(memberKey))));
  }

  @GetMapping("/places")
  @Operation(summary = "기록한 장소 검색", description = "키워드에 해당하는 내 기록이 있는 장소 목록을 최신 공부 날짜순으로 반환합니다.")
  public ResponseEntity<ApiResponse<List<PlaceSearchResponse>>> searchPlaces(
      @AuthenticationPrincipal UUID memberKey, @RequestParam String keyword) {
    List<PlaceSearchResponse> result =
        mapService.searchRecordedPlaces(memberKey, keyword).stream()
            .map(PlaceSearchResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/sheet/records")
  @Operation(summary = "하단 시트 내 기록 장소 목록 조회", description = "내 기록이 있는 전체 장소 목록을 커서 페이징으로 반환합니다.")
  public ResponseEntity<ApiResponse<PageResponse<PlaceSummaryResponse>>> getSheetRecords(
      @AuthenticationPrincipal UUID memberKey,
      @RequestParam(defaultValue = "LATEST") SortType sortType,
      @CursorDefault Cursorable<String> cursorable) {
    Slice<PlaceSummaryResponse> slice =
        mapService
            .findAllRecordPlaces(memberKey, sortType, cursorable)
            .map(PlaceSummaryResponse::from);
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }

  @GetMapping("/sheet/bookmarks")
  @Operation(summary = "하단 시트 북마크 장소 목록 조회", description = "내 북마크가 있는 전체 장소 목록을 커서 페이징으로 반환합니다.")
  public ResponseEntity<ApiResponse<PageResponse<PlaceSummaryResponse>>> getSheetBookmarks(
      @AuthenticationPrincipal UUID memberKey,
      @RequestParam(defaultValue = "LATEST") SortType sortType,
      @CursorDefault Cursorable<String> cursorable) {
    Slice<PlaceSummaryResponse> slice =
        mapService
            .findAllBookmarkPlaces(memberKey, sortType, cursorable)
            .map(PlaceSummaryResponse::from);
    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }

  @GetMapping("/pins/records")
  @Operation(summary = "지도 홈 화면 내 기록 목록 조회", description = "지도 홈 화면에서 뷰포트 내의 핀에 대한 내 기록 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<List<MapPinResponse>>> getRecords(
      @AuthenticationPrincipal UUID memberKey, @Valid MapRequest request) {
    List<MapPinResponse> pins =
        mapService.findMyRecordPins(memberKey, request.toViewport()).stream()
            .map(MapPinResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.success(pins));
  }

  @GetMapping("/pins/bookmarks")
  @Operation(
      summary = "지도 홈 화면 북마크 목록 조회",
      description = "지도 홈 화면에서 뷰포트 내의 핀에 대한 자신의 북마크 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<List<MapPinResponse>>> getBookmarks(
      @AuthenticationPrincipal UUID memberKey, @Valid MapRequest request) {
    List<MapPinResponse> pins =
        mapService.findMyBookmarkPins(memberKey, request.toViewport()).stream()
            .map(MapPinResponse::from)
            .toList();

    return ResponseEntity.ok(ApiResponse.success(pins));
  }

  @GetMapping("/pins/records/{placeId}")
  @Operation(summary = "기록 핀 상세 조회", description = "기록 핀을 탭했을 때 장소 정보와 전체 리뷰 건수·평균 평점을 반환합니다.")
  public ResponseEntity<ApiResponse<PlaceDetailResponse>> getRecordPinDetail(
      @AuthenticationPrincipal UUID memberKey, @PathVariable Long placeId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PlaceDetailResponse.from(mapService.findRecordPinDetail(memberKey, placeId))));
  }

  @GetMapping("/pins/bookmarks/{placeId}")
  @Operation(summary = "북마크 핀 상세 조회", description = "북마크 핀을 탭했을 때 장소 정보와 전체 리뷰 건수·평균 평점을 반환합니다.")
  public ResponseEntity<ApiResponse<PlaceDetailResponse>> getBookmarkPinDetail(
      @AuthenticationPrincipal UUID memberKey, @PathVariable Long placeId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PlaceDetailResponse.from(mapService.findBookmarkPinDetail(memberKey, placeId))));
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
