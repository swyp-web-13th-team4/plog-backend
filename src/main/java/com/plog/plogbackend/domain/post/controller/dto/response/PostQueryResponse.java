package com.plog.plogbackend.domain.post.controller.dto.response;

public record PostQueryResponse(PostRequestPartResponse post, PostImageResponse images) {
  public static PostQueryResponse of(PostRequestPartResponse post, PostImageResponse images) {
    return new PostQueryResponse(post, images);
  }
}
