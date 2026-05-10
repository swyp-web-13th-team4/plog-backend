package com.plog.plogbackend.domain.post.controller.dto.request;

public record FeedFindRequest(long lastPostId
    // @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createAt
    ) {}
