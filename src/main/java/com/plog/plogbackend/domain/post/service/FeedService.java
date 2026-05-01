package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.post.controller.dto.response.*;
import com.plog.plogbackend.domain.post.entity.Like;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.repository.LikeRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.post.service.dto.FeedDetailCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedFindCommand;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

  private final PostRepository postRepository;
  private final MemberRepository memberRepository;
  private final BookMarkRepository bookMarkRepository;
  private final LikeRepository likeRepository;

  // 피드 조회

  public FeedResponse feedFind(FeedFindCommand command, UUID memberKey) {

    Member member =
        (memberKey != null) ? memberRepository.findByMemberKey(memberKey).orElse(null) : null;
    int pageSize = 10;

    List<Post> feeds =
        new ArrayList<>(
            postRepository.findAllByFeed(command.createAt(), command.lastPostId(), pageSize + 1));
    List<Long> postIds = feeds.stream().map(Post::getId).toList();

    Set<Long> likedPosts;
    Set<Long> bookMarks;
    if (member != null) {
      likedPosts = new HashSet<>(postRepository.checkLikes(member.getId(), postIds));
      bookMarks = new HashSet<>(postRepository.checkBookmarks(member.getId(), postIds));
    } else {
      likedPosts = Collections.emptySet();
      bookMarks = Collections.emptySet();
    }
    boolean nextPage = feeds.size() > pageSize;

    if (nextPage) {
      feeds.remove(pageSize);
    }
    List<FeedFindResponse> content =
        feeds.stream()
            .map(
                post -> {
                  boolean isLiked = likedPosts.contains(post.getId());
                  boolean isBookMarked = bookMarks.contains(post.getId());
                  return FeedFindResponse.from(post, isLiked, isBookMarked);
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

    Post post =
        postRepository
            .findById(command.postId())
            .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    if (member.getId().equals(post.getMember().getId())) {

      isAuthor = true;
    }
    boolean isLiked = likeRepository.existsByMemberIdAndPostId(member.getId(), post.getId());
    boolean isBookMarked =
        bookMarkRepository.existsByMemberIdAndPostId(member.getId(), post.getId());

    return FeedDetailResponse.from(post, isAuthor, isLiked, isBookMarked);
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

  //    public FeedMyPageResponse memberProfileView(FeedMyPageCommand command) {
  //
  //      Member member = memberRepository.findByMemberKey(command.memberKey()).orElseThrow(()->
  //              new AppException(ErrorType.MEMBER_NOT_FOUND));
  //
  //    }

}
