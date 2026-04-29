package com.plog.plogbackend.domain.post.controller.api;

import com.plog.plogbackend.domain.post.controller.dto.request.FeedFindRequest;
import com.plog.plogbackend.domain.post.service.dto.FeedDetailCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedFindCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedMyPageCommand;
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
}
