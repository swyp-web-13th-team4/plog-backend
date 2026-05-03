package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

  @Modifying
  @Query("update Post p set p.likes = p.likes +1 where p.id = :posdId")
  int increaseLikeCount(Long postId);

  @Modifying
  @Query("update Post p set p.likes = p.likes -1 where p.id = :posdId")
  int decreaseLikeCount(Long postId);

  /** 특정 회원의 전체 게시글 수 조회 (첫 게시글 뱃지 부여 조건 판단용) */
  long countByMemberId(Long memberId);
}
