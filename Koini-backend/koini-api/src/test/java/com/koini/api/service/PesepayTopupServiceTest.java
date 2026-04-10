package com.koini.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koini.api.config.PesepayProperties;
import com.koini.api.service.integration.PesepayClient;
import com.koini.api.service.money.MoneyConversionService;
import com.koini.api.service.topup.PesepayTopupService;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.TransactionStatus;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.domain.enums.WalletStatus;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PesepayTopupServiceTest {

  @Mock WalletRepository walletRepository;
  @Mock TransactionRepository transactionRepository;
  @Mock PesepayClient pesepayClient;
  @Mock PesepayProperties pesepayProperties;
  @Mock MoneyConversionService moneyConversionService;
  @Mock AuditService auditService;
  @Mock HttpServletRequest httpServletRequest;

  private ObjectMapper objectMapper;
  private PesepayTopupService service;

  @BeforeEach
  void setup() {
    objectMapper = new ObjectMapper();
    service = new PesepayTopupService(
        walletRepository,
        transactionRepository,
        pesepayClient,
        pesepayProperties,
        moneyConversionService,
        objectMapper,
        auditService,
        1.0
    );
  }

  @Test
  @DisplayName("confirm: credits KC based on paid USD amount using tx kcPerUsd")
  void confirm_creditsUsingConversionRateFromTx() {
    UUID txId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = User.builder().userId(userId).build();
    Wallet wallet = Wallet.builder()
        .walletId(UUID.randomUUID())
        .user(user)
        .balanceKc(0L)
        .points(0L)
        .status(WalletStatus.ACTIVE)
        .build();

    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("userId", userId.toString());
    meta.put("kcPerUsd", 10_000);
    meta.put("amountUsd", new BigDecimal("0.01"));
    meta.put("pesepayReferenceNumber", "ref-123");

    Transaction tx = Transaction.builder()
        .txId(txId)
        .txType(TransactionType.TOPUP)
        .status(TransactionStatus.PENDING)
        .amountKc(100L)
        .feeKc(0L)
        .reference("TOPUP-REF")
        .toWallet(wallet)
        .metadata(objectMapper.valueToTree(meta))
        .build();

    Map<String, Object> raw = Map.of("amountDetails", Map.of("amount", "0.01", "currencyCode", "USD"));
    when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
    when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
    when(pesepayClient.checkPayment("ref-123"))
        .thenReturn(new PesepayClient.PesepayCheckResponse("ref-123", null, true, "SUCCESS", raw));
    when(moneyConversionService.kcPerUsd()).thenReturn(10_000L);
    when(moneyConversionService.toKc(new BigDecimal("0.01"), 10_000L)).thenReturn(100L);
    when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    doNothing().when(auditService).log(any(), any(), any(), any(), any(), any(), any(), any(), any());

    PesepayTopupService.ConfirmTopupResult result = service.confirm(txId, "ref-123", httpServletRequest);

    assertThat(result.paid()).isTrue();
    assertThat(result.amountKcCredited()).isEqualTo(100L);
    assertThat(result.pointsAdded()).isEqualTo(100L);
    assertThat(wallet.getBalanceKc()).isEqualTo(100L);
    assertThat(wallet.getPoints()).isEqualTo(100L);
    assertThat(tx.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
  }
}

