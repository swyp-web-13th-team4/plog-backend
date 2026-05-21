package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.domain.map.repository.dto.PlaceRecord;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import java.util.List;
import lombok.Builder;

@Builder
public record PlaceRecordResponse(
    Long postId,
    String placeName,
    String title,
    Integer studyTime,
    Integer focus,
    String contents,
    String thumbnailUrl,
    PlaceCategoryCode categoryCode,
    List<PlaceTag> tags) {

  public static PlaceRecordResponse from(PlaceRecord record) {
    return PlaceRecordResponse.builder()
        .postId(record.getPostId())
        .placeName(record.getPlaceName())
        .title(record.getTitle())
        .studyTime(record.getStudyTime())
        .focus(record.getFocus())
        .contents(record.getContents())
        .thumbnailUrl(record.getThumbnailUrl())
        .categoryCode(record.getCategoryCode())
        .tags(record.getTags())
        .build();
  }
}
