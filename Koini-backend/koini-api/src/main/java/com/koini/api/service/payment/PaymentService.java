package com.koini.api.service.payment;

import com.koini.api.dto.request.ApproveRequestRequest;
import com.koini.api.dto.request.CreateRequestRequest;
import com.koini.api.dto.request.GenerateCodeRequest;
import com.koini.api.dto.request.RedeemCodeRequest;
import com.koini.api.dto.response.CreateRequestResponse;
import com.koini.api.dto.response.GenerateCodeResponse;
import com.koini.api.dto.response.PaymentRequestStatusResponse;
import com.koini.api.dto.response.RedeemCodeResponse;
import com.koini.api.dto.response.TransactionResponse;
import com.koini.api.mapper.TransactionMapper;
import com.koini.api.service.AuditService;
import com.koini.api.service.IdempotencyService;
import com.koini.api.service.RedisService;
import com.koini.api.service.auth.AuthService;
import com.koini.core.domain.entity.PaymentCode;
import com.koini.core.domain.entity.PaymentRequest;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.AuditOutcome;
import com.koini.core.domain.enums.PaymentCodeStatus;
import com.koini.core.domain.enums.PaymentReqStatus;
import com.koini.core.domain.enums.TransactionStatus;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.domain.valueobject.MoneyUtils;
import com.koini.core.domain.valueobject.PhoneUtils;
import com.koini.core.exception.DuplicatePaymentException;
import com.koini.core.exception.InsufficientBalanceException;
import com.koini.core.exception.PaymentCodeAlreadyUsedException;
import com.koini.core.exception.PaymentCodeExpiredException;
import com.koini.core.exception.PaymentCodeInvalidException;
import com.koini.core.exception.PaymentRequestExpiredException;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.core.exception.UserNotFoundException;
import com.koini.notification.sms.SmsService;
import com.koini.persistence.repository.PaymentCodeRepository;
import com.koini.persistence.repository.PaymentRequestRepository;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import com.koini.core.util.KoiniConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

  private final PaymentCodeRepository paymentCodeRepository;
  private final PaymentRequestRepository paymentRequestRepository;
  private final WalletRepository walletRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final AuthService authService;
  private final RedisService redisService;
  private final IdempotencyService idempotencyService;
  private final PasswordEncoder passwordEncoder;
  private final SmsService smsService;
  private final AuditService auditService;
  private final TransactionMapper transactionMapper;
  private final double feeRate;

  public PaymentService(
      PaymentCodeRepository paymentCodeRepository,
      PaymentRequestRepository paymentRequestRepository,
      WalletRepository walletRepository,
      UserRepository userRepository,
      TransactionRepository transactionRepository,
      AuthService authService,
      RedisService redisService,
      IdempotencyService idempotencyService,
      PasswordEncoder passwordEncoder,
      SmsService smsService,
      AuditService auditService,
      TransactionMapper transactionMapper,
      @Value("${koini.fees.payment-rate}") double feeRate
  ) {
    this.paymentCodeRepository = paymentCodeRepository;
    this.paymentRequestRepository = paymentRequestRepository;
    this.walletRepository = walletRepository;
    this.userRepository = userRepository;
    this.transactionRepository = transactionRepository;
    this.authService = authService;
    this.redisService = redisService;
    this.idempotencyService = idempotencyService;
    this.passwordEncoder = passwordEncoder;
    this.smsService = smsService;
    this.auditService = auditService;
    this.transactionMapper = transactionMapper;
    this.feeRate = feeRate;
  }

  /**
   * Generates a payment code for a client.
   */
  @PreAuthorize("hasAnyRole('CLIENT','PASSENGER')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public GenerateCodeResponse generatePaymentCode(GenerateCodeRequest request, UUID passengerId,
      String idempotencyKey) {
    if (idempotencyKey != null) {
      String key = "koini:idempotency:" + idempotencyKey;
      Optional<GenerateCodeResponse> cached = idempotencyService.get(key, GenerateCodeResponse.class);
      if (cached.isPresent()) {
        return cached.get();
      }
    }
    authService.verifyPin(request.pin(), passengerId);
    String rateKey = "koini:rate:pay:" + passengerId;
    long attempts = redisService.increment(rateKey);
    if (attempts == 1) {
      redisService.expire(rateKey, Duration.ofHours(1));
    }
    if (attempts > KoiniConstants.PAYMENT_CODE_RATE_LIMIT_PER_HOUR) {
      throw new DuplicatePaymentException("Payment code generation rate limit exceeded");
    }
    Wallet wallet = walletRepository.findByUserIdForUpdate(passengerId)
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    long feeKc = MoneyUtils.calculateFee(request.amountKc(), feeRate);
    long total = request.amountKc() + feeKc;
    if (wallet.getBalanceKc() < total) {
      throw new InsufficientBalanceException("Insufficient balance");
    }
    String code = generateSixDigitCode();
    String hash = passwordEncoder.encode(code);
    PaymentCode paymentCode = PaymentCode.builder()
        .codeHash(hash)
        .holder(wallet.getUser())
        .amountKc(request.amountKc())
        .feeKc(feeKc)
        .status(PaymentCodeStatus.PENDING)
        .expiresAt(LocalDateTime.now().plusSeconds(KoiniConstants.PAYMENT_CODE_TTL_SECONDS))
        .build();
    paymentCodeRepository.save(paymentCode);
    redisService.set("koini:code:" + code, paymentCode.getCodeId().toString(),        Duration.ofSeconds(KoiniConstants.PAYMENT_CODE_TTL_SECONDS));

    GenerateCodeResponse response = new GenerateCodeResponse(
        code,
        paymentCode.getExpiresAt().toString(),
        request.amountKc(),
        MoneyUtils.formatUsd(request.amountKc()),
        feeKc,
        MoneyUtils.formatUsd(feeKc),
        total);
    if (idempotencyKey != null) {
      idempotencyService.store("koini:idempotency:" + idempotencyKey, response,          Duration.ofHours(KoiniConstants.IDEMPOTENCY_TTL_HOURS));
    }
    return response;
  }

  /**
   * Redeems a payment code by a merchant.
   */
  @PreAuthorize("hasAnyRole('MERCHANT','CONDUCTOR')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public RedeemCodeResponse redeemPaymentCode(RedeemCodeRequest request, UUID conductorId,
      HttpServletRequest httpRequest, String idempotencyKey) {
    if (idempotencyKey != null) {
      Optional<RedeemCodeResponse> cached = idempotencyService.get(
          "koini:idempotency:" + idempotencyKey, RedeemCodeResponse.class);
      if (cached.isPresent()) {
        return cached.get();
      }
    }
    PaymentCode paymentCode = findPaymentCode(request.code());
    if (paymentCode.getStatus() != PaymentCodeStatus.PENDING) {
      throw new PaymentCodeAlreadyUsedException("Payment code already used");
    }
    if (paymentCode.getExpiresAt().isBefore(LocalDateTime.now())) {
      paymentCode.setStatus(PaymentCodeStatus.EXPIRED);
      paymentCodeRepository.save(paymentCode);
      throw new PaymentCodeExpiredException("Payment code expired");
    }

    Wallet passengerWallet = walletRepository.findByUserIdForUpdate(paymentCode.getHolder().getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    long total = paymentCode.getAmountKc() + paymentCode.getFeeKc();
    if (passengerWallet.getBalanceKc() < total) {
      throw new InsufficientBalanceException("Insufficient balance");
    }
    passengerWallet.setBalanceKc(passengerWallet.getBalanceKc() - total);
    paymentCode.setStatus(PaymentCodeStatus.REDEEMED);
    paymentCode.setRedeemedBy(User.builder().userId(conductorId).build());
    paymentCode.setRedeemedAt(LocalDateTime.now());
    paymentCodeRepository.save(paymentCode);

    String reference = generateReference("FARE");
    Transaction tx = Transaction.builder()
        .txType(TransactionType.FARE_PAYMENT)
        .fromWallet(passengerWallet)
        .toWallet(null)
        .amountKc(paymentCode.getAmountKc())
        .feeKc(paymentCode.getFeeKc())
        .status(TransactionStatus.COMPLETED)
        .reference(reference)
        .initiatedBy(paymentCode.getHolder())
        .description("Fare payment")
        .build();
    transactionRepository.save(tx);
    redisService.delete("koini:code:" + request.code());

    smsService.sendPaymentReceipt(paymentCode.getHolder().getPhone(), paymentCode.getAmountKc(), reference);
    auditService.log("CODE_REDEEMED", conductorId, "MERCHANT", "PaymentCode",
        paymentCode.getCodeId().toString(), null, paymentCode, AuditOutcome.SUCCESS, httpRequest);

    RedeemCodeResponse response = new RedeemCodeResponse(
        tx.getTxId().toString(),
        reference,
        paymentCode.getAmountKc(),
        MoneyUtils.formatUsd(paymentCode.getAmountKc()),
        PhoneUtils.mask(paymentCode.getHolder().getPhone()),
        null);
    if (idempotencyKey != null) {
      idempotencyService.store("koini:idempotency:" + idempotencyKey, response,          Duration.ofHours(KoiniConstants.IDEMPOTENCY_TTL_HOURS));
    }
    return response;
  }

  /**
   * Creates a payment request by a merchant.
   */
  @PreAuthorize("hasAnyRole('MERCHANT','CONDUCTOR')")
  public CreateRequestResponse createPaymentRequest(CreateRequestRequest request, UUID conductorId) {
    String normalized = PhoneUtils.normalize(request.passengerPhone());
    User passenger = userRepository.findByPhone(normalized)
        .orElseThrow(() -> new UserNotFoundException("Passenger not found"));
    User conductor = userRepository.findById(conductorId)
        .orElseThrow(() -> new UserNotFoundException("Merchant not found"));
    Wallet wallet = walletRepository.findByUserUserId(passenger.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    long feeKc = MoneyUtils.calculateFee(request.amountKc(), feeRate);
    if (wallet.getBalanceKc() < request.amountKc() + feeKc) {
      throw new InsufficientBalanceException("Insufficient balance");
    }
    PaymentRequest paymentRequest = PaymentRequest.builder()
        .conductor(conductor)
        .passenger(passenger)
        .amountKc(request.amountKc())
        .routeId(request.routeId() != null ? UUID.fromString(request.routeId()) : null)
        .status(PaymentReqStatus.PENDING)
        .expiresAt(LocalDateTime.now().plusSeconds(KoiniConstants.PAYMENT_REQUEST_TTL_SECONDS))
        .build();
    paymentRequestRepository.save(paymentRequest);
    smsService.sendPaymentRequest(passenger.getPhone(), request.amountKc(), "Merchant");
    redisService.set("koini:req:" + paymentRequest.getRequestId(),        paymentRequest.getStatus().name(), Duration.ofSeconds(KoiniConstants.PAYMENT_REQUEST_TTL_SECONDS));
    return new CreateRequestResponse(paymentRequest.getRequestId().toString(),
        PhoneUtils.mask(passenger.getPhone()), request.amountKc(),
        paymentRequest.getExpiresAt().toString());
  }

  /**
   * Approves a payment request by the passenger.
   */
  @PreAuthorize("hasAnyRole('CLIENT','PASSENGER')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public TransactionResponse approvePaymentRequest(ApproveRequestRequest request, UUID passengerId) {
    authService.verifyPin(request.pin(), passengerId);
    UUID requestId = UUID.fromString(request.requestId());
    PaymentRequest paymentRequest = paymentRequestRepository.findById(requestId)
        .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    if (paymentRequest.getStatus() != PaymentReqStatus.PENDING) {
      throw new DuplicatePaymentException("Request already processed");
    }
    if (paymentRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
      paymentRequest.setStatus(PaymentReqStatus.EXPIRED);
      paymentRequestRepository.save(paymentRequest);
      throw new PaymentRequestExpiredException("Payment request expired");
    }
    if (!paymentRequest.getPassenger().getUserId().equals(passengerId)) {
      throw new ResourceNotFoundException("Request not found");
    }
    Wallet wallet = walletRepository.findByUserIdForUpdate(passengerId)
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    long feeKc = MoneyUtils.calculateFee(paymentRequest.getAmountKc(), feeRate);
    long total = paymentRequest.getAmountKc() + feeKc;
    if (wallet.getBalanceKc() < total) {
      throw new InsufficientBalanceException("Insufficient balance");
    }
    wallet.setBalanceKc(wallet.getBalanceKc() - total);
    paymentRequest.setStatus(PaymentReqStatus.APPROVED);
    paymentRequest.setRespondedAt(LocalDateTime.now());
    paymentRequestRepository.save(paymentRequest);
    String reference = generateReference("FARE");
    Transaction tx = Transaction.builder()
        .txType(TransactionType.FARE_PAYMENT)
        .fromWallet(wallet)
        .amountKc(paymentRequest.getAmountKc())
        .feeKc(feeKc)
        .status(TransactionStatus.COMPLETED)
        .reference(reference)
        .initiatedBy(wallet.getUser())
        .description("Fare payment request")
        .build();
    transactionRepository.save(tx);
    redisService.set("koini:req:" + paymentRequest.getRequestId(), "APPROVED",        Duration.ofSeconds(KoiniConstants.PAYMENT_REQUEST_TTL_SECONDS));
    smsService.sendGenericAlert(paymentRequest.getConductor().getPhone(),
        "Payment approved. Ref: " + reference);
    return transactionMapper.toResponse(tx);
  }

  /**
   * Declines a payment request by the passenger.
   */
  @PreAuthorize("hasAnyRole('CLIENT','PASSENGER')")
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public PaymentRequestStatusResponse declinePaymentRequest(String requestId, UUID passengerId) {
    UUID id = UUID.fromString(requestId);
    PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    if (!paymentRequest.getPassenger().getUserId().equals(passengerId)) {
      throw new ResourceNotFoundException("Request not found");
    }
    paymentRequest.setStatus(PaymentReqStatus.DECLINED);
    paymentRequest.setRespondedAt(LocalDateTime.now());
    paymentRequestRepository.save(paymentRequest);
    redisService.set("koini:req:" + paymentRequest.getRequestId(), "DECLINED",        Duration.ofSeconds(KoiniConstants.PAYMENT_REQUEST_TTL_SECONDS));
    smsService.sendGenericAlert(paymentRequest.getConductor().getPhone(),
        "Payment declined for request " + paymentRequest.getRequestId());
    return new PaymentRequestStatusResponse(paymentRequest.getRequestId().toString(),
        paymentRequest.getStatus().name(), paymentRequest.getRespondedAt().toString());
  }

  /**
   * Polls the status of a payment request.
   */
  @PreAuthorize("hasAnyRole('MERCHANT','CONDUCTOR')")
  public PaymentRequestStatusResponse pollPaymentRequestStatus(String requestId, UUID conductorId) {
    String cached = redisService.get("koini:req:" + requestId).orElse(null);
    PaymentRequest paymentRequest = paymentRequestRepository.findById(UUID.fromString(requestId))
        .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    if (!paymentRequest.getConductor().getUserId().equals(conductorId)) {
      throw new ResourceNotFoundException("Request not found");
    }
    String status = cached != null ? cached : paymentRequest.getStatus().name();
    String respondedAt = paymentRequest.getRespondedAt() != null ? paymentRequest.getRespondedAt().toString() : null;
    return new PaymentRequestStatusResponse(requestId, status, respondedAt);
  }

  private PaymentCode findPaymentCode(String code) {
    String cachedId = redisService.get("koini:code:" + code).orElse(null);
    if (cachedId != null) {
      return paymentCodeRepository.findById(UUID.fromString(cachedId))
          .orElseThrow(() -> new PaymentCodeInvalidException("Invalid code"));
    }
    List<PaymentCode> active = paymentCodeRepository.findActiveCodes(LocalDateTime.now());
    return active.stream()
        .filter(pc -> passwordEncoder.matches(code, pc.getCodeHash()))
        .findFirst()
        .orElseThrow(() -> new PaymentCodeInvalidException("Invalid code"));
  }

  private String generateSixDigitCode() {
    int code = new java.security.SecureRandom().nextInt(900000) + 100000;
    return String.valueOf(code);
  }

  private String generateReference(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
  }
}
