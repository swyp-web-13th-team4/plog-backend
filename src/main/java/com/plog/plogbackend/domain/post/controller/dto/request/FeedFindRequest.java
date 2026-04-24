package com.plog.plogbackend.domain.post.controller.dto.request;

import java.time.LocalDateTime;

public record FeedFindRequest(long lastPostId, LocalDateTime createAt) {}
