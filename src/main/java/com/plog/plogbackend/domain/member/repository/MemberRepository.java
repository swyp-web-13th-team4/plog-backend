package com.plog.plogbackend.domain.member.repository;

import com.plog.plogbackend.domain.member.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
  Optional<Member> findByProviderId(String providerId);

  Optional<Member> findByMemberKey(UUID memberKey);

  boolean existsByNickname(String nickname);

  // 어드민용 검색 및 페이징 (닉네임 포함 검색)
  org.springframework.data.domain.Page<Member> findByNicknameContaining(String nickname, org.springframework.data.domain.Pageable pageable);

  // 어드민용 통계 카운트 쿼리
  long countByRole(com.plog.plogbackend.domain.member.enums.Role role);
  long countByCreatedAtAfter(java.time.LocalDateTime createdAt);
  long countByStatus(com.plog.plogbackend.global.common.Enum.EntityStatus status);
}
