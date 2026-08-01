package com.plog.plogbackend.domain.place.controller;

import com.plog.plogbackend.domain.place.dto.response.PlaceNameResponse;
import com.plog.plogbackend.domain.place.service.PlaceService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/{placeId}/name")
    @Operation(summary = "리뷰 화면 장소명 조회", description = "장소 ID로 리뷰 화면에 표시할 장소명을 조회합니다.")
    public ResponseEntity<ApiResponse<PlaceNameResponse>> getReviewPlace(
            @Parameter(description = "장소 ID") @PathVariable Long placeId) {
        return ResponseEntity.ok(ApiResponse.success(placeService.getPlace(placeId)));
    }

}
