package com.koini.api.service.merchant;

import com.koini.api.dto.response.MerchantDashboardSummaryResponse;
import com.koini.api.dto.response.TransactionResponse;
import com.koini.api.dto.response.WalletBalanceResponse;
import com.koini.api.mapper.TransactionMapper;
import com.koini.api.service.money.MoneyConversionService;
import com.koini.api.service.wallet.WalletService;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.PaymentReqStatus;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.exception.WalletNotFoundException;
import com.koini.persistence.repository.PaymentRequestRepository;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.WalletRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class MerchantDashboardService {

  private final WalletService walletService;
  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final PaymentRequestRepository paymentRequestRepository;
  private final TransactionMapper transactionMapper;
  private final MoneyConversionService moneyConversionService;

  public MerchantDashboardService(
      WalletService walletService,
      WalletRepository walletRepository,
      TransactionRepository transactionRepository,
      PaymentRequestRepository paymentRequestRepository,
      TransactionMapper transactionMapper,
      MoneyConversionService moneyConversionService
  ) {
    this.walletService = walletService;
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.paymentRequestRepository = paymentRequestRepository;
    this.transactionMapper = transactionMapper;
    this.moneyConversionService = moneyConversionService;
  }

  public MerchantDashboardSummaryResponse summary(UUID merchantUserId, LocalDate date) {
    LocalDate resolved = date != null ? date : LocalDate.now();
    LocalDateTime start = resolved.atStartOfDay();
    LocalDateTime end = resolved.plusDays(1).atStartOfDay();

    WalletBalanceResponse balance = walletService.getBalance(merchantUserId);
    MerchantDashboardSummaryResponse.Wallet wallet = new MerchantDashboardSummaryResponse.Wallet(
        balance.balanceKc(),
        balance.balanceUsd(),
        balance.points(),
        balance.status()
    );

    long earningsKc = transactionRepository.sumByTypeForToWalletBetween(
        merchantUserId, TransactionType.FARE_PAYMENT, start, end);
    long faresCount = transactionRepository.countByTypeForToWalletBetween(
        merchantUserId, TransactionType.FARE_PAYMENT, start, end);
    LocalDateTime firstFareAt = transactionRepository.minCreatedAtByTypeForToWalletBetween(
        merchantUserId, TransactionType.FARE_PAYMENT, start, end);
    LocalDateTime lastFareAt = transactionRepository.maxCreatedAtByTypeForToWalletBetween(
        merchantUserId, TransactionType.FARE_PAYMENT, start, end);

    MerchantDashboardSummaryResponse.Today today = new MerchantDashboardSummaryResponse.Today(
        faresCount,
        earningsKc,
        moneyConversionService.formatUsd(earningsKc),
        firstFareAt != null ? firstFareAt.toString() : null,
        lastFareAt != null ? lastFareAt.toString() : null
    );

    long pendingCount = paymentRequestRepository.countForConductorBetweenWithStatus(
        merchantUserId, PaymentReqStatus.PENDING, start, end);
    LocalDateTime lastRequestAt = paymentRequestRepository.maxCreatedAtForConductorBetween(
        merchantUserId, start, end);
    MerchantDashboardSummaryResponse.Requests requests = new MerchantDashboardSummaryResponse.Requests(
        pendingCount,
        lastRequestAt != null ? lastRequestAt.toString() : null
    );

    Wallet walletEntity = walletRepository.findByUserUserId(merchantUserId)
        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    List<Transaction> recentTx = transactionRepository.findByFromWalletOrToWalletOrderByCreatedAtDesc(
        walletEntity, walletEntity, PageRequest.of(0, 10)).getContent();
    List<TransactionResponse> recentTransactions = transactionMapper.toResponseList(recentTx);

    return new MerchantDashboardSummaryResponse(wallet, today, requests, recentTransactions);
  }
}
