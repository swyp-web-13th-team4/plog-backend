package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.dto.response.MyPageBadgeResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPageBookmarkResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPageMemberResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPagePostsResponse;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.badge.dto.BadgeResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

  private final MemberRepository memberRepository;
  private final MemberService memberService;

  /**
   * GET /api/members/mypage 회원 기본 정보 + 내가 작성한 게시글 목록을 반환합니다.
   *
   * @param memberKey 회원 UUID
   * @return 회원 정보 + 게시글 목록
   */
  @Transactional(readOnly = true)
  public MyPagePostsResponse getMyPageData(UUID memberKey) {
    MyPageMemberResponse memberInfo = memberService.getMyPageInfo(memberKey);

    List<FeedFindResponse> posts =
        memberRepository.findMyPosts(memberKey).stream().map(FeedFindResponse::from).toList();

    return new MyPagePostsResponse(memberInfo, posts);
  }

  /**
   * GET /api/members/bookmark 내가 북마크한 게시글 목록을 반환합니다.
   *
   * @param memberKey 회원 UUID
   * @return 북마크 게시글 목록
   */
  @Transactional(readOnly = true)
  public MyPageBookmarkResponse getMyBookmarks(UUID memberKey) {
    List<FeedFindResponse> bookmarks =
        memberRepository.findMyBookmarks(memberKey).stream().map(FeedFindResponse::from).toList();

    return new MyPageBookmarkResponse(bookmarks);
  }

  /**
   * GET /api/members/badge 내가 획득한 배지 목록을 반환합니다.
   *
   * @param memberKey 회원 UUID
   * @return 배지 목록
   */
  @Transactional(readOnly = true)
  public MyPageBadgeResponse getMyBadges(UUID memberKey) {
    List<BadgeResponse> badges =
        memberRepository.findMyBadges(memberKey).stream().map(BadgeResponse::from).toList();

    return new MyPageBadgeResponse(badges);
  }

  // TODO: GET /api/members/analytics - 분석 정보 메서드 추가 예정
}
