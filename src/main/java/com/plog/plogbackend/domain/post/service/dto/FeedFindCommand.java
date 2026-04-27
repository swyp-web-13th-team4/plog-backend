package com.plog.plogbackend.domain.post.service.dto;

import java.time.LocalDateTime;

public record FeedFindCommand(long lastPostId, LocalDateTime createAt) {}
