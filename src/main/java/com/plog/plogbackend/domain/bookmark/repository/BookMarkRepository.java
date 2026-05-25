package com.plog.plogbackend.domain.bookmark.repository;

import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {

  Optional<BookMark> findByPostIdAndMemberId(Long postId, Long memberId);

  Boolean existsByMemberIdAndPostId(Long member_id, Long post_id);

  /** 특정 회원의 전체 북마크 수 조회 (첫 북마크 뱃지 부여 조건 판단용) */
  long countByMemberId(Long memberId);

  @Query(
      """
      select case when count(b) > 0 then true else false end
      from BookMark b
      join b.post p
      where b.member.id = :memberId
        and p.place.id = :placeId
      """)
  boolean existsByMemberIdAndPlaceId(Long memberId, Long placeId);

  @Modifying
  @Query("DELETE FROM BookMark b WHERE b.member.id = :memberId")
  void deleteAllByMemberId(Long memberId);

  @Modifying
  @Query("DELETE FROM BookMark b WHERE b.post.id IN :postIds")
  void deleteAllByPostIdIn(List<Long> postIds);
}
