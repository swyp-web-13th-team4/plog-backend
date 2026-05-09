package com.plog.plogbackend.domain.post.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(
    List<FeedFindResponse> feedFindResponses, Long lastPostId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    LocalDateTime createdAt) {}
