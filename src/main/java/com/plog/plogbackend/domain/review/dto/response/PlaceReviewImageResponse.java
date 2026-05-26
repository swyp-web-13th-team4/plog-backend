package com.plog.plogbackend.domain.review.dto.response;

import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import java.util.List;

public record PlaceReviewImageResponse(List<PlaceReviewImageItem> images, int total) {

  public static PlaceReviewImageResponse from(List<PlaceReviewImage> images) {
    if (images == null || images.isEmpty()) {
      return new PlaceReviewImageResponse(List.of(), 0);
    }

    List<PlaceReviewImageItem> items = images.stream().map(PlaceReviewImageItem::from).toList();
    return new PlaceReviewImageResponse(items, items.size());
  }

  public record PlaceReviewImageItem(Long id, String url) {
    public static PlaceReviewImageItem from(PlaceReviewImage image) {
      return new PlaceReviewImageItem(image.getId(), image.getImageUrl());
    }
  }
}
