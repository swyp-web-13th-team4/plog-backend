package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import java.util.List;

public record PostCreateResponse(PostTextResponse texts, List<ImageUrlResponse> images) {

  public static PostCreateResponse of(PostTextResponse texts, List<ImageUrlResponse> images) {
    return new PostCreateResponse(texts, images);
  }
}
