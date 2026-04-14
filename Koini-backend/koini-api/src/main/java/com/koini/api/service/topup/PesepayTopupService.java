package com.koini.api.service.topup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koini.api.config.PesepayProperties;
import com.koini.api.service.AuditService;
import com.koini.api.service.integration.PesepayClient;
import com.koini.api.service.money.MoneyConversionService;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.AuditOutcome;
import com.koini.core.domain.enums.TransactionStatus;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.domain.enums.WalletStatus;
import com.koini.core.exception.MisconfigurationException;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.core.exception.WalletNotFoundException;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PesepayTopupService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final PesepayClient pesepayClient;
  private final PesepayProperties pesepayProperties;
  private final ObjectMapper objectMapper;
  private final AuditService auditService;
  private final MoneyConversionService moneyConversionService;
  private final double pointsMultiplier;

  public PesepayTopupService(
      WalletRepository walletRepository,
      TransactionRepository transactionRepository,
      PesepayClient pesepayClient,
      PesepayProperties pesepayProperties,
      MoneyConversionService moneyConversionService,
      ObjectMapper objectMapper,
      AuditService auditService,
      @Value("${koini.points.topup-multiplier:1.0}") double pointsMultiplier
  ) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.pesepayClient = pesepayClient;
    this.pesepayProperties = pesepayProperties;
    this.moneyConversionService = moneyConversionService;
    this.objectMapper = objectMapper;
    this.auditService = auditService;
    this.pointsMultiplier = pointsMultiplier;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public InitiateTopupResult initiate(UUID userId, long amountKc, String currencyCode, HttpServletRequest httpRequest) {
    if (amountKc <= 0) {
      throw new ResourceNotFoundException("Invalid top-up amount");
    }
    Wallet wallet = walletRepository.findByUserUserId(userId)
        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    if (wallet.getStatus() != WalletStatus.ACTIVE) {
      throw new ResourceNotFoundException("Wallet not active");
    }

    Transaction tx = Transaction.builder()
        .txType(TransactionType.TOPUP)
        .fromWallet(null)
        .toWallet(wallet)
        .amountKc(amountKc)
        .feeKc(0)
        .status(TransactionStatus.PENDING)
        .reference(generateReference("TOPUP"))
        .initiatedBy(User.builder().userId(userId).build())
        .description("Pesepay wallet top-up (pending)")
        .build();
    tx = transactionRepository.save(tx);

    String returnUrl = appendQuery(pesepayProperties.getReturnUrl(), "txId", tx.getTxId().toString());
    String resultUrl = appendQuery(pesepayProperties.getResultUrl(), "txId", tx.getTxId().toString());
    long kcPerUsd = moneyConversionService.kcPerUsd();
    BigDecimal amountUsd = moneyConversionService.toUsd(amountKc, kcPerUsd);

    PesepayClient.PesepayInitiateResponse pesepay = pesepayClient.initiateWalletTopup(
        amountKc,
        currencyCode,
        "Wallet top-up",
        tx.getReference(),
        returnUrl,
        resultUrl
    );

    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("provider", "PESEPAY");
    meta.put("pesepayReferenceNumber", pesepay.referenceNumber());
    meta.put("pollUrl", pesepay.pollUrl());
    meta.put("redirectUrl", pesepay.redirectUrl());
    meta.put("currencyCode", currencyCode);
    meta.put("userId", userId.toString());
    meta.put("pointsMultiplier", pointsMultiplier);
    meta.put("kcPerUsd", kcPerUsd);
    meta.put("amountUsd", amountUsd);
    tx.setMetadata(objectMapper.valueToTree(meta));
    transactionRepository.save(tx);

    auditService.log("TOPUP_INITIATED", userId, "USER", "Transaction", tx.getTxId().toString(),
        null, meta, AuditOutcome.SUCCESS, httpRequest);

    return new InitiateTopupResult(
        tx.getTxId().toString(),
        tx.getReference(),
        amountKc,
        amountUsd,
        moneyConversionService.formatUsd(amountKc, kcPerUsd),
        kcPerUsd,
        pesepay.referenceNumber(),
        pesepay.pollUrl(),
        pesepay.redirectUrl());
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ConfirmTopupResult confirm(UUID txId, String pesepayReferenceNumber, HttpServletRequest httpRequest) {
    Transaction tx = transactionRepository.findById(txId)
        .orElseThrow(() -> new ResourceNotFoundException("Top-up not found"));

    if (tx.getTxType() != TransactionType.TOPUP) {
      throw new ResourceNotFoundException("Top-up not found");
    }

    if (tx.getStatus() == TransactionStatus.COMPLETED) {
      long amountKcCredited = readMetaLong(tx.getMetadata(), "amountKcCredited", tx.getAmountKc());
      long pointsAdded = readMetaLong(tx.getMetadata(), "pointsAdded", 0L);
      return new ConfirmTopupResult(tx.getTxId().toString(), tx.getReference(), true, "already_confirmed",
          amountKcCredited, pointsAdded);
    }

    String ref = (pesepayReferenceNumber != null && !pesepayReferenceNumber.isBlank())
        ? pesepayReferenceNumber
        : readMeta(tx.getMetadata(), "pesepayReferenceNumber");
    if (ref == null || ref.isBlank()) {
      throw new ResourceNotFoundException("Missing payment reference");
    }

    PesepayClient.PesepayCheckResponse check = pesepayClient.checkPayment(ref);
    if (!check.paid()) {
      auditService.log("TOPUP_NOT_PAID", null, "SYSTEM", "Transaction", tx.getTxId().toString(),
          null, Map.of("ref", ref, "status", check.transactionStatus()), AuditOutcome.FAILURE, httpRequest);
      return new ConfirmTopupResult(tx.getTxId().toString(), tx.getReference(), false, check.transactionStatus(), 0, 0);
    }

    String userIdString = readMeta(tx.getMetadata(), "userId");
    UUID userId = userIdString != null ? UUID.fromString(userIdString) : null;
    if (userId == null) {
      throw new ResourceNotFoundException("Missing user for top-up");
    }

    Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

    long kcPerUsd = readMetaLong(tx.getMetadata(), "kcPerUsd", moneyConversionService.kcPerUsd());
    BigDecimal amountUsdPaid = extractAmountUsd(check.raw());
    if (amountUsdPaid == null) {
      amountUsdPaid = readMetaDecimal(tx.getMetadata(), "amountUsd");
    }

    long amountKcCredited = tx.getAmountKc();
    if (amountUsdPaid != null && kcPerUsd > 0) {
      amountKcCredited = moneyConversionService.toKc(amountUsdPaid, kcPerUsd);
    }

    long pointsAdded = amountKcCredited;
    wallet.setPoints(wallet.getPoints() + pointsAdded);
    wallet.setBalanceKc(wallet.getPoints());

    tx.setMetadata(objectMapper.valueToTree(enrichMeta(tx.getMetadata(), amountUsdPaid, amountKcCredited, pointsAdded)));
    tx.setStatus(TransactionStatus.COMPLETED);
    tx.setDescription("Pesepay wallet top-up");
    transactionRepository.save(tx);

    auditService.log("TOPUP_COMPLETED", userId, "USER", "Transaction", tx.getTxId().toString(),
        null, Map.of("amountKcCredited", amountKcCredited, "pointsAdded", pointsAdded), AuditOutcome.SUCCESS,
        httpRequest);

    return new ConfirmTopupResult(tx.getTxId().toString(), tx.getReference(), true, "SUCCESS",
        amountKcCredited, pointsAdded);
  }

  private String readMeta(JsonNode meta, String field) {
    if (meta == null) {
      return null;
    }
    JsonNode node = meta.get(field);
    return node != null && !node.isNull() ? node.asText() : null;
  }

  private long readMetaLong(JsonNode meta, String field, long defaultValue) {
    if (meta == null) {
      return defaultValue;
    }
    JsonNode node = meta.get(field);
    if (node == null || node.isNull()) {
      return defaultValue;
    }
    if (node.canConvertToLong()) {
      return node.asLong();
    }
    try {
      return Long.parseLong(node.asText());
    } catch (Exception ex) {
      return defaultValue;
    }
  }

  private BigDecimal readMetaDecimal(JsonNode meta, String field) {
    String value = readMeta(meta, field);
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(value.trim());
    } catch (Exception ex) {
      return null;
    }
  }

  private BigDecimal extractAmountUsd(Map<String, Object> raw) {
    if (raw == null) {
      return null;
    }
    Object amountDetails = raw.get("amountDetails");
    if (amountDetails instanceof Map<?, ?> detailsMap) {
      Object amount = detailsMap.get("amount");
      BigDecimal parsed = parseDecimal(amount);
      if (parsed != null) {
        return parsed;
      }
    }
    // fallback: some APIs return "amount" at the root
    return parseDecimal(raw.get("amount"));
  }

  private BigDecimal parseDecimal(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof BigDecimal bd) {
      return bd;
    }
    if (value instanceof Number number) {
      return new BigDecimal(number.toString());
    }
    String s = value.toString();
    if (s == null) {
      return null;
    }
    s = s.trim();
    if (s.isEmpty()) {
      return null;
    }
    // strip common formatting like "$1.00"
    s = s.replace("$", "").trim();
    try {
      return new BigDecimal(s);
    } catch (Exception ex) {
      return null;
    }
  }

  private Map<String, Object> enrichMeta(JsonNode existing, BigDecimal amountUsdPaid, long amountKcCredited,
      long pointsAdded) {
    Map<String, Object> meta = existing != null
        ? objectMapper.convertValue(existing, new TypeReference<Map<String, Object>>() {})
        : new LinkedHashMap<>();
    meta.put("amountUsdPaid", amountUsdPaid);
    meta.put("amountKcCredited", amountKcCredited);
    meta.put("pointsAdded", pointsAdded);
    return meta;
  }

  private String appendQuery(String baseUrl, String key, String value) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new MisconfigurationException("Pesepay returnUrl/resultUrl not configured",
          "Set KOINI_PESEPAY_RETURN_URL and KOINI_PESEPAY_RESULT_URL and restart the API.");
    }
    String sep = baseUrl.contains("?") ? "&" : "?";
    return baseUrl + sep + key + "=" + value;
  }

  private String generateReference(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
  }

  public record InitiateTopupResult(
      String txId,
      String reference,
      long amountKc,
      BigDecimal amountUsd,
      String amountUsdFormatted,
      long kcPerUsd,
      String pesepayReferenceNumber,
      String pollUrl,
      String redirectUrl
  ) {
  }

  public record ConfirmTopupResult(
      String txId,
      String reference,
      boolean paid,
      String status,
      long amountKcCredited,
      long pointsAdded
  ) {
  }
}
