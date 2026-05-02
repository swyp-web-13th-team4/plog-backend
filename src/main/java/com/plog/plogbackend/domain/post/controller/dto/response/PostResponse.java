package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record PostResponse(
    Long postId,
    String title,
    LocalDate studyDate,
    PublicScope scope,
    @Schema(example = "2026.05.02 14:30") String createdAt) {
  public static PostResponse from(Post post) {

    return new PostResponse(
        post.getId(),
        post.getTitle(),
        post.getStudyDate(),
        post.getScope(),
        post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
  }
}
