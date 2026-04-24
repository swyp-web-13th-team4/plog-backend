package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.post.controller.dto.response.FeedDetailResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.post.service.dto.FeedDetailCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedFindCommand;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

  private final PostRepository postRepository;

  // 피드 조회

  public List<FeedFindResponse> feedFind(FeedFindCommand command) {

    List<Post> feeds = postRepository.findAllByFeed(command.createAt(), command.lastPostId());

    return feeds.stream().map(FeedFindResponse::from).toList();
  }

  // 피드 상세 조회

  public FeedDetailResponse feedDetail(FeedDetailCommand command) {

    boolean isAuthor = false;

    Post post =
        postRepository
            .findById(command.postId())
            .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));

    if (command.memberId().equals(post.getMember().getId())) {

      isAuthor = true;
    }

    return FeedDetailResponse.from(post, isAuthor);
  }
}
