package com.plog.plogbackend.domain.post.controller.api;

import com.plog.plogbackend.domain.post.controller.dto.request.FeedDetailRequest;
import com.plog.plogbackend.domain.post.controller.dto.request.FeedFindRequest;
import com.plog.plogbackend.domain.post.service.dto.FeedDetailCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedFindCommand;

public class PostMapper {

  public static FeedFindCommand from(FeedFindRequest request) {

    return new FeedFindCommand(request.lastPostId(), request.createAt());
  }

  public static FeedDetailCommand from(FeedDetailRequest request) {

    return new FeedDetailCommand(request.postId(), request.memberId());
  }
}
