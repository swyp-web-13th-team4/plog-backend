package com.plog.plogbackend.domain.place.controller;

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
class PlaceSwaggerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("장소 Swagger 문서에 장소명 조회 경로를 표시한다")
  void apiDocs_displaysPlaceNamePath() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/places/{placeId}/name'].get.summary").value("장소명 조회"))
        .andExpect(jsonPath("$.paths['/api/reviews/{placeId}/place']").doesNotExist());
  }
}
