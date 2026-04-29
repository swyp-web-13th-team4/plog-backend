package com.plog.plogbackend.domain.post.controller.dto.feed.response;

import java.util.List;

public record FeedMyPageResponse(
    String postImages,
    String title,
    String contents,
    String placeTag,
    Integer studyTime,
    Integer focus,
    List<String> tags,
    boolean bookMark) {}
