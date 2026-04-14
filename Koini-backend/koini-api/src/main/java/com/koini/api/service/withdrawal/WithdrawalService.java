package com.koini.api.service.withdrawal;

import com.koini.api.dto.request.ReverseWithdrawalRequest;
import com.koini.api.dto.request.WithdrawalRequest;
import com.koini.api.dto.response.WithdrawalResponse;
import com.koini.api.service.AuditService;
import com.koini.api.service.money.MoneyConversionService;
import com.koini.core.domain.entity.Agent;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.AuditOutcome;
import com.koini.core.domain.enums.TransactionStatus;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.domain.valueobject.PhoneUtils;
import com.koini.core.exception.AgentInsufficientCashException;
import com.koini.core.exception.InsufficientBalanceException;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.core.util.KoiniConstants;
import com.koini.notification.sms.SmsService;
import com.koini.persistence.repository.AgentRepository;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WithdrawalService {

  private final WalletRepository walletRepository;
  private final UserRepository userRepository;
  private final AgentRepository agentRepository;
  private final TransactionRepository transactionRepository;
  private final SmsService smsService;
  private final AuditService auditService;
  private final MoneyConversionService moneyConversionService;

  public WithdrawalService(
      WalletRepository walletRepository,
      UserRepository userRepository,
      AgentRepository agentRepository,
      TransactionRepository transactionRepository,
      SmsService smsService,
      AuditService auditService,
      MoneyConversionService moneyConversionService
  ) {
    this.walletRepository = walletRepository;
    this.userRepository = userRepository;
    this.agentRepository = agentRepository;
    this.transactionRepository = transactionRepository;
    this.smsService = smsService;
    this.auditService = auditService;
    this.moneyConversionService = moneyConversionService;
  }

  /**
   * Initiates a withdrawal by an agent.
   */
  @PreAuthorize("hasRole('AGENT')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public WithdrawalResponse initiateWithdrawal(WithdrawalRequest request, UUID agentUserId) {
    if (request.amountKc() < KoiniConstants.WITHDRAW_MIN_KC || request.amountKc() > KoiniConstants.WITHDRAW_MAX_KC) {
      throw new ResourceNotFoundException("Withdrawal amount out of range");
    }
    String normalized = PhoneUtils.normalize(request.passengerPhone());
    User passenger = userRepository.findByPhone(normalized)
        .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
    Wallet wallet = walletRepository.findByUserIdForUpdate(passenger.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    long dailySum = transactionRepository.sumWithdrawalsForWalletToday(wallet);
    if (dailySum + request.amountKc() > KoiniConstants.WITHDRAW_DAILY_LIMIT_KC) {
      throw new ResourceNotFoundException("Daily withdrawal limit exceeded");
    }
    if (wallet.getPoints() < request.amountKc()) {
      throw new InsufficientBalanceException("Insufficient balance");
    }
    Agent agent = agentRepository.findByUserUserId(agentUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
    BigDecimal amountUsd = moneyConversionService.toUsd(request.amountKc());
    if (agent.getCashHeldUsd().compareTo(amountUsd) < 0) {
      throw new AgentInsufficientCashException("Agent cash insufficient");
    }

    wallet.setPoints(wallet.getPoints() - request.amountKc());
    wallet.setBalanceKc(wallet.getPoints());
    agent.setCashHeldUsd(agent.getCashHeldUsd().subtract(amountUsd));

    String reference = "WD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
    Transaction tx = Transaction.builder()
        .txType(TransactionType.WITHDRAWAL)
        .fromWallet(wallet)
        .amountKc(request.amountKc())
        .feeKc(0)
        .status(TransactionStatus.PENDING)
        .reference(reference)
        .initiatedBy(agent.getUser())
        .description("Withdrawal initiated")
        .build();
    transactionRepository.save(tx);

    return new WithdrawalResponse(
        tx.getTxId().toString(),
        request.amountKc(),
        moneyConversionService.formatUsd(request.amountKc()),
        tx.getStatus().name(),
        LocalDateTime.now().plusMinutes(10).toString());
  }

  /**
   * Confirms a withdrawal once cash is handed over.
   */
  @PreAuthorize("hasRole('AGENT')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void confirmWithdrawal(String withdrawalId, UUID agentUserId, HttpServletRequest httpRequest) {
    Transaction tx = transactionRepository.findById(UUID.fromString(withdrawalId))
        .orElseThrow(() -> new ResourceNotFoundException("Withdrawal not found"));
    if (tx.getInitiatedBy() == null || !tx.getInitiatedBy().getUserId().equals(agentUserId)) {
      throw new ResourceNotFoundException("Withdrawal not owned by agent");
    }
    if (tx.getStatus() != TransactionStatus.PENDING) {
      throw new ResourceNotFoundException("Withdrawal not pending");
    }
    tx.setStatus(TransactionStatus.COMPLETED);
    smsService.sendWithdrawalConfirmation(tx.getFromWallet().getUser().getPhone(), tx.getAmountKc(), tx.getReference());
    auditService.log("WITHDRAWAL_CONFIRMED", agentUserId, "AGENT", "Transaction",
        tx.getTxId().toString(), null, tx, AuditOutcome.SUCCESS, httpRequest);
  }

  /**
   * Reverses a withdrawal within the allowed window.
   */
  @PreAuthorize("hasRole('AGENT')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void reverseWithdrawal(String withdrawalId, UUID agentUserId, ReverseWithdrawalRequest request,
      HttpServletRequest httpRequest) {
    Transaction tx = transactionRepository.findById(UUID.fromString(withdrawalId))
        .orElseThrow(() -> new ResourceNotFoundException("Withdrawal not found"));
    if (tx.getInitiatedBy() == null || !tx.getInitiatedBy().getUserId().equals(agentUserId)) {
      throw new ResourceNotFoundException("Withdrawal not owned by agent");
    }
    if (tx.getStatus() != TransactionStatus.PENDING) {
      throw new ResourceNotFoundException("Withdrawal not pending");
    }
    if (tx.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
      throw new ResourceNotFoundException("Reversal window expired");
    }
    Wallet wallet = tx.getFromWallet();
    wallet.setPoints(wallet.getPoints() + tx.getAmountKc());
    wallet.setBalanceKc(wallet.getPoints());
    Agent agent = agentRepository.findByUserUserId(agentUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
    agent.setCashHeldUsd(agent.getCashHeldUsd().add(moneyConversionService.toUsd(tx.getAmountKc())));
    tx.setStatus(TransactionStatus.REVERSED);
    smsService.sendGenericAlert(wallet.getUser().getPhone(),
        "Withdrawal of " + moneyConversionService.formatUsd(tx.getAmountKc()) + " reversed. Reason: " + request.reason());
    auditService.log("WITHDRAWAL_REVERSED", agentUserId, "AGENT", "Transaction",
        tx.getTxId().toString(), null, tx, AuditOutcome.SUCCESS, httpRequest);
  }
}
