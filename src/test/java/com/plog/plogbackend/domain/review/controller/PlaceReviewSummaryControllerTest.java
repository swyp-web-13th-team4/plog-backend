package com.plog.plogbackend.domain.review.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPageResponse;
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
  @DisplayName("기록 장소의 리뷰 요약과 빈 리뷰 목록을 반환한다")
  void getRecordReviews_returnsReviewPage() {
    Long placeId = 1L;
    given(placeReviewStatisticsService.getSummary(placeId))
        .willReturn(new PlaceReviewSummary(15L, 4.27, List.of()));

    ResponseEntity<ApiResponse<PlaceReviewPageResponse>> response =
        placeReviewSummaryController.getRecordReviews(placeId);

    assertThat(response.getBody().getData().summary().reviewCount()).isEqualTo(15L);
    assertThat(response.getBody().getData().reviews().content()).isEmpty();
    assertThat(response.getBody().getData().reviews().hasNext()).isFalse();
    assertThat(response.getBody().getData().reviews().nextCursor()).isNull();
  }

  @Test
  @DisplayName("북마크 장소의 리뷰 요약과 빈 리뷰 목록을 반환한다")
  void getBookmarkReviews_returnsReviewPage() {
    Long placeId = 1L;
    given(placeReviewStatisticsService.getSummary(placeId))
        .willReturn(new PlaceReviewSummary(7L, 3.5, List.of()));

    ResponseEntity<ApiResponse<PlaceReviewPageResponse>> response =
        placeReviewSummaryController.getBookmarkReviews(placeId);

    assertThat(response.getBody().getData().summary().reviewCount()).isEqualTo(7L);
    assertThat(response.getBody().getData().reviews().content()).isEmpty();
    assertThat(response.getBody().getData().reviews().hasNext()).isFalse();
    assertThat(response.getBody().getData().reviews().nextCursor()).isNull();
  }
}
