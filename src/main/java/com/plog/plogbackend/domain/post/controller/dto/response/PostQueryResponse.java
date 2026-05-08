package com.plog.plogbackend.domain.post.controller.dto.response;

public record PostQueryResponse(PostRequestPartResponse request, PostImageResponse images) {
  public static PostQueryResponse of(PostRequestPartResponse request, PostImageResponse images) {
    return new PostQueryResponse(request, images);
  }
}
