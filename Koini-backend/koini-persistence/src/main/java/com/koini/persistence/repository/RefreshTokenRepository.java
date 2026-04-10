package com.koini.persistence.repository;

import com.koini.core.domain.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findByUserUserIdAndRevokedFalse(UUID userId);

  long deleteByUserUserId(UUID userId);

  long deleteByExpiresAtBefore(LocalDateTime time);
}
