package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.post.entity.PostImage;
import java.util.List;

public record PostImageResponse(List<PostImageItem> images, int total) {

  public static PostImageResponse from(List<PostImage> postImages) {
    List<PostImageItem> items = postImages.stream().map(PostImageItem::from).toList();
    return new PostImageResponse(items, items.size());
  }

  public record PostImageItem(Long id, String url) {
    public static PostImageItem from(PostImage postImage) {
      return new PostImageItem(postImage.getId(), postImage.getImageUrl());
    }
  }
}
