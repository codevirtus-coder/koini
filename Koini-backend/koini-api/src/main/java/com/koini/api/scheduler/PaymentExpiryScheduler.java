package com.koini.api.scheduler;

import com.koini.persistence.repository.PaymentCodeRepository;
import com.koini.persistence.repository.PaymentRequestRepository;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentExpiryScheduler {

  private final PaymentCodeRepository paymentCodeRepository;
  private final PaymentRequestRepository paymentRequestRepository;

  public PaymentExpiryScheduler(
      PaymentCodeRepository paymentCodeRepository,
      PaymentRequestRepository paymentRequestRepository
  ) {
    this.paymentCodeRepository = paymentCodeRepository;
    this.paymentRequestRepository = paymentRequestRepository;
  }

  /**
   * Expires stale payment codes.
   */
  @Scheduled(fixedDelay = 60000)
  @Transactional
  public void expireCodes() {
    paymentCodeRepository.expireStaleCodesBeforeTime(LocalDateTime.now());
  }

  /**
   * Expires stale payment requests.
   */
  @Scheduled(fixedDelay = 60000)
  @Transactional
  public void expireRequests() {
    paymentRequestRepository.expireStaleRequestsBeforeTime(LocalDateTime.now());
  }
}
