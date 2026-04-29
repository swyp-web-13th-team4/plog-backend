package com.plog.plogbackend.domain.Member.dto.response;

import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** GET /api/members/mypage 응답 - 회원 기본 정보 + 내가 작성한 게시글 목록 */
public record MyPagePostsResponse(
    @Schema(description = "회원 기본 정보") MemberResponse memberInfo,
    @Schema(description = "내가 작성한 게시글 목록") List<FeedFindResponse> myPosts) {}
