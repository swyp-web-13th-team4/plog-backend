package com.plog.plogbackend.domain.post.controller.dto.response;

import com.plog.plogbackend.domain.Member.dto.response.MemberResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPageFeedResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FeedUserResponse(
    @Schema(description = "회원 정보") MemberResponse memberInfo,
    @Schema(description = "게시글 목록") List<MyPageFeedResponse> posts) {}
