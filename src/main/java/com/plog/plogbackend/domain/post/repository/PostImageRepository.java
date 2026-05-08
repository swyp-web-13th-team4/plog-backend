package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.PostImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

  List<PostImage> findAllByPostId(Long postId); // 게시글 ID로 조회

  int countByPostId(Long postId); // 게시글 이미지 개수 조회

  void deleteAllByPostId(Long postId); // 게시글 모든 이미지 삭제

  List<PostImage> findAllByPostIdIn(List<Long> postIds);

  @Modifying
  @Query("DELETE FROM PostImage pi WHERE pi.post.id IN :postIds")
  void deleteAllByPostIdIn(List<Long> postIds);
}
