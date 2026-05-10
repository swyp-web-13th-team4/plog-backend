package com.plog.plogbackend.security.jwt;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT r FROM RefreshToken r WHERE r.token = :token")
  Optional<RefreshToken> findByTokenWithLock(@org.springframework.data.repository.query.Param("token") String token);

  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByMemberKey(UUID memberKey);

  void deleteByMemberKey(UUID memberKey);
}
