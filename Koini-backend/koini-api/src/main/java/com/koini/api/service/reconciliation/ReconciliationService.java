package com.koini.api.service.reconciliation;

import com.koini.api.dto.response.ReconciliationResponse;
import com.koini.core.domain.enums.TransactionType;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.WalletRepository;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class ReconciliationService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;

  public ReconciliationService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
  }

  /**
   * Runs nightly reconciliation at 02:00.
   */
  @Scheduled(cron = "0 0 2 * * *")
  public void runNightlyReconciliation() {
    computeReport(LocalDate.now());
  }

  /**
   * Returns reconciliation report for a given date.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public ReconciliationResponse getReport(LocalDate date) {
    return computeReport(date);
  }

  private ReconciliationResponse computeReport(LocalDate date) {
    long totalBalance = walletRepository.sumBalances();
    long topups = transactionRepository.sumByTypeOnDate(TransactionType.TOPUP, date);
    long fares = transactionRepository.sumByTypeOnDate(TransactionType.FARE_PAYMENT, date);
    long withdrawals = transactionRepository.sumByTypeOnDate(TransactionType.WITHDRAWAL, date);
    long transfers = transactionRepository.sumByTypeOnDate(TransactionType.TRANSFER, date);
    long net = topups - fares - withdrawals;
    long discrepancy = net - totalBalance;
    String status = discrepancy == 0 ? "BALANCED" : "DISCREPANCY";
    return new ReconciliationResponse(date.toString(), topups, fares, withdrawals, transfers, net, discrepancy, status);
  }
}
