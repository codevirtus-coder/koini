package com.koini.api.service.wallet;

import com.koini.api.dto.request.TopUpRequest;
import com.koini.api.dto.request.TransactionHistoryFilter;
import com.koini.api.dto.request.TransferRequest;
import com.koini.api.dto.response.TopUpResponse;
import com.koini.api.dto.response.TransactionHistoryResponse;
import com.koini.api.dto.response.TransactionResponse;
import com.koini.api.dto.response.TransferResponse;
import com.koini.api.dto.response.WalletBalanceResponse;
import com.koini.api.mapper.TransactionMapper;
import com.koini.api.mapper.WalletMapper;
import com.koini.api.service.AuditService;
import com.koini.api.service.auth.AuthService;
import com.koini.core.domain.entity.Agent;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.AuditOutcome;
import com.koini.core.domain.enums.TransactionStatus;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.valueobject.MoneyUtils;
import com.koini.core.domain.valueobject.PhoneUtils;
import com.koini.core.exception.AgentFloatExceededException;
import com.koini.core.exception.InsufficientBalanceException;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.core.exception.UserNotFoundException;
import com.koini.core.exception.WalletNotFoundException;
import com.koini.notification.sms.SmsService;
import com.koini.persistence.repository.AgentRepository;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;
  private final AgentRepository agentRepository;
  private final WalletMapper walletMapper;
  private final TransactionMapper transactionMapper;
  private final AuthService authService;
  private final SmsService smsService;
  private final AuditService auditService;

  public WalletService(
      WalletRepository walletRepository,
      TransactionRepository transactionRepository,
      UserRepository userRepository,
      AgentRepository agentRepository,
      WalletMapper walletMapper,
      TransactionMapper transactionMapper,
      AuthService authService,
      SmsService smsService,
      AuditService auditService
  ) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.userRepository = userRepository;
    this.agentRepository = agentRepository;
    this.walletMapper = walletMapper;
    this.transactionMapper = transactionMapper;
    this.authService = authService;
    this.smsService = smsService;
    this.auditService = auditService;
  }

  /**
   * Returns a user's wallet balance.
   */
  @PreAuthorize("isAuthenticated()")
  public WalletBalanceResponse getBalance(UUID userId) {
    Wallet wallet = walletRepository.findByUserUserId(userId)
        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    return walletMapper.toBalanceResponse(wallet);
  }

  /**
   * Tops up a passenger wallet (agent-only).
   */
  @PreAuthorize("hasRole('AGENT')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public TopUpResponse topUp(TopUpRequest request, UUID agentUserId, HttpServletRequest httpRequest) {
    Agent agent = agentRepository.findByUserUserId(agentUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
    if (agent.getFloatBalanceKc() < request.amountKc()) {
      throw new AgentFloatExceededException("Agent float balance insufficient");
    }
    String normalized = PhoneUtils.normalize(request.holderPhone());
    User passenger = userRepository.findByPhone(normalized)
        .orElseThrow(() -> new UserNotFoundException("Passenger not found"));
    if (passenger.getRole().canonical() != UserRole.CLIENT) {
      throw new UserNotFoundException("Passenger not found");
    }
    Wallet passengerWallet = walletRepository.findByUserIdForUpdate(passenger.getUserId())
        .orElseThrow(() -> new WalletNotFoundException("Passenger wallet not found"));

    passengerWallet.setBalanceKc(passengerWallet.getBalanceKc() + request.amountKc());
    agent.setFloatBalanceKc(agent.getFloatBalanceKc() - request.amountKc());

    String reference = generateReference("TOPUP");
    Transaction tx = Transaction.builder()
        .txType(TransactionType.TOPUP)
        .fromWallet(null)
        .toWallet(passengerWallet)
        .amountKc(request.amountKc())
        .feeKc(0)
        .status(TransactionStatus.COMPLETED)
        .reference(reference)
        .initiatedBy(agent.getUser())
        .description("Agent top-up")
        .build();
    transactionRepository.save(tx);

    smsService.sendTopUpConfirmation(passenger.getPhone(), request.amountKc(), passengerWallet.getBalanceKc());
    auditService.log("TOPUP", agentUserId, "AGENT", "Transaction", tx.getTxId().toString(),
        null, tx, AuditOutcome.SUCCESS, httpRequest);

    return new TopUpResponse(
        tx.getTxId().toString(),
        reference,
        request.amountKc(),
        MoneyUtils.formatUsd(request.amountKc()),
        passengerWallet.getBalanceKc(),
        PhoneUtils.mask(passenger.getPhone()));
  }

  /**
   * Transfers credits between passengers.
   */
  @PreAuthorize("hasAnyRole('CLIENT','PASSENGER')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public TransferResponse transfer(TransferRequest request, UUID fromUserId) {
    authService.verifyPin(request.pin(), fromUserId);
    String normalized = PhoneUtils.normalize(request.toPhone());
    User toUser = userRepository.findByPhone(normalized)
        .orElseThrow(() -> new UserNotFoundException("Recipient not found"));
    if (toUser.getUserId().equals(fromUserId)) {
      throw new ResourceNotFoundException("Cannot transfer to self");
    }

    Wallet fromWallet = walletRepository.findByUserIdForUpdate(fromUserId)
        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    Wallet toWallet = walletRepository.findByUserIdForUpdate(toUser.getUserId())
        .orElseThrow(() -> new WalletNotFoundException("Recipient wallet not found"));

    List<Wallet> ordered = List.of(fromWallet, toWallet).stream()
        .sorted(Comparator.comparing(Wallet::getWalletId))
        .toList();
    walletRepository.findById(ordered.get(0).getWalletId());
    walletRepository.findById(ordered.get(1).getWalletId());

    if (fromWallet.getBalanceKc() < request.amountKc()) {
      throw new InsufficientBalanceException("Insufficient balance");
    }
    fromWallet.setBalanceKc(fromWallet.getBalanceKc() - request.amountKc());
    toWallet.setBalanceKc(toWallet.getBalanceKc() + request.amountKc());

    String reference = generateReference("TRANSFER");
    Transaction tx = Transaction.builder()
        .txType(TransactionType.TRANSFER)
        .fromWallet(fromWallet)
        .toWallet(toWallet)
        .amountKc(request.amountKc())
        .feeKc(0)
        .status(TransactionStatus.COMPLETED)
        .reference(reference)
        .initiatedBy(fromWallet.getUser())
        .description("Wallet transfer")
        .build();
    transactionRepository.save(tx);

    smsService.sendGenericAlert(fromWallet.getUser().getPhone(),
        "Sent " + MoneyUtils.formatUsd(request.amountKc()) + " to " + PhoneUtils.mask(toUser.getPhone()));
    smsService.sendGenericAlert(toUser.getPhone(),
        "Received " + MoneyUtils.formatUsd(request.amountKc()) + " from " + PhoneUtils.mask(fromWallet.getUser().getPhone()));

    return new TransferResponse(
        tx.getTxId().toString(),
        reference,
        request.amountKc(),
        MoneyUtils.formatUsd(request.amountKc()),
        PhoneUtils.mask(toUser.getPhone()),
        fromWallet.getBalanceKc());
  }

  /**
   * Returns transaction history for a user's wallet.
   */
  @PreAuthorize("isAuthenticated()")
  public TransactionHistoryResponse getTransactionHistory(UUID userId, TransactionHistoryFilter filter) {
    Wallet wallet = walletRepository.findByUserUserId(userId)
        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    int page = Math.max(filter.page(), 0);
    int size = filter.size() > 0 && filter.size() <= 50 ? filter.size() : 20;
    Pageable pageable = PageRequest.of(page, size);
    Page<Transaction> pageResult = transactionRepository
        .findByFromWalletOrToWalletOrderByCreatedAtDesc(wallet, wallet, pageable);
    List<TransactionResponse> responses = transactionMapper.toResponseList(pageResult.getContent());
    return new TransactionHistoryResponse(responses, page, size, pageResult.getTotalElements());
  }

  private String generateReference(String prefix) {
    String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    return prefix + "-" + System.currentTimeMillis() + "-" + suffix;
  }
}
