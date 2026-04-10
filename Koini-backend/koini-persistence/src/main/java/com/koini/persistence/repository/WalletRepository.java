package com.koini.persistence.repository;

import com.koini.core.domain.entity.Wallet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
  @Query("SELECT w FROM Wallet w WHERE w.user.userId = :userId")
  Optional<Wallet> findByUserId(@Param("userId") UUID userId);

  Optional<Wallet> findByUserUserId(UUID userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT w FROM Wallet w WHERE w.user.userId = :userId")
  Optional<Wallet> findByUserIdForUpdate(@Param("userId") UUID userId);

  @Query("SELECT COALESCE(SUM(w.balanceKc), 0) FROM Wallet w")
  long sumBalances();
}
