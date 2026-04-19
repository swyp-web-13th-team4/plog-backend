package com.plog.plogbackend.security.jwt;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByMemberKey(UUID memberKey);

  void deleteByMemberKey(UUID memberKey);
}
