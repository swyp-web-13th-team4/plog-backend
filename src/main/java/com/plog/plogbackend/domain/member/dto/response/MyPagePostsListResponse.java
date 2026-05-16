package com.plog.plogbackend.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** GET /api/members/mypage/posts 응답 - 나의 게시글 목록 (정렬 지원) */
public record MyPagePostsListResponse(
    @Schema(description = "게시글 목록") List<MyPageFeedResponse> posts) {}
