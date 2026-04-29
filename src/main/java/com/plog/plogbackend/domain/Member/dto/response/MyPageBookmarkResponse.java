package com.plog.plogbackend.domain.Member.dto.response;

import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** GET /api/members/bookmark 응답 - 북마크한 게시글 목록 */
public record MyPageBookmarkResponse(
    @Schema(description = "북마크한 게시글 목록") List<FeedFindResponse> myBookmarks) {}
