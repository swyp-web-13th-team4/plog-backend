package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FeedDetailResponse(
    Long postId,
    UUID memberKey,
    String name,
    String profileImage,
    //    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    LocalDateTime createAt,
    List<String> postImages,
    Long likes,
    String title,
    String contents,
    String placeName,
    Integer studyTime,
    Integer focus,
    List<PlaceTag> tags,
    boolean isAuthor,
    boolean like,
    boolean bookMark,
    String category,
    String address,
    PublicScope scope) {

  public static FeedDetailResponse from(
      Post post, boolean isAuthor, Boolean isLiked, Boolean isBookMarked, String category) {

    return new FeedDetailResponse(
        post.getId(),
        post.getMember().getMemberKey(),
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
        isAuthor,
        isLiked,
        isBookMarked,
        category,
        post.getPlace().getAddress(),
        post.getScope());
  }
}
