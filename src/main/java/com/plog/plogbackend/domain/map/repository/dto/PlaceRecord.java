package com.plog.plogbackend.domain.map.repository.dto;

import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PlaceRecord {
  private Long postId;
  private String placeName;
  private String title;
  private Integer studyTime;
  private Integer focus;
  private String contents;
  private String thumbnailUrl;
  private PlaceCategoryCode categoryCode;
  private List<PlaceTag> tags;

  public static PlaceRecord of(
      Post post, String placeName, String thumbnailUrl, List<PlaceTag> tags) {
    return PlaceRecord.builder()
        .postId(post.getId())
        .placeName(placeName)
        .title(post.getTitle())
        .studyTime(post.getStudyTime())
        .focus(post.getFocus())
        .contents(post.getContents())
        .thumbnailUrl(thumbnailUrl)
        .categoryCode(post.getPlaceCategory())
        .tags(tags)
        .build();
  }
}
