package com.plog.plogbackend.domain.badge.dto;

import com.plog.plogbackend.domain.badge.entity.Badge;

public record BadgeResponse(Long id, String name, String description, String imageUrl) {
  public static BadgeResponse from(Badge badge) {
    return new BadgeResponse(
        badge.getId(), badge.getName(), badge.getDescription(), badge.getImageUrl());
  }
}
