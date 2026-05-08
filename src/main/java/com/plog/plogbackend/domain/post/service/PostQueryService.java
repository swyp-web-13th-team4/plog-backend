package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.post.controller.dto.response.PostImageResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.PostQueryResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.PostRequestPartResponse;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

  private final PostRepository postRepository;

  public PostQueryResponse getPost(Long postId, UUID memberKey) {

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));

    // 작성자 본인만 수정용 조회 가능
    if (!post.getMember().getMemberKey().equals(memberKey)) {
      throw new AppException(ErrorType.POST_FORBIDDEN);
    }

    PostRequestPartResponse postResponse = PostRequestPartResponse.from(post);
    PostImageResponse postImageResponse = PostImageResponse.from(post.getImages());

    return PostQueryResponse.of(postResponse, postImageResponse);
  }
}
