package com.plog.plogbackend.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.plog.plogbackend.domain.place.dto.response.PlaceNameResponse;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

  @Mock private PlaceRepository placeRepository;
  @InjectMocks private PlaceService placeService;

  @Test
  @DisplayName("장소 ID로 장소명을 조회한다")
  void getPlace_returnsPlaceName() {
    Long placeId = 1L;
    Place place = Place.of("콤파일", "서울 마포구 잔다리로 73", 37.5501, 126.9212);
    given(placeRepository.findById(placeId)).willReturn(Optional.of(place));

    PlaceNameResponse response = placeService.getPlace(placeId);

    assertThat(response.placeName()).isEqualTo("콤파일");
  }

  @Test
  @DisplayName("장소가 없으면 장소 없음 예외를 반환한다")
  void getPlace_whenPlaceNotFound_throwsPlaceNotFound() {
    given(placeRepository.findById(1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> placeService.getPlace(1L))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.PLACE_NOT_FOUND);
  }
}
