package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import java.time.LocalDate;
import java.util.List;

public record PostRequestPartResponse(
    String title,
    String contents,
    TimePickerResponse startedAt,
    TimePickerResponse endedAt,
    LocalDate studyDate,
    Integer studyTime,
    Integer focus,
    PublicScope scope,
    List<PlaceTag> placeTags,
    String placeName,
    String placeAddress,
    Double latitude,
    Double longitude,
    PlaceCategoryCode categoryCode) {

  public static PostRequestPartResponse from(Post post) {
    return new PostRequestPartResponse(
        post.getTitle(),
        post.getContents(),
        TimePickerResponse.from(post.getStartedAt()),
        TimePickerResponse.from(post.getEndedAt()),
        post.getStudyDate(),
        post.getStudyTime(),
        post.getFocus(),
        post.getScope(),
        post.getTags().stream().map(postTag -> postTag.getTag().getPlaceTag()).toList(),
        post.getPlace().getName(),
        post.getPlace().getAddress(),
        post.getPlace().getLatitude(),
        post.getPlace().getLongitude(),
        post.getPlaceCategory().getCategoryName());
  }
}
