package com.koini.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.koini.api.dto.request.GenerateCodeRequest;
import com.koini.api.dto.response.GenerateCodeResponse;
import com.koini.api.mapper.TransactionMapper;
import com.koini.api.service.auth.AuthService;
import com.koini.api.service.payment.PaymentService;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
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
        .status(WalletStatus.ACTIVE).build();
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
}
