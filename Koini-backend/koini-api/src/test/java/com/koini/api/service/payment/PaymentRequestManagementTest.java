package com.koini.api.service.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.koini.api.dto.response.CancelPaymentRequestResponse;
import com.koini.api.dto.response.MerchantPaymentRequestListResponse;
import com.koini.api.mapper.TransactionMapper;
import com.koini.api.service.AuditService;
import com.koini.api.service.IdempotencyService;
import com.koini.api.service.RedisService;
import com.koini.api.service.auth.AuthService;
import com.koini.core.domain.entity.PaymentRequest;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.PaymentReqStatus;
import com.koini.notification.sms.SmsService;
import com.koini.persistence.repository.PaymentCodeRepository;
import com.koini.persistence.repository.PaymentRequestRepository;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PaymentRequestManagementTest {

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
  void cancelPaymentRequest_setsCancelled() {
    UUID merchantId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    User merchant = User.builder().userId(merchantId).build();
    User passenger = User.builder().userId(UUID.randomUUID()).phone("0782846870").build();

    PaymentRequest pr = PaymentRequest.builder()
        .requestId(requestId)
        .conductor(merchant)
        .passenger(passenger)
        .amountKc(100)
        .status(PaymentReqStatus.PENDING)
        .expiresAt(LocalDateTime.now().plusMinutes(10))
        .build();

    when(paymentRequestRepository.findByRequestIdAndConductorUserId(requestId, merchantId)).thenReturn(Optional.of(pr));
    when(paymentRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    CancelPaymentRequestResponse response = paymentService.cancelPaymentRequest(requestId.toString(), merchantId, null);

    assertThat(response.requestId()).isEqualTo(requestId.toString());
    assertThat(response.status()).isEqualTo(PaymentReqStatus.CANCELLED.name());
    assertThat(pr.getStatus()).isEqualTo(PaymentReqStatus.CANCELLED);
    verify(paymentRequestRepository).save(eq(pr));
    verify(redisService).set(any(), eq("CANCELLED"), any());
    verify(smsService).sendGenericAlert(eq("0782846870"), any());
  }

  @Test
  void listPaymentRequests_returnsItems() {
    UUID merchantId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    User merchant = User.builder().userId(merchantId).build();
    User passenger = User.builder().userId(UUID.randomUUID()).phone("0782846870").build();
    PaymentRequest pr = PaymentRequest.builder()
        .requestId(requestId)
        .conductor(merchant)
        .passenger(passenger)
        .amountKc(100)
        .status(PaymentReqStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusMinutes(10))
        .build();

    when(paymentRequestRepository.findForConductorBetween(eq(merchantId), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(pr)));

    MerchantPaymentRequestListResponse response = paymentService.listPaymentRequests(
        merchantId, null, LocalDate.now(), LocalDate.now(), 0, 20);

    assertThat(response.requests()).hasSize(1);
    assertThat(response.requests().get(0).requestId()).isEqualTo(requestId.toString());
    assertThat(response.requests().get(0).status()).isEqualTo(PaymentReqStatus.PENDING.name());
  }
}
