package com.koini.persistence.repository;

import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
  Page<Transaction> findByFromWalletOrToWalletOrderByCreatedAtDesc(
      Wallet from, Wallet to, Pageable pageable);

  default long sumByTypeToday(TransactionType type) {
    LocalDate today = LocalDate.now();
    return sumByTypeBetween(type, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
  }

  default long sumByTypeOnDate(TransactionType type, LocalDate date) {
    return sumByTypeBetween(type, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
  }

  default long sumWithdrawalsForWalletToday(Wallet wallet) {
    LocalDate today = LocalDate.now();
    return sumWithdrawalsForWalletBetween(wallet, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
  }

  default long sumByTypeTodayForInitiator(UUID userId, TransactionType type) {
    LocalDate today = LocalDate.now();
    return sumByTypeForInitiatorBetween(userId, type, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
  }

  @Query("SELECT COALESCE(SUM(t.amountKc), 0) FROM Transaction t "
      + "WHERE t.txType = :type AND t.status = 'COMPLETED' "
      + "AND t.createdAt >= :start AND t.createdAt < :end")
  long sumByTypeBetween(@Param("type") TransactionType type,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("SELECT COALESCE(SUM(t.amountKc), 0) FROM Transaction t "
      + "WHERE t.txType = 'WITHDRAWAL' AND t.fromWallet = :wallet "
      + "AND t.createdAt >= :start AND t.createdAt < :end")
  long sumWithdrawalsForWalletBetween(@Param("wallet") Wallet wallet,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("SELECT COALESCE(SUM(t.amountKc), 0) FROM Transaction t "
      + "WHERE t.txType = :type AND t.initiatedBy.userId = :userId "
      + "AND t.createdAt >= :start AND t.createdAt < :end")
  long sumByTypeForInitiatorBetween(@Param("userId") UUID userId,
      @Param("type") TransactionType type,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
