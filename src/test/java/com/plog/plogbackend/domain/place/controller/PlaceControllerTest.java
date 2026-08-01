package com.plog.plogbackend.domain.place.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.plog.plogbackend.domain.place.dto.response.PlaceInfoResponse;
import com.plog.plogbackend.domain.place.dto.response.PlaceNameResponse;
import com.plog.plogbackend.domain.place.service.PlaceService;
import com.plog.plogbackend.global.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PlaceControllerTest {

  @Mock private PlaceService placeService;
  @InjectMocks private PlaceController placeController;

  @Test
  @DisplayName("장소 기본 정보를 반환한다")
  void getPlaceInfo_returnsPlaceInfo() {
    Long placeId = 1L;
    given(placeService.getPlaceInfo(placeId))
        .willReturn(new PlaceInfoResponse(placeId, "콤파일", "서울 마포구 잔다리로 73", 37.5501, 126.9212));

    ResponseEntity<ApiResponse<PlaceInfoResponse>> response = placeController.getPlaceInfo(placeId);

    assertThat(response.getBody().getData().placeId()).isEqualTo(placeId);
    assertThat(response.getBody().getData().name()).isEqualTo("콤파일");
    assertThat(response.getBody().getData().address()).isEqualTo("서울 마포구 잔다리로 73");
    assertThat(response.getBody().getData().latitude()).isEqualTo(37.5501);
    assertThat(response.getBody().getData().longitude()).isEqualTo(126.9212);
  }

  @Test
  @DisplayName("장소명을 반환한다")
  void getPlaceName_returnsPlaceName() {
    Long placeId = 1L;
    given(placeService.getPlaceName(placeId)).willReturn(new PlaceNameResponse(placeId, "콤파일"));

    ResponseEntity<ApiResponse<PlaceNameResponse>> response = placeController.getPlaceName(placeId);

    assertThat(response.getBody().getData().placeId()).isEqualTo(placeId);
    assertThat(response.getBody().getData().placeName()).isEqualTo("콤파일");
  }
}
