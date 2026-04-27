package com.plog.plogbackend.domain.Member.dto.response;

import com.plog.plogbackend.domain.badge.dto.BadgeResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record MyPageResponse(
    @Schema(description = "회원 기본 정보") MyPageMemberResponse memberInfo,
    @Schema(description = "작성한 게시글 목록") List<FeedFindResponse> myPosts,
    @Schema(description = "북마크한 게시글 목록") List<FeedFindResponse> myBookmarks,
    @Schema(description = "획득한 배지 목록") List<BadgeResponse> myBadges){}
//TODO : 분석정보
