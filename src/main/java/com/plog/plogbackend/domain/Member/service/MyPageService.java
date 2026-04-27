package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.dto.response.MyPageMemberResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPageResponse;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.badge.dto.BadgeResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

  private final MemberRepository memberRepository;
  private final MemberService memberService;

  @Transactional(readOnly = true)
  public MyPageResponse getMyPageData(UUID memberKey) {
    // 1. 회원 정보 조회
    MyPageMemberResponse memberInfo = memberService.getMyPageInfo(memberKey);

    // 2. 작성한 게시글 조회
    var posts =
        memberRepository.findMyPosts(memberKey).stream().map(FeedFindResponse::from).toList();

    // 3. 북마크한 게시글 조회
    var bookmarks =
        memberRepository.findMyBookmarks(memberKey).stream().map(FeedFindResponse::from).toList();

    // 4. 획득한 배지 조회
    var badges =
        memberRepository.findMyBadges(memberKey).stream().map(BadgeResponse::from).toList();

    return new MyPageResponse(memberInfo, posts, bookmarks, badges);
  }
}
