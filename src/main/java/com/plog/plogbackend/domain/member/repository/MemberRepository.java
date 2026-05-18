package com.plog.plogbackend.domain.member.repository;

import com.plog.plogbackend.domain.member.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
  Optional<Member> findByProviderId(String providerId);

  Optional<Member> findByMemberKey(UUID memberKey);

  boolean existsByNickname(String nickname);
}
