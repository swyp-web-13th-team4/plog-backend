package com.plog.plogbackend.domain.Member;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByProviderId(String providerId);

  Optional<Member> findByMemberKey(UUID memberKey);
}
