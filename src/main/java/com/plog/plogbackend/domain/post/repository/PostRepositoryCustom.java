package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.Post;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepositoryCustom {
  // 날짜 기준 검색
  List<Post> summary(PostSearch search);

  // 전체 작업 시간
  Integer sum(Long memberId, PostSearch search);

  // 작성한 글의 수
  Long count(Long memberId, PostSearch search);

  // 피드 글 조회

  List<Post> findAllByFeed(LocalDateTime lastStudyDate, Long lastPostId);

  // 북마크
  List<Post> bookMarkPost(Long memberId);
}
