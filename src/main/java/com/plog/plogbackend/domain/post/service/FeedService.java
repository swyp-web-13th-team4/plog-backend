package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedDetailResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.UpdateBookMarked;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.post.service.dto.FeedDetailCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedFindCommand;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  // 피드 조회

  public FeedResponse feedFind(FeedFindCommand command) {

    int pageSize = 10;

    List<Post> feeds =
        postRepository.findAllByFeed(command.createAt(), command.lastPostId(), pageSize + 1);
    boolean nextPage = feeds.size() > pageSize;

    if (nextPage) {
      feeds.remove(pageSize);
    }
    List<FeedFindResponse> content = feeds.stream().map(FeedFindResponse::from).toList();
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

    return FeedDetailResponse.from(post, isAuthor);
  }

  @Transactional
  public UpdateBookMarked bookmarked(Long postId, UUID memberKey) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    Optional<BookMark> bookMark =
        bookMarkRepository.findByPostIdAndMemberId(member.getId(), postId);

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

  //    public FeedMyPageResponse memberProfileView(FeedMyPageCommand command) {
  //
  //      Member member = memberRepository.findByMemberKey(command.memberKey()).orElseThrow(()->
  //              new AppException(ErrorType.MEMBER_NOT_FOUND));
  //
  //    }

}
