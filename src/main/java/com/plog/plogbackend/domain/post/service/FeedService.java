package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.dto.response.*;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.badge.event.BadgeGrantEvent;
import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.post.controller.dto.response.*;
import com.plog.plogbackend.domain.post.entity.Like;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.repository.LikeRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.post.service.dto.FeedDetailCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedFindCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedMyPageCommand;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

  /** 첫 북마크 뱃지 ID */
  private static final long BADGE_ID_FIRST_BOOKMARK = 3L;

  private final PostRepository postRepository;
  private final MemberRepository memberRepository;
  private final BookMarkRepository bookMarkRepository;
  private final LikeRepository likeRepository;

  // 이벤트 처리 객체
  private final ApplicationEventPublisher eventPublisher;

  // 피드 조회

  public FeedResponse feedFind(FeedFindCommand command, UUID memberKey) {

    Member member =
        (memberKey != null) ? memberRepository.findByMemberKey(memberKey).orElse(null) : null;
    int pageSize = 10;

    List<Post> feeds =
        new ArrayList<>(
            postRepository.findAllByFeed( // command.createAt(),
                command.lastPostId(), pageSize + 1));
    List<Long> postIds = feeds.stream().map(Post::getId).toList();

    Set<Long> likedPosts;
    Set<Long> bookMarks;
    Set<Long> isAuthors;
    if (member != null) {
      likedPosts = new HashSet<>(postRepository.checkLikes(member.getId(), postIds));
      bookMarks = new HashSet<>(postRepository.checkBookmarks(member.getId(), postIds));
      isAuthors = new HashSet<>(postRepository.checkMembers(member.getId(), postIds));
    } else {
      likedPosts = Collections.emptySet();
      bookMarks = Collections.emptySet();
      isAuthors = Collections.emptySet();
    }
    boolean nextPage = feeds.size() > pageSize;

    if (nextPage) {
      feeds.remove(pageSize);
    }
    List<FeedFindResponse> content =
        feeds.stream()
            .map(
                post -> {
                  boolean isAuthor = isAuthors.contains(post.getId());
                  boolean isLiked = likedPosts.contains(post.getId());
                  boolean isBookMarked = bookMarks.contains(post.getId());
                  return FeedFindResponse.from(post, isLiked, isBookMarked, isAuthor);
                })
            .toList();

    Long lastPostId = null;
    LocalDateTime nextCreatedAt = null;

    if (!content.isEmpty()) {
      Post post = feeds.get(feeds.size() - 1);
      lastPostId = post.getId();
      nextCreatedAt = post.getCreatedAt();
    }
    return new FeedResponse(content, lastPostId, nextCreatedAt);
  }

  // 피드 상세 조회

  public FeedDetailResponse feedDetail(FeedDetailCommand command, UUID memberKey) {

    boolean isAuthor = false;
    boolean isLiked = false;
    boolean isBookMarked = false;
    Post post =
        postRepository
            .findById(command.postId())
            .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));

    if (memberKey != null) {
      Member member =
          memberRepository
              .findByMemberKey(memberKey)
              .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

      if (member.getId().equals(post.getMember().getId())) {

        isAuthor = true;
      }

      isLiked = likeRepository.existsByMemberIdAndPostId(member.getId(), post.getId());
      isBookMarked = bookMarkRepository.existsByMemberIdAndPostId(member.getId(), post.getId());
    }
    String categoryName = post.getPlaceCategory().getCategoryName().getLabel();

    return FeedDetailResponse.from(post, isAuthor, isLiked, isBookMarked, categoryName);
  }

  @Transactional
  public UpdateBookMarked bookmarked(Long postId, UUID memberKey) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    Optional<BookMark> bookMark =
        bookMarkRepository.findByPostIdAndMemberId(postId, member.getId());

    if (bookMark.isEmpty()) {

      Post post =
          postRepository
              .findById(postId)
              .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));
      BookMark newBookMark = new BookMark(member, post);

      bookMarkRepository.save(newBookMark);

      // 첫 북마크 뱃지(id:3) 부여: 저장 후 전체 북마크 수가 1개이면 최초 북마크
      long totalBookmarks = bookMarkRepository.countByMemberId(member.getId());
      // 첫 북마크 뱃지(id:3) 부여 이벤트 발행
      // - 트랜잭션 커밋 후 BadgeEventHandler가 독립 트랜잭션으로 처리
      if (totalBookmarks == 1) {
        eventPublisher.publishEvent(new BadgeGrantEvent(member.getId(), BADGE_ID_FIRST_BOOKMARK));
      }

      return new UpdateBookMarked(true);

    } else {
      BookMark mark = bookMark.get();
      bookMarkRepository.delete(mark);

      return new UpdateBookMarked(false);
    }
  }

  @Transactional
  public UpdateLiked updateLiked(Long postId, UUID memberKey) {
    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    Optional<Like> likes = likeRepository.findByPostIdAndMemberId(postId, member.getId());

    if (likes.isEmpty()) {

      Post post =
          postRepository
              .findById(postId)
              .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));
      Like newLike = new Like(member, post);

      likeRepository.save(newLike);
      postRepository.increaseLikeCount(postId);

      return new UpdateLiked(true);

    } else {
      Like like = likes.get();
      likeRepository.delete(like);
      postRepository.decreaseLikeCount(postId);

      return new UpdateLiked(false);
    }
  }

  // 다른 유저의 피드+유저정보 조회
  @Transactional(readOnly = true)
  public FeedUserResponse memberProfileView(FeedMyPageCommand command, UUID loggedInMemberKey) {

    Member targetMember =
        memberRepository
            .findByMemberKey(command.memberKey())
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    // 메인 뱃지 불러오기
    MemberBadgeResponse badgeResponse = null;
    if (targetMember.getMainBadge() != null) {
      badgeResponse = MemberBadgeResponse.of(targetMember.getMainBadge(), true);
    }

    MemberResponse memberInfo =
        MemberResponse.builder()
            .nickname(targetMember.getNickname())
            .profileImageUrl(targetMember.getProfileImage())
            .introduction(targetMember.getIntroduction())
            .mainBadge(badgeResponse)
            .build();

    // 대상 유저의 게시글 조회
    List<Post> feeds = memberRepository.findMyPostsSorted(command.memberKey(), "latest", null);

    // 로그인한 유저(자신) 기준으로 좋아요/북마크 여부 확인
    Member loggedInMember = null;
    if (loggedInMemberKey != null) {
      loggedInMember = memberRepository.findByMemberKey(loggedInMemberKey).orElse(null);
    }

    List<Long> postIds = feeds.stream().map(Post::getId).toList();
    Set<Long> likedPosts;
    Set<Long> bookMarks;

    if (loggedInMember != null && !postIds.isEmpty()) {
      likedPosts = new HashSet<>(postRepository.checkLikes(loggedInMember.getId(), postIds));
      bookMarks = new HashSet<>(postRepository.checkBookmarks(loggedInMember.getId(), postIds));
    } else {
      likedPosts = Collections.emptySet();
      bookMarks = Collections.emptySet();
    }

    List<MyPageFeedResponse> posts =
        feeds.stream()
            .map(
                post -> {
                  boolean isLiked = likedPosts.contains(post.getId());
                  boolean isBookMarked = bookMarks.contains(post.getId());
                  return MyPageFeedResponse.from(post, isLiked, isBookMarked);
                })
            .toList();

    return new FeedUserResponse(memberInfo, posts);
  }

  // 다른 유저의 게시글만 정렬(최신순, 집중도순, 작업시간순)해서 조회
  @Transactional(readOnly = true)
  public MyPagePostsListResponse getOtherUserPostsSorted(
      UUID targetMemberKey, UUID loggedInMemberKey, String sort) {

    // 대상 유저의 게시글 조회 (정렬 포함, 태그 조건은 null)
    List<Post> feeds = memberRepository.findMyPostsSorted(targetMemberKey, sort, null);

    // 로그인한 유저 기준으로 좋아요/북마크 여부 확인
    Member loggedInMember = null;
    if (loggedInMemberKey != null) {
      loggedInMember = memberRepository.findByMemberKey(loggedInMemberKey).orElse(null);
    }

    List<Long> postIds = feeds.stream().map(Post::getId).toList();
    Set<Long> likedPosts;
    Set<Long> bookMarks;

    if (loggedInMember != null && !postIds.isEmpty()) {
      likedPosts = new HashSet<>(postRepository.checkLikes(loggedInMember.getId(), postIds));
      bookMarks = new HashSet<>(postRepository.checkBookmarks(loggedInMember.getId(), postIds));
    } else {
      likedPosts = Collections.emptySet();
      bookMarks = Collections.emptySet();
    }

    List<MyPageFeedResponse> posts =
        feeds.stream()
            .map(
                post -> {
                  boolean isLiked = likedPosts.contains(post.getId());
                  boolean isBookMarked = bookMarks.contains(post.getId());
                  return MyPageFeedResponse.from(post, isLiked, isBookMarked);
                })
            .toList();

    return new MyPagePostsListResponse(posts);
  }
}
