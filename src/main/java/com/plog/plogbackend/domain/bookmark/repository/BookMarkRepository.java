package com.plog.plogbackend.domain.bookmark.repository;

import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {

  Optional<BookMark> findByPostIdAndMemberId(Long postId, Long memberId);

  Boolean existsByMemberIdAndPostId(Long member_id, Long post_id);
}
