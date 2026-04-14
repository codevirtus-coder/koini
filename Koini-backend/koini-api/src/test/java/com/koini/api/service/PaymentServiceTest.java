package com.koini.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.koini.api.dto.request.GenerateCodeRequest;
import com.koini.api.dto.request.RedeemCodeRequest;
import com.koini.api.dto.response.GenerateCodeResponse;
import com.koini.api.mapper.TransactionMapper;
import com.koini.api.service.auth.AuthService;
import com.koini.api.service.payment.PaymentService;
import com.koini.core.domain.entity.PaymentCode;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.PaymentCodeStatus;
import com.koini.core.domain.enums.WalletStatus;
import com.koini.notification.sms.SmsService;
import com.koini.persistence.repository.PaymentCodeRepository;
import com.koini.persistence.repository.PaymentRequestRepository;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock PaymentCodeRepository paymentCodeRepository;
  @Mock PaymentRequestRepository paymentRequestRepository;
  @Mock WalletRepository walletRepository;
  @Mock UserRepository userRepository;
  @Mock TransactionRepository transactionRepository;
  @Mock AuthService authService;
  @Mock RedisService redisService;
  @Mock IdempotencyService idempotencyService;
  @Mock PasswordEncoder passwordEncoder;
  @Mock SmsService smsService;
  @Mock AuditService auditService;
  @Mock TransactionMapper transactionMapper;

  private PaymentService paymentService;

  @BeforeEach
  void setup() {
    paymentService = new PaymentService(
        paymentCodeRepository,
        paymentRequestRepository,
        walletRepository,
        userRepository,
        transactionRepository,
        authService,
        redisService,
        idempotencyService,
        passwordEncoder,
        smsService,
        auditService,
        transactionMapper,
        1.5
    );
  }

  @Test
  @DisplayName("generatePaymentCode: should generate code when balance sufficient")
  void generateCode_success() {
    UUID userId = UUID.randomUUID();
    User user = User.builder().userId(userId).build();
    Wallet wallet = Wallet.builder().walletId(UUID.randomUUID()).user(user).balanceKc(1000)
        .points(1000).status(WalletStatus.ACTIVE).build();
    when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
    when(passwordEncoder.encode(any())).thenReturn("hash");
    when(redisService.increment(any())).thenReturn(1L);
    when(paymentCodeRepository.save(any())).thenAnswer(invocation -> {
      com.koini.core.domain.entity.PaymentCode code = invocation.getArgument(0);
      code.setCodeId(UUID.randomUUID());
      return code;
    });

    GenerateCodeRequest request = new GenerateCodeRequest(100, "1234", null);
    GenerateCodeResponse response = paymentService.generatePaymentCode(request, userId, null);

    assertThat(response.amountKc()).isEqualTo(100);
    assertThat(response.code()).isNotBlank();
  }

  @Test
  @DisplayName("redeemPaymentCode: should deduct balance+points and credit merchant wallet")
  void redeemPaymentCode_success() {
    String code = "123456";
    UUID paymentCodeId = UUID.randomUUID();
    UUID passengerId = UUID.randomUUID();
    UUID merchantId = UUID.randomUUID();

    User passenger = User.builder().userId(passengerId).phone("0782846870").build();
    User merchant = User.builder().userId(merchantId).phone("0711111111").build();

    PaymentCode pc = PaymentCode.builder()
        .codeId(paymentCodeId)
        .codeHash("hash")
        .holder(passenger)
        .amountKc(100)
        .feeKc(10)
        .status(PaymentCodeStatus.PENDING)
        .expiresAt(java.time.LocalDateTime.now().plusMinutes(5))
        .build();

    Wallet passengerWallet = Wallet.builder().walletId(UUID.randomUUID()).user(passenger).balanceKc(1000).points(1000)
        .status(WalletStatus.ACTIVE).build();
    Wallet merchantWallet = Wallet.builder().walletId(UUID.randomUUID()).user(merchant).balanceKc(0).points(0)
        .status(WalletStatus.ACTIVE).build();

    when(redisService.get("koini:code:" + code)).thenReturn(Optional.of(paymentCodeId.toString()));
    when(paymentCodeRepository.findById(paymentCodeId)).thenReturn(Optional.of(pc));
    when(walletRepository.findByUserIdForUpdate(passengerId)).thenReturn(Optional.of(passengerWallet));
    when(walletRepository.findByUserIdForUpdate(merchantId)).thenReturn(Optional.of(merchantWallet));
    when(transactionRepository.save(any())).thenAnswer(invocation -> {
      Transaction tx = invocation.getArgument(0);
      tx.setTxId(UUID.randomUUID());
      return tx;
    });

    RedeemCodeRequest req = new RedeemCodeRequest(code);
    paymentService.redeemPaymentCode(req, merchantId, null, null);

    assertThat(passengerWallet.getBalanceKc()).isEqualTo(890);
    assertThat(passengerWallet.getPoints()).isEqualTo(890);
    assertThat(merchantWallet.getBalanceKc()).isEqualTo(100);
    assertThat(merchantWallet.getPoints()).isEqualTo(100);

    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository).save(captor.capture());
    assertThat(captor.getValue().getFromWallet()).isSameAs(passengerWallet);
    assertThat(captor.getValue().getToWallet()).isSameAs(merchantWallet);
  }
}
