package com.plog.plogbackend.domain.bookmark.repository;

import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {

  Optional<BookMark> findByPostIdAndMemberId(Long postId, Long memberId);

  Boolean existsByMemberIdAndPostId(Long member_id, Long post_id);

  /** 특정 회원의 전체 북마크 수 조회 (첫 북마크 뱃지 부여 조건 판단용) */
  long countByMemberId(Long memberId);
}
