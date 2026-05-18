package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "수정용 게시글 데이터 응답")
public record PostRequestPartResponse(
    @Schema(description = "제목") String title,
    @Schema(description = "내용") String contents,
    @Schema(description = "공부 시작 시각") TimePickerResponse startedAt,
    @Schema(description = "공부 종료 시각") TimePickerResponse endedAt,
    @Schema(description = "공부 날짜") LocalDate studyDate,
    @Schema(description = "총 공부 시간 (분)") Integer studyTime,
    @Schema(description = "집중도 (1~5)") Integer focus,
    @Schema(description = "공개 범위") PublicScope scope,
    @Schema(description = "장소 태그 목록") List<PlaceTag> placeTags,
    @Schema(description = "장소 정보 (객체 구조)") PlaceResponse place,
    @Schema(description = "장소 카테고리 코드") PlaceCategoryCode categoryCode) {

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
        PlaceResponse.from(
            post.getPlace().getName(),
            post.getPlace().getAddress(),
            post.getPlace().getLatitude(),
            post.getPlace().getLongitude()),
        post.getPlaceCategory());
  }
}
