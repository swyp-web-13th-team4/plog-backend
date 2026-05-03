package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

  @Modifying
  @Query("update Post p set p.likes = p.likes +1 where p.id = :postId")
  int increaseLikeCount(Long postId);

  @Modifying
  @Query("update Post p set p.likes = p.likes -1 where p.id = :postId")
  int decreaseLikeCount(Long postId);
}
