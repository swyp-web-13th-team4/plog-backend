package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.Like;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

  Optional<Like> findByPostIdAndMemberId(Long postId, Long memberId);

  Boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}
