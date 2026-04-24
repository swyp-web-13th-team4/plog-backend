package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import java.time.LocalDateTime;
import java.util.List;

public record FeedDetailResponse(
    String name,
    String profileImage,
    LocalDateTime createAt,
    List<String> postImages,
    Long likes,
    String title,
    String contents,
    String placeName,
    Integer studyTime,
    Integer focus,
    List<String> tags,
    boolean isAuthor) {

  public static FeedDetailResponse from(Post post, boolean isAuthor) {

    return new FeedDetailResponse(
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
        post.getTags().stream().map(postTag -> postTag.getTag().getName()).toList(),
        isAuthor);
  }
}
