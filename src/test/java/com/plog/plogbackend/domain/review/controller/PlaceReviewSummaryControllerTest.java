package com.plog.plogbackend.domain.review.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPageResponse;
import com.plog.plogbackend.domain.review.enums.PlaceReviewSortType;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import com.plog.plogbackend.domain.review.service.PlaceReviewPageService;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.support.paging.Cursorable;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PlaceReviewSummaryControllerTest {

  @Mock private PlaceReviewPageService placeReviewPageService;
  @InjectMocks private PlaceReviewSummaryController placeReviewSummaryController;

  @Test
  @DisplayName("장소의 리뷰 요약과 빈 리뷰 목록을 반환한다")
  void getReviews_returnsReviewPage() {
    UUID memberKey = UUID.randomUUID();
    Long placeId = 1L;
    Cursorable<String> cursorable = new Cursorable<>(null, 10);
    boolean imageOnly = true;
    PlaceReviewSortType sortType = PlaceReviewSortType.LATEST;
    given(placeReviewPageService.getReviewPage(memberKey, placeId, cursorable, imageOnly, sortType))
        .willReturn(PlaceReviewPageResponse.from(new PlaceReviewSummary(15L, 4.27, List.of())));

    ResponseEntity<ApiResponse<PlaceReviewPageResponse>> response =
        placeReviewSummaryController.getReviews(
            memberKey, placeId, cursorable, imageOnly, sortType);

    assertThat(response.getBody().getData().summary().reviewCount()).isEqualTo(15L);
    assertThat(response.getBody().getData().reviews().content()).isEmpty();
    assertThat(response.getBody().getData().reviews().hasNext()).isFalse();
    assertThat(response.getBody().getData().reviews().nextCursor()).isNull();
  }
}
