package com.plog.plogbackend.domain.block.repository;

import com.plog.plogbackend.domain.block.entity.Block;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BlockRepository extends JpaRepository<Block, Long> {

  /** 차단 해제 시 대상 조회 */
  Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

  /** 차단 여부 확인 (feedDetail, profileView 접근 차단 체크용) */
  boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

  /** 차단 목록 조회 (마이페이지 차단 목록 API용) */
  List<Block> findAllByBlockerId(Long blockerId);

  /** 피드 필터링용 — 차단된 멤버 ID 목록 조회 */
  @Query("SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :blockerId")
  List<Long> findBlockedMemberIdsByBlockerId(Long blockerId);

  /** 회원 탈퇴 시 연관 차단 데이터 일괄 삭제 */
  @Modifying
  @Query("DELETE FROM Block b WHERE b.blocker.id = :memberId OR b.blocked.id = :memberId")
  void deleteAllByMemberId(Long memberId);
}
