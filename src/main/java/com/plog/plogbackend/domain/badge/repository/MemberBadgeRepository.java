package com.plog.plogbackend.domain.badge.repository;

import com.plog.plogbackend.domain.badge.entity.MemberBadge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {

  /** 특정 회원이 특정 뱃지를 이미 보유하고 있는지 확인 */
  boolean existsByMemberIdAndBadgeId(Long memberId, Long badgeId);

  /** 특정 회원의 모든 획득 뱃지 조회 */
  List<MemberBadge> findAllByMemberId(Long memberId);

  @Modifying
  @Query("DELETE FROM MemberBadge mb WHERE mb.member.id = :memberId")
  void deleteAllByMemberId(Long memberId);
}
