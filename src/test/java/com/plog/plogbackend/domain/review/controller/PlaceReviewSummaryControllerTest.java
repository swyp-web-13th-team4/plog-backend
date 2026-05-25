package com.plog.plogbackend.domain.review.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.plog.plogbackend.domain.review.dto.response.PlaceReviewSummaryResponse;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import com.plog.plogbackend.domain.review.service.PlaceReviewStatisticsService;
import com.plog.plogbackend.global.response.ApiResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PlaceReviewSummaryControllerTest {

  @Mock private PlaceReviewStatisticsService placeReviewStatisticsService;
  @InjectMocks private PlaceReviewSummaryController placeReviewSummaryController;

  @Test
  @DisplayName("기록 장소의 리뷰 요약을 반환한다")
  void getRecordReviewSummary_returnsReviewSummary() {
    Long placeId = 1L;
    given(placeReviewStatisticsService.getSummary(placeId))
        .willReturn(new PlaceReviewSummary(15L, 4.27, List.of()));

    ResponseEntity<ApiResponse<PlaceReviewSummaryResponse>> response =
        placeReviewSummaryController.getRecordReviewSummary(placeId);

    assertThat(response.getBody().getData().reviewCount()).isEqualTo(15L);
  }

  @Test
  @DisplayName("북마크 장소의 리뷰 요약을 반환한다")
  void getBookmarkReviewSummary_returnsReviewSummary() {
    Long placeId = 1L;
    given(placeReviewStatisticsService.getSummary(placeId))
        .willReturn(new PlaceReviewSummary(7L, 3.5, List.of()));

    ResponseEntity<ApiResponse<PlaceReviewSummaryResponse>> response =
        placeReviewSummaryController.getBookmarkReviewSummary(placeId);

    assertThat(response.getBody().getData().reviewCount()).isEqualTo(7L);
  }
}
