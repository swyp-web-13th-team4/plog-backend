package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.dto.response.MemberAnalyticsResponse;
import com.plog.plogbackend.domain.Member.dto.response.MemberResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPageBadgeResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPageBookmarkResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPagePostsListResponse;
import com.plog.plogbackend.domain.Member.dto.response.MyPagePostsResponse;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.badge.dto.BadgeResponse;
import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.domain.badge.repository.BadgeRepository;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

  private final MemberRepository memberRepository;
  private final MemberService memberService;
  private final BadgeRepository badgeRepository;
  private final PostRepository postRepository;
  private final MemberAnalyticsService memberAnalyticsService;

  /**
   * GET /api/members/mypage 회원 기본 정보 + 내가 작성한 게시글 목록을 반환합니다.
   *
   * @param memberKey 회원 UUID
   * @return 회원 정보 + 게시글 목록
   */
  @Transactional(readOnly = true)
  public MyPagePostsResponse getMyPageData(UUID memberKey) {
    MemberResponse memberInfo = memberService.getMyPageInfo(memberKey);
    Member member = getMember(memberKey);

    List<Post> feeds = memberRepository.findMyPosts(memberKey);
    List<FeedFindResponse> posts = createFeedFindResponses(member, feeds);

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
    Member member = getMember(memberKey);

    List<Post> feeds = memberRepository.findMyBookmarks(memberKey);
    List<FeedFindResponse> bookmarks = createFeedFindResponses(member, feeds);

    return new MyPageBookmarkResponse(bookmarks);
  }

  /**
   * GET /api/members/mypage/posts 내가 작성한 게시글 목록을 정렬 및 태그 조건에 따라 반환합니다.
   *
   * @param memberKey 회원 UUID
   * @param sort 정렬 조건 (latest, focus, studyTime)
   * @param tags 필터링할 태그 목록 (선택)
   * @return 정렬/필터링된 게시글 목록
   */
  @Transactional(readOnly = true)
  public MyPagePostsListResponse getMyPostsSorted(
      UUID memberKey, String sort, List<PlaceTag> tags) {
    Member member = getMember(memberKey);

    List<Post> feeds = memberRepository.findMyPostsSorted(memberKey, sort, tags);
    List<FeedFindResponse> posts = createFeedFindResponses(member, feeds);

    return new MyPagePostsListResponse(posts);
  }

  /**
   * GET /api/members/bookmark/sorted 내가 북마크한 게시글 목록을 정렬 및 태그 조건에 따라 반환합니다.
   *
   * @param memberKey 회원 UUID
   * @param sort 정렬 조건 (latest, likes)
   * @param tags 필터링할 태그 목록 (선택)
   * @return 정렬/필터링된 북마크 게시글 목록
   */
  @Transactional(readOnly = true)
  public MyPageBookmarkResponse getMyBookmarksSorted(
      UUID memberKey, String sort, List<PlaceTag> tags) {
    Member member = getMember(memberKey);

    List<Post> feeds = memberRepository.findMyBookmarksSorted(memberKey, sort, tags);
    List<FeedFindResponse> bookmarks = createFeedFindResponses(member, feeds);

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

  /**
   * GET /api/members/analytics 회원 분석 정보를 반환합니다.
   *
   * @param memberKey 회원 UUID
   * @return 5가지 분석 결과
   */
  @Transactional(readOnly = true)
  public MemberAnalyticsResponse getMemberAnalytics(UUID memberKey) {
    return memberAnalyticsService.getAnalytics(memberKey);
  }

  /**
   * PATCH /api/members/badge/main 대표 뱃지를 변경합니다.
   *
   * <p>해당 뱃지가 존재하는지, 로그인한 회원이 해당 뱃지를 보유하고 있는지 검증한 뒤 {@link
   * com.plog.plogbackend.domain.Member.Member#updateMainBadge(Badge)}를 호출합니다.
   *
   * @param memberKey 회원 UUID
   * @param badgeId 대표로 설정할 뱃지 PK
   */
  @Transactional
  public void updateMainBadge(UUID memberKey, Long badgeId) {
    // 1. 뱃지 존재 여부 확인
    Badge badge =
        badgeRepository
            .findById(badgeId)
            .orElseThrow(() -> new AppException(ErrorType.BADGE_NOT_FOUND));

    // 2. 사용자가 해당 뱃지를 보유하는지 확인
    if (!memberRepository.existsMemberBadge(memberKey, badgeId)) {
      throw new AppException(ErrorType.BADGE_NOT_OWNED);
    }

    // 3. 대표 뱃지 업데이트
    Member member = getMember(memberKey);
    member.updateMainBadge(badge);
  }

  private Member getMember(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
  }

  private List<FeedFindResponse> createFeedFindResponses(Member member, List<Post> feeds) {
    List<Long> postIds = feeds.stream().map(Post::getId).toList();

    Set<Long> likedPosts;
    Set<Long> bookMarks;

    if (!postIds.isEmpty()) {
      likedPosts = new HashSet<>(postRepository.checkLikes(member.getId(), postIds));
      bookMarks = new HashSet<>(postRepository.checkBookmarks(member.getId(), postIds));
    } else {
      likedPosts = Collections.emptySet();
      bookMarks = Collections.emptySet();
    }

    return feeds.stream()
        .map(
            post -> {
              boolean isLiked = likedPosts.contains(post.getId());
              boolean isBookMarked = bookMarks.contains(post.getId());
              return FeedFindResponse.from(post, isLiked, isBookMarked);
            })
        .toList();
  }
}
