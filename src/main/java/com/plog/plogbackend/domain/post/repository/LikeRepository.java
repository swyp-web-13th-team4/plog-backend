package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LikeRepository extends JpaRepository<Like, Long> {

  Optional<Like> findByPostIdAndMemberId(Long postId, Long memberId);

  Boolean existsByMemberIdAndPostId(Long memberId, Long postId);

  @Modifying
  @Query("DELETE FROM Like l WHERE l.member.id = :memberId")
  void deleteAllByMemberId(Long memberId);

  @Modifying
  @Query("DELETE FROM Like l WHERE l.post.id IN :postIds")
  void deleteAllByPostIdIn(List<Long> postIds);
}
