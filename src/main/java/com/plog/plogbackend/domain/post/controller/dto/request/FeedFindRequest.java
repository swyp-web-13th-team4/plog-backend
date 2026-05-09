package com.plog.plogbackend.domain.post.controller.dto.request;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record FeedFindRequest(
    long lastPostId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createAt) {}
