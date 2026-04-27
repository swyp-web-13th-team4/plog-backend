package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import java.time.LocalDateTime;
import java.util.List;

public record FeedFindResponse(
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
    List<String> tags) {

  public static FeedFindResponse from(Post post) {

    return new FeedFindResponse(
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
        post.getTags().stream().map(postTag -> postTag.getTag().getName()).toList());
  }
}
