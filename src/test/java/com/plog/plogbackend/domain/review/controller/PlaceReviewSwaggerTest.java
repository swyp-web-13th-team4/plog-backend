package com.plog.plogbackend.domain.review.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PlaceReviewSwaggerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("장소 리뷰 Swagger 문서에 통합 조회 경로와 요약 환경 스키마를 표시한다")
  void apiDocs_displaysUnifiedReviewPathAndSummaryEnvironmentSchema() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/reviews/{placeId}'].get.summary").value("장소 리뷰 화면 조회"))
        .andExpect(jsonPath("$.paths['/api/reviews/record/{placeId}']").doesNotExist())
        .andExpect(jsonPath("$.paths['/api/reviews/bookmark/{placeId}']").doesNotExist())
        .andExpect(
            jsonPath(
                    "$.components.schemas.PlaceReviewEnvironmentSummaryResponse.properties.environmentName")
                .exists())
        .andExpect(
            jsonPath("$.components.schemas.PlaceReviewEnvironmentSummaryResponse.properties.score")
                .exists())
        .andExpect(
            jsonPath("$.components.schemas.PlaceReviewEnvironmentSummaryResponse.properties.count")
                .exists())
        .andExpect(
            jsonPath(
                    "$.components.schemas.PlaceReviewEnvironmentSummaryResponse.properties.iconName")
                .doesNotExist())
        .andExpect(
            jsonPath("$.components.schemas.PlaceReviewEnvironmentSummaryResponse.properties.title")
                .doesNotExist())
        .andExpect(
            jsonPath("$.components.schemas.PlaceReviewEnvironmentSummaryResponse.properties.label")
                .doesNotExist())
        .andExpect(
            jsonPath(
                    "$.components.schemas.PlaceReviewEnvironmentItemResponse.properties.environmentName")
                .exists())
        .andExpect(
            jsonPath("$.components.schemas.PlaceReviewEnvironmentItemResponse.properties.score")
                .exists())
        .andExpect(
            jsonPath("$.components.schemas.PlaceReviewEnvironmentItemResponse.properties.title")
                .doesNotExist())
        .andExpect(
            jsonPath("$.components.schemas.PlaceReviewEnvironmentItemResponse.properties.iconName")
                .doesNotExist())
        .andExpect(
            jsonPath("$.components.schemas.PlaceReviewEnvironmentItemResponse.properties.label")
                .doesNotExist());
  }
}
