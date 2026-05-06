package com.plog.plogbackend.domain.Member.dto.response;

import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/** 마이페이지 게시글/북마크 목록 응답 - postId를 포함하여 상세 페이지 이동 지원 */
public record MyPageFeedResponse(
    @Schema(description = "게시글 ID (상세 조회 시 사용)") Long postId,
    @Schema(description = "작성자 닉네임") String name,
    @Schema(description = "작성자 프로필 이미지") String profileImage,
    @Schema(description = "작성 시각") LocalDateTime createAt,
    @Schema(description = "게시글 이미지 목록") List<String> postImages,
    @Schema(description = "좋아요 수") Long likes,
    @Schema(description = "제목") String title,
    @Schema(description = "본문") String contents,
    @Schema(description = "장소명") String placeName,
    @Schema(description = "작업 시간(분)") Integer studyTime,
    @Schema(description = "집중도") Integer focus,
    @Schema(description = "태그 목록") List<PlaceTag> tags,
    @Schema(description = "좋아요 여부") boolean like,
    @Schema(description = "북마크 여부") boolean bookMark,
    @Schema(description = "장소 카테고리(value)") String placeCategory) {

  public static MyPageFeedResponse from(Post post, boolean isLiked, boolean isBookMarked) {
    return new MyPageFeedResponse(
        post.getId(),
        post.getMember().getNickname(),
        post.getMember().getProfileImage(),
        post.getCreatedAt(),
        post.getImages().stream().map(PostImage::getImageUrl).toList(),
        post.getLikes(),
        post.getTitle(),
        post.getContents(),
        post.getPlace().getName(),
        post.getStudyTime(),
        post.getFocus(),
        post.getTags().stream().map(postTag -> postTag.getTag().getPlaceTag()).toList(),
        isLiked,
        isBookMarked,
        post.getPlaceCategory() != null && post.getPlaceCategory().getCategoryName() != null
            ? post.getPlaceCategory().getCategoryName().getValue()
            : null);
  }
}
