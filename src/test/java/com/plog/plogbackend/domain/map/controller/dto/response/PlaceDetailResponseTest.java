package com.plog.plogbackend.domain.map.controller.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.map.repository.dto.PlaceDetail;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceDetailResponseTest {

  @Test
  @DisplayName("장소 상세 응답에는 장소 정보만 포함한다")
  void from_excludesReviewSummary() {
    PlaceDetail detail =
        PlaceDetail.of(
            1L,
            "스타벅스 광화문점",
            "서울시 종로구 세종대로 172",
            24L,
            4.5,
            1440L,
            "https://storage/place.jpg",
            PlaceCategoryCode.CAFE);

    PlaceDetailResponse response = PlaceDetailResponse.from(detail);

    assertThat(response.placeId()).isEqualTo(1L);
    assertThat(response.placeName()).isEqualTo("스타벅스 광화문점");
    assertThat(response.placeCategory()).isEqualTo(PlaceCategoryCode.CAFE);
  }
}
