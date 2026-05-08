package com.plog.plogbackend.domain.post.controller.api;

import com.plog.plogbackend.domain.post.controller.dto.request.FeedFindRequest;
import com.plog.plogbackend.domain.post.controller.dto.request.post.PostCreateRequest;
import com.plog.plogbackend.domain.post.controller.dto.request.post.PostUpdateRequest;
import com.plog.plogbackend.domain.post.service.dto.*;
import java.util.UUID;

public class PostMapper {

  public static FeedFindCommand from(FeedFindRequest request) {

    return new FeedFindCommand(request.lastPostId(), request.createAt());
  }

  public static FeedDetailCommand from(Long postId) {

    return new FeedDetailCommand(postId);
  }

  public static FeedMyPageCommand from(UUID memberKey) {
    return new FeedMyPageCommand(memberKey);
  }

  public static PostCreateCommand from(PostCreateRequest request, UUID memberKey) {

    return new PostCreateCommand(
        request.title(),
        request.contents(),
        TimePickerCommand.from(request.startedAt()),
        TimePickerCommand.from(request.endedAt()),
        request.studyDate(),
        request.focus(),
        request.scope(),
        PlaceCommand.from(request.place()),
        request.placeTags(),
        request.categoryCode(),
        memberKey);
  }

  public static PostUpdateCommand from(Long postId, PostUpdateRequest request, UUID memberKey) {
    return new PostUpdateCommand(
        postId,
        memberKey,
        request.title(),
        request.contents(),
        TimePickerCommand.from(request.startedAt()),
        TimePickerCommand.from(request.endedAt()),
        request.studyDate(),
        request.focus(),
        request.scope(),
        PlaceCommand.from(request.place()),
        request.placeTags(),
        request.categoryCode(),
        request.keepImageIds());
  }
}
