package com.plog.plogbackend.domain.post.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(
    List<FeedFindResponse> feedFindResponses, Long lastPostId, LocalDateTime createdAt) {}
