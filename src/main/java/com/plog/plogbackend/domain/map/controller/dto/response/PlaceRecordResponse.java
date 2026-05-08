package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.model.PlaceRecord;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record PlaceRecordResponse(
    Long postId,
    LocalDate studyDate,
    Integer studyTime,
    Integer focus,
    String thumbnailUrl,
    PlaceCategoryCode categoryCode) {

  public static PlaceRecordResponse from(PlaceRecord record) {
    return PlaceRecordResponse.builder()
        .postId(record.getPostId())
        .studyDate(record.getStudyDate())
        .studyTime(record.getStudyTime())
        .focus(record.getFocus())
        .thumbnailUrl(record.getThumbnailUrl())
        .categoryCode(record.getCategoryCode())
        .build();
  }
}
